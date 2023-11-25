package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.OnchainVerifier;
import com.radynamics.dallipay.cryptoledger.TransactionResult;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.cryptoledger.bitcoin.signing.RpcSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class JsonRpcApi {
    final static Logger log = LogManager.getLogger(JsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
    }

    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) {
        var tr = new TransactionResult();

        try {
            var client = new BitcoinJSONRPCClient(network.getUrl().url());
            // PARAM must be a label instead of a publicKey
            var transactions = client.listTransactions(/*wallet.getPublicKey()*/);
            for (var t : transactions) {
                tr.add(toTransaction(t));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tr;
    }

    private com.radynamics.dallipay.cryptoledger.Transaction toTransaction(BitcoindRpcClient.Transaction t) {
        var amt = Money.of(t.amount().doubleValue(), new Currency(ledger.getNativeCcySymbol()));
        var trx = new com.radynamics.dallipay.cryptoledger.xrpl.Transaction(ledger, amt);
        trx.setId(t.txId());
        // Null uncomfirmed tx
        if (t.blockTime() != null) {
            trx.setBooked(toUserTimeZone(t.blockTime()));
        }
        if (t.account() != null) {
            trx.setSender(ledger.createWallet(t.account()));
        }
        trx.setReceiver(ledger.createWallet(t.address()));

        // TODO: read transaction data/messages
        if (t.comment() != null) {
            trx.addMessage(t.comment());
        }
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

    private ZonedDateTime toUserTimeZone(Date dt) {
        return DateTimeConvert.toUserTimeZone(ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.getTime()), ZoneId.of("UTC")));
    }

    public TransactionSubmitter createTransactionSubmitter(PrivateKeyProvider privateKeyProvider) {
        var signer = new RpcSubmitter(ledger, privateKeyProvider);
        signer.setVerifier(new OnchainVerifier(ledger));
        return signer;
    }
}
