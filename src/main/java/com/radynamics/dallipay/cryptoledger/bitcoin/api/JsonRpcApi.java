package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.cryptoledger.bitcoin.signing.RpcSubmitter;
import com.radynamics.dallipay.cryptoledger.generic.WalletInput;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

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

    public TransactionResult listPaymentsReceived(WalletInput walletInput, DateTimeRange period) {
        var tr = new TransactionResult();

        try {
            var transactions = listPaymentsReceived(walletInput);
            for (var t : transactions) {
                // Skip outgoing tx and tx without an amount.
                if (t.amount().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                // Skip unconfirmed
                if (t.blockTime() == null) {
                    continue;
                }
                if (!period.isBetween(ZonedDateTime.ofInstant(t.blockTime().toInstant(), ZoneId.of("UTC")))) {
                    continue;
                }
                tr.add(toTransaction(t, walletInput.wallet()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tr;
    }

    private List<BitcoindRpcClient.Transaction> listPaymentsReceived(WalletInput walletInput) {
        if (ledger.isValidPublicKey(walletInput.raw())) {
            return openedWallets.listReceivedByAddress(walletInput.wallet());
        } else {
            return openedWallets.listTransactions(walletInput.raw(), 9999);
        }
    }

    private com.radynamics.dallipay.cryptoledger.Transaction toTransaction(BitcoindRpcClient.Transaction t, Wallet receivingWallet) throws DecoderException, UnsupportedEncodingException {
        var amt = Money.of(t.amount().doubleValue(), new Currency(ledger.getNativeCcySymbol()));
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

    public TransactionSubmitter createTransactionSubmitter(PrivateKeyProvider privateKeyProvider) {
        var signer = new RpcSubmitter(ledger, privateKeyProvider, openedWallets);
        signer.setVerifier(new OnchainVerifier(ledger));
        return signer;
    }

    public boolean validateAddress(String publicKey) {
        var result = openedWallets.validateAddress(publicKey);
        return result.isValid();
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
        return openedWallets.estimateSmartFee(targetInBlocks).feeRate();
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

    public void importWallet(Wallet wallet, LocalDateTime historicTransactionSince) throws ApiException {
        openedWallets.importWallet(wallet, historicTransactionSince);
    }

    public boolean isValidWalletPassPhrase(Wallet wallet) {
        return openedWallets.isValidWalletPassPhrase(wallet);
    }
}
