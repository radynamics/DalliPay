package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.EndpointInfo;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.TransactionResult;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Device;
import com.radynamics.dallipay.cryptoledger.bitcoin.signing.BitcoinCoreRpcSubmitter;
import com.radynamics.dallipay.cryptoledger.generic.WalletInput;
import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JsonRpcApi {
    final static Logger log = LogManager.getLogger(JsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;
    private final MultiWalletJsonRpcApi openedWallets;

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
        this.openedWallets = new MultiWalletJsonRpcApi(ledger, network);
    }

    public TransactionResult listPaymentsSent(com.radynamics.dallipay.cryptoledger.generic.Wallet from, long sinceDaysAgo, int limit) throws ApiException {
        // Use endOfToday to ensure data until latest ledger is loaded.
        var period = DateTimeRange.of(Utils.endOfToday().minusDays(sinceDaysAgo), Utils.endOfToday());

        var tr = new TransactionResult();
        try {
            var transactions = openedWallets.listByAddress(from, 9999);
            var filtered = transactions.stream()
                    .filter(inPeriod(period))
                    .filter(outgoing())
                    .collect(Collectors.toList());
            for (var t : filtered) {
                tr.add(toTransaction(t, from));
            }
        } catch (Exception e) {
            throwException(e);
        }
        return tr;
    }

    public TransactionResult listPaymentsReceived(WalletInput walletInput, DateTimeRange period) throws ApiException {
        var tr = new TransactionResult();
        try {
            var transactions = listPaymentsReceived(walletInput);
            var filtered = transactions.stream()
                    .filter(inPeriod(period))
                    .filter(incoming())
                    .collect(Collectors.toList());
            for (var t : filtered) {
                tr.add(toTransaction(t, walletInput.wallet()));
            }
        } catch (Exception e) {
            throwException(e);
        }
        return tr;
    }

    private void throwException(Exception e) throws ApiException {
        var errorJson = BitcoinCoreRpcClientExt.errorJson(e);
        if (errorJson.isPresent()) {
            throw new ApiException("%s (Code %s)".formatted(errorJson.get().getString("message"), errorJson.get().getInt("code")));
        } else {
            throw new RuntimeException(e);
        }
    }

    private Predicate<? super BitcoindRpcClient.Transaction> outgoing() {
        // Skip incoming tx and tx without an amount.
        return t -> t.amount().compareTo(BigDecimal.ZERO) <= 0;
    }

    private Predicate<? super BitcoindRpcClient.Transaction> incoming() {
        // Skip outgoing tx and tx without an amount.
        return t -> t.amount().compareTo(BigDecimal.ZERO) > 0;
    }

    private Predicate<? super BitcoindRpcClient.Transaction> inPeriod(DateTimeRange period) {
        return t ->
                // Skip unconfirmed
                t.blockTime() != null
                        && period.isBetween(ZonedDateTime.ofInstant(t.blockTime().toInstant(), ZoneId.of("UTC")));
    }

    private List<BitcoindRpcClient.Transaction> listPaymentsReceived(WalletInput walletInput) {
        if (ledger.isValidPublicKey(walletInput.raw())) {
            return openedWallets.listReceivedByAddress(walletInput.wallet());
        } else {
            return openedWallets.listTransactions(walletInput.raw(), 9999);
        }
    }

    private com.radynamics.dallipay.cryptoledger.Transaction toTransaction(BitcoindRpcClient.Transaction t, Wallet receivingWallet) throws DecoderException, UnsupportedEncodingException {
        // Outgoing transactions have a negative value. We handle both incoming and outgoing with positive values.
        var amt = Money.of(Math.abs(t.amount().doubleValue()), new Currency(ledger.getNativeCcySymbol()));
        var trx = new com.radynamics.dallipay.cryptoledger.generic.Transaction(ledger, amt);
        trx.setId(t.txId());
        // Null uncomfirmed tx
        if (t.blockTime() != null) {
            trx.setBooked(toUserTimeZone(t.blockTime()));
        }

        var rawTx = openedWallets.getRawTransaction(t.txId());
        trx.setSender(getSender(t, rawTx).orElse(null));
        trx.setReceiver(t.address() == null ? receivingWallet : ledger.createWallet(t.address()));

        for (var vout : rawTx.vOut()) {
            var content = openedWallets.decodeScript(vout.scriptPubKey().hex()).asm();
            final String OP_RETURN = "OP_RETURN ";
            if (content.startsWith(OP_RETURN)) {
                var payloadDataHex = content.substring(OP_RETURN.length());
                var memoText = Utils.hexToString(payloadDataHex);

                var unwrappedMemo = PayloadConverter.fromMemo(memoText);
                var messages = new ArrayList<>(Arrays.asList(unwrappedMemo.freeTexts()));

                for (var r : com.radynamics.dallipay.cryptoledger.generic.StructuredReferenceLookup.find(memoText)) {
                    trx.addStructuredReference(r);
                    messages.removeIf(o -> o.equals(r.getUnformatted()));
                }
                for (var m : messages) {
                    trx.addMessage(m);
                }
            }
        }

        return trx;
    }

    private Optional<Wallet> getSender(BitcoindRpcClient.Transaction t, BitcoindRpcClient.RawTransaction tx) {
        if (t.account() != null) {
            return Optional.of(ledger.createWallet(t.account()));
        }

        if (tx.vIn().size() == 1) {
            return getAddress(tx.vIn().get(0));
        }

        var exactAmount = new ArrayList<Wallet>();
        for (var in : tx.vIn()) {
            if (in.amount() != null && in.amount().compareTo(t.amount()) == 0) {
                var a = getAddress(in);
                if (a.isPresent()) {
                    exactAmount.add(a.orElseThrow());
                }
            }
        }
        if (exactAmount.size() == 1) {
            return Optional.of(exactAmount.get(0));
        }

        return Optional.empty();
    }

    private Optional<Wallet> getAddress(BitcoindRpcClient.RawTransaction.In in) {
        try {
            // getTransactionOutput may throw RuntimeException for mined blocks.
            return Optional.of(ledger.createWallet(in.getTransactionOutput().scriptPubKey().mapStr("address")));
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private ZonedDateTime toUserTimeZone(Date dt) {
        return DateTimeConvert.toUserTimeZone(ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.getTime()), ZoneId.of("UTC")));
    }

    public BitcoinCoreRpcSubmitter createTransactionSubmitter(PrivateKeyProvider privateKeyProvider) {
        return new BitcoinCoreRpcSubmitter(ledger, privateKeyProvider, openedWallets);
    }

    public boolean validateAddress(String publicKey) {
        return openedWallets.validateAddress(publicKey);
    }

    public void refreshBalance(Wallet wallet, boolean useCache) {
        var balance = openedWallets.getBalance(wallet);
        if (!balance.isPresent()) {
            log.info("refreshBalance failed. Unknown wallet %s".formatted(wallet.getPublicKey()));
            return;
        }
        wallet.getBalances().set(Money.of(balance.orElseThrow().doubleValue(), new Currency(ledger.getNativeCcySymbol())));
    }

    public BigDecimal estimateSmartFee(int targetInBlocks) {
        var est = openedWallets.estimateSmartFee(targetInBlocks);
        if (StringUtils.isEmpty(est.errors())) {
            return est.feeRate();
        } else {
            log.warn(est.errors());
            return BigDecimal.ZERO;
        }
    }

    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        var c = new BitcoinJSONRPCClient(networkInfo.getUrl().url());
        var info = c.getNetworkInfo();

        return EndpointInfo.builder()
                .networkInfo(networkInfo)
                .serverVersion(info.subversion());
    }

    public List<String> walletNames() {
        return openedWallets.walletNames();
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Wallet wallet) throws ApiException {
        openedWallets.importWallet(walletName, historicTransactionSince, wallet);
    }

    public void importWallet(String walletName, LocalDateTime historicTransactionSince, Device device) throws ApiException {
        openedWallets.importWallet(walletName, historicTransactionSince, device);
    }

    public boolean isValidWalletPassPhrase(Wallet wallet) {
        return openedWallets.isValidWalletPassPhrase(wallet);
    }
}
