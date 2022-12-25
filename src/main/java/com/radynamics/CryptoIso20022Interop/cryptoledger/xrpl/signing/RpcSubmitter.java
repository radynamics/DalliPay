package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentUtils;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionStateListener;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api.PaymentBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class RpcSubmitter implements TransactionSubmitter<ImmutablePayment.Builder> {
    private final Ledger ledger;
    private final XrplClient xrplClient;
    private final PrivateKeyProvider privateKeyProvider;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    public final static String Id = "rpcSubmitter";

    public RpcSubmitter(Ledger ledger, XrplClient xrplClient, PrivateKeyProvider privateKeyProvider) {
        this.ledger = ledger;
        this.xrplClient = xrplClient;
        this.privateKeyProvider = privateKeyProvider;
    }

    @Override
    public void submit(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] transactions) {
        var sendingWallets = PaymentUtils.distinctSendingWallets(transactions);
        // Process by sending wallet to keep sequence number handling simple (prevent terPRE_SEQ).
        for (var sendingWallet : sendingWallets) {
            var trxByWallet = PaymentUtils.fromSender(sendingWallet, transactions);
            var sequences = new ImmutablePair<>(UnsignedInteger.ZERO, UnsignedInteger.ZERO);
            for (var trx : trxByWallet) {
                var t = (Transaction) trx;
                try {
                    sequences = submit(t, sequences);
                } catch (Exception ex) {
                    t.refreshTransmission(ex);
                    raiseFailure(t);
                }
            }
        }
    }

    private ImmutablePair<UnsignedInteger, UnsignedInteger> submit(Transaction t, ImmutablePair<UnsignedInteger, UnsignedInteger> sequences) throws Exception {
        var previousLastLedgerSequence = sequences.getLeft();
        var accountSequenceOffset = sequences.getRight();

        // Get the latest validated ledger index
        var validatedLedger = xrplClient.ledger(LedgerRequestParams.builder().ledgerIndex(LedgerIndex.VALIDATED).build())
                .ledgerIndex()
                .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

        // Workaround for https://github.com/XRPLF/xrpl4j/issues/84
        var lastLedgerSequence = UnsignedInteger.valueOf(validatedLedger.plus(UnsignedLong.valueOf(4)).unsignedLongValue().intValue());
        if (previousLastLedgerSequence == UnsignedInteger.ZERO) {
            accountSequenceOffset = UnsignedInteger.ZERO;
        } else {
            accountSequenceOffset = accountSequenceOffset.plus(UnsignedInteger.ONE);
        }
        previousLastLedgerSequence = lastLedgerSequence;

        var pb = PaymentBuilder.builder().payment(t);

        var requestParams = AccountInfoRequestParams.builder()
                .ledgerIndex(LedgerIndex.VALIDATED)
                .account(pb.getSender())
                .build();
        var accountInfoResult = xrplClient.accountInfo(requestParams);
        var sequence = accountInfoResult.accountData().sequence().plus(accountSequenceOffset);

        var builder = pb.build();
        builder.sequence(sequence);
        builder.lastLedgerSequence(lastLedgerSequence);

        var transactionHash = submit(builder);
        if (transactionHash != null) {
            t.setId(transactionHash);
            t.setBooked(ZonedDateTime.now());
            t.refreshTransmission();
            raiseSuccess(t);
        }

        return new ImmutablePair<>(previousLastLedgerSequence, accountSequenceOffset);
    }

    private String submit(ImmutablePayment.Builder builder) throws LedgerException, JsonRpcClientErrorException, JsonProcessingException {
        var publicKey = builder.build().account().value();
        var privateKey = privateKeyProvider.get(publicKey);
        if (privateKey == null) {
            throw new LedgerException("PrivateKey missing");
        }

        var signed = sign(builder, privateKey);

        var prelimResult = xrplClient.submit(signed);
        if (!prelimResult.result().equalsIgnoreCase("tesSUCCESS")) {
            throw new LedgerException(String.format("Ledger submit failed with result %s %s", prelimResult.result(), prelimResult.engineResultMessage().get()));
        }

        return signed.hash().value();
    }

    private SignedTransaction<Payment> sign(ImmutablePayment.Builder builder, String privateKeyPlain) {
        var walletFactory = DefaultWalletFactory.getInstance();
        var sender = walletFactory.fromSeed(privateKeyPlain, ledger.getNetwork().isTestnet());

        builder.signingPublicKey(sender.publicKey());

        var privateKey = PrivateKey.fromBase16EncodedPrivateKey(sender.privateKey().get());
        var signatureService = new SingleKeySignatureService(privateKey);

        var prepared = builder.build();
        return signatureService.sign(KeyMetadata.EMPTY, prepared);
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return privateKeyProvider;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    private void raiseSuccess(Transaction t) {
        for (var l : stateListener) {
            l.onSuccess(t);
        }
    }

    private void raiseFailure(Transaction t) {
        for (var l : stateListener) {
            l.onFailure(t);
        }
    }
}
