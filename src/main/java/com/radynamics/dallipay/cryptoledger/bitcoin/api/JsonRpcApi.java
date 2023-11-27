package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.cryptoledger.bitcoin.signing.RpcSubmitter;
import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class JsonRpcApi {
    final static Logger log = LogManager.getLogger(JsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;
    private final BitcoindRpcClient client;

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
        this.client = new BitcoinJSONRPCClient(network.getUrl().url());
    }

    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) {
        var tr = new TransactionResult();

        try {
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

    private com.radynamics.dallipay.cryptoledger.Transaction toTransaction(BitcoindRpcClient.Transaction t) throws DecoderException, UnsupportedEncodingException {
        var amt = Money.of(t.amount().doubleValue(), new Currency(ledger.getNativeCcySymbol()));
        var trx = new com.radynamics.dallipay.cryptoledger.generic.Transaction(ledger, amt);
        trx.setId(t.txId());
        // Null uncomfirmed tx
        if (t.blockTime() != null) {
            trx.setBooked(toUserTimeZone(t.blockTime()));
        }
        if (t.account() != null) {
            trx.setSender(ledger.createWallet(t.account()));
        }
        if (t.address() != null) {
            trx.setReceiver(ledger.createWallet(t.address()));
        }

        var rawTx = client.getRawTransaction(t.txId());
        for (var vout : rawTx.vOut()) {
            var content = client.decodeScript(vout.scriptPubKey().hex()).asm();
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

    private ZonedDateTime toUserTimeZone(Date dt) {
        return DateTimeConvert.toUserTimeZone(ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.getTime()), ZoneId.of("UTC")));
    }

    public TransactionSubmitter createTransactionSubmitter(PrivateKeyProvider privateKeyProvider) {
        var signer = new RpcSubmitter(ledger, privateKeyProvider);
        signer.setVerifier(new OnchainVerifier(ledger));
        return signer;
    }

    public boolean validateAddress(String publicKey) {
        var result = client.validateAddress(publicKey);
        return result.isValid();
    }

    public void refreshBalance(Wallet wallet, boolean useCache) {
        // TODO: Verify wallet matches
        var balance = client.getBalance();
        wallet.getBalances().set(Money.of(balance.doubleValue(), new Currency(ledger.getNativeCcySymbol())));
    }

    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        var c = new BitcoinJSONRPCClient(networkInfo.getUrl().url());
        var info = c.getNetworkInfo();

        return EndpointInfo.builder()
                .networkInfo(networkInfo)
                .serverVersion(info.subversion());
    }
}
