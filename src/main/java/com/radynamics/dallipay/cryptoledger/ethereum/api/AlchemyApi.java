package com.radynamics.dallipay.cryptoledger.ethereum.api;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.ethereum.Ledger;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.UserDialogPrivateKeyProvider;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import org.apache.commons.codec.DecoderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;

public class AlchemyApi {
    final static Logger log = LogManager.getLogger(AlchemyApi.class);
    private final com.radynamics.dallipay.cryptoledger.ethereum.Ledger ledger;
    private final NetworkInfo network;
    private final Web3j web3;
    private final Cache<MoneyBag> accountBalanceCache;

    public AlchemyApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
        web3 = Web3j.build(new HttpService(network.getUrl().toString()));
        this.accountBalanceCache = new Cache<>(network.getUrl().toString());
    }

    public TransactionResult listPaymentsReceived(com.radynamics.dallipay.cryptoledger.Wallet wallet, DateTimeRange period) throws Exception {
        // https://docs.alchemy.com/reference/alchemy-getassettransfers
        var maxCount = "0x" + Integer.toHexString(3); // "0x3e8";

        var params = new JSONObject();
        params.put("fromBlock", "0x0"); // "0x0", "0x103FDDA"
        params.put("toBlock", "latest"); // "latest", "0x103FDDF"
        params.put("toAddress", wallet.getPublicKey());
        var categories = new JSONArray();
        categories.put("external");
        categories.put("erc20");
        params.put("category", categories);
        params.put("withMetadata", false);
        params.put("excludeZeroValue", true);
        params.put("maxCount", maxCount);
        params.put("order", "desc");
        var data = createRequestData("alchemy_getAssetTransfers", params);

        var json = post(HttpRequest.BodyPublishers.ofString(data.toString()));

        var tr = new TransactionResult();
        readTransactions(tr, json);
        return tr;
    }

    private JSONObject post(HttpRequest.BodyPublisher body) throws IOException, InterruptedException, AlchemyException {
        return send(createRequestBuilder().POST(body).build());
    }

    private JSONObject send(HttpRequest request) throws IOException, InterruptedException, AlchemyException {
        var client = HttpClient.newHttpClient();
        var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleStatusCode(httpResponse.statusCode());
        var responseText = httpResponse.body();
        try {
            var json = new JSONObject(responseText);
            throwIfError(json);
            return json;
        } catch (Exception e) {
            throw new AlchemyException(e.getMessage(), e);
        }
    }

    private HttpRequest.Builder createRequestBuilder() {
        return HttpRequest.newBuilder()
                .uri(network.getUrl().uri())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
    }

    private static JSONObject createRequestData(String method, JSONObject params) {
        var paramsArray = new JSONArray();
        paramsArray.put(params);
        return createRequestData(method, paramsArray);
    }

    private static JSONObject createRequestData(String method, JSONArray params) {
        var d = new JSONObject();
        d.put("id", 1);
        d.put("jsonrpc", "2.0");
        d.put("method", method);
        d.put("params", params);
        return d;
    }

    private void handleStatusCode(Integer statusCode) throws AlchemyException {
        var statusCodeText = statusCode.toString();
        var msg = String.format("Alchemy API responded HTTP status code %s.", statusCodeText);
        if (statusCodeText.startsWith("2")) {
            log.debug(msg);
            return;
        }

        if (statusCodeText.startsWith("1") || statusCodeText.startsWith("3")) {
            log.info(msg);
            return;
        }

        throw new AlchemyException(msg);
    }

    private void throwIfError(JSONObject json) throws AlchemyException {
        var error = json.optJSONObject("error");
        if (error == null) {
            return;
        }

        throw new AlchemyException(String.format("%s (Code %s)", error.getString("message"), error.getInt("code")));
    }

    private void readTransactions(TransactionResult tr, JSONObject json) throws DecoderException, UnsupportedEncodingException, ExecutionException, InterruptedException {
        var transfers = json.getJSONObject("result").getJSONArray("transfers");
        for (var i = 0; i < transfers.length(); i++) {
            tr.add(toTransaction(transfers.getJSONObject(i)));
        }
    }

    private com.radynamics.dallipay.cryptoledger.xrpl.Transaction toTransaction(JSONObject t) throws DecoderException, UnsupportedEncodingException, ExecutionException, InterruptedException {
        var ccy = "erc20".equals(t.optString("category"))
                ? new Currency(t.getString("asset"), ledger.createWallet(t.getJSONObject("rawContract").getString("address")))
                : new Currency(t.getString("asset"));
        var amt = Money.of(t.getDouble("value"), ccy);
        var trx = new com.radynamics.dallipay.cryptoledger.xrpl.Transaction(ledger, amt);
        trx.setId(t.getString("hash"));
        trx.setBooked(getDateTimeOfBlock(BigInteger.valueOf(Integer.decode(t.getString("blockNum")))));
        trx.setSender(ledger.createWallet(t.getString("from")));
        trx.setReceiver(ledger.createWallet(t.getString("to")));

        // TODO: read transaction data/messages
        /*for (MemoWrapper mw : t.memos()) {
            if (!mw.memo().memoData().isPresent()) {
                continue;
            }
            var unwrappedMemo = PayloadConverter.fromMemo(Utils.hexToString(mw.memo().memoData().get()));
            for (var ft : unwrappedMemo.freeTexts()) {
                trx.addMessage(ft);
            }
        }

        var l = new StructuredReferenceLookup(t);
        for (var r : l.find()) {
            trx.addStructuredReference(r);
        }*/

        return trx;
    }

    private ZonedDateTime getDateTimeOfBlock(BigInteger blockNum) throws ExecutionException, InterruptedException {
        // TODO: consider local cache
        var result = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNum), false).sendAsync().get();

        var instant = Instant.ofEpochSecond(result.getBlock().getTimestamp().longValue());
        return DateTimeConvert.toUserTimeZone(ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")));
    }

    public void refreshBalance(com.radynamics.dallipay.cryptoledger.Wallet wallet, boolean useCache) {
        var key = new WalletKey(wallet);
        if (!useCache) {
            accountBalanceCache.evict(key);
        }

        accountBalanceCache.evictOutdated();
        var data = accountBalanceCache.get(key);
        if (data != null) {
            wallet.getBalances().replaceBy(data);
            return;
        }
        // Contained without data means "wallet doesn't exist" (wasn't found previously)
        if (accountBalanceCache.isPresent(key)) {
            return;
        }

        // TODO: implement ERC20
        try {
            var result = web3.ethGetBalance(wallet.getPublicKey(), DefaultBlockParameterName.LATEST).sendAsync().get();
            wallet.getBalances().set(Money.of(weiToEth(result.getBalance()), new Currency(ledger.getNativeCcySymbol())));
            accountBalanceCache.add(key, wallet.getBalances());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Double weiToEth(BigInteger value) {
        var scaledBalance = new BigDecimal(value).divide(
                new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP
        );
        return scaledBalance.doubleValue();
    }

    public Money estimatedGasPrice() {
        try {
            var result = web3.ethGasPrice().sendAsync().get();
            return Money.of(weiToEth(result.getGasPrice()), new Currency(ledger.getNativeCcySymbol()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public TransactionSubmitter createTransactionSubmitter(UserDialogPrivateKeyProvider privateKeyProvider) {
        return new RpcSubmitter(ledger, web3, privateKeyProvider);
    }
}
