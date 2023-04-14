package com.radynamics.dallipay.cryptoledger.ethereum.api;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.Cache;
import com.radynamics.dallipay.cryptoledger.MoneyBag;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.TransactionResult;
import com.radynamics.dallipay.cryptoledger.ethereum.Ledger;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import org.apache.commons.codec.DecoderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONObject;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
        var tr = new TransactionResult();
        // https://docs.alchemy.com/reference/alchemy-getassettransfers
        // TODO: overhaul and replace demo code
        var client = new DefaultAsyncHttpClient();
        var fromBlock = "0x103FDDA"; // "0x0"
        var toBlock = "latest"; // "latest", "0x103FDDF"
        var maxCount = "0x" + Integer.toHexString(3); // "0x3e8";
        client.prepare("POST", network.getUrl().toString())
                .setHeader("accept", "application/json")
                .setHeader("content-type", "application/json")
                .setBody("{\"id\":1,\"jsonrpc\":\"2.0\",\"method\":\"alchemy_getAssetTransfers\",\"params\":[{\"fromBlock\":\"" + fromBlock + "\",\"toBlock\":\"" + toBlock + "\",\"toAddress\":\"" + wallet.getPublicKey() + "\",\"category\":[\"external\"],\"withMetadata\":false,\"excludeZeroValue\":true,\"maxCount\":\"" + maxCount + "\",\"order\":\"desc\"}]}")
                .execute()
                .toCompletableFuture()
                .thenAccept(response -> {
                    try {
                        readTransactions(tr, new JSONObject(response.getResponseBody()));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                })
                .join();

        client.close();
        return tr;
    }

    private void readTransactions(TransactionResult tr, JSONObject json) throws DecoderException, UnsupportedEncodingException, ExecutionException, InterruptedException {
        var transfers = json.getJSONObject("result").getJSONArray("transfers");
        for (var i = 0; i < transfers.length(); i++) {
            tr.add(toTransaction(transfers.getJSONObject(i)));
        }
    }

    private com.radynamics.dallipay.cryptoledger.xrpl.Transaction toTransaction(JSONObject t) throws DecoderException, UnsupportedEncodingException, ExecutionException, InterruptedException {
        var amt = Money.of(t.getDouble("value"), new Currency(t.getString("asset")));
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
        if (!useCache) {
            accountBalanceCache.evict(wallet);
        }

        accountBalanceCache.evictOutdated();
        var data = accountBalanceCache.get(wallet);
        // Contained without data means "wallet doesn't exist" (wasn't found previously)
        if (data != null || accountBalanceCache.isPresent(wallet)) {
            return;
        }

        // TODO: implement ERC20
        try {
            var result = web3.ethGetBalance(wallet.getPublicKey(), DefaultBlockParameterName.LATEST).sendAsync().get();
            wallet.getBalances().set(Money.of(weiToEth(result.getBalance()), new Currency(ledger.getNativeCcySymbol())));
            accountBalanceCache.add(wallet, wallet.getBalances());
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
}
