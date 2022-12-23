package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionStateListener;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionSubmitter;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.util.ArrayList;
import java.util.function.Function;

public class InternalSubmitter implements TransactionSubmitter<ImmutablePayment.Builder> {
    private final Ledger ledger;
    private final XrplClient xrplClient;
    private final PrivateKeyProvider privateKeyProvider;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    public InternalSubmitter(Ledger ledger, XrplClient xrplClient, PrivateKeyProvider privateKeyProvider) {
        this.ledger = ledger;
        this.xrplClient = xrplClient;
        this.privateKeyProvider = privateKeyProvider;
    }

    @Override
    public void submit(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t, ImmutablePayment.Builder builder, Function<String, Void> onSuccess) throws LedgerException {
        var xrplTransaction = ((com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction) t);
        var publicKey = builder.build().account().value();
        var privateKey = privateKeyProvider.get(publicKey);
        if (privateKey == null) {
            xrplTransaction.refreshTransmission(new LedgerException("PrivateKey missing"));
            raiseFailure(t);
            return;
        }

        var signed = sign(builder, privateKey);

        SubmitResult<Transaction> prelimResult;
        try {
            prelimResult = xrplClient.submit(signed);
        } catch (JsonRpcClientErrorException | JsonProcessingException e) {
            xrplTransaction.refreshTransmission(new LedgerException(e.getMessage(), e));
            raiseFailure(t);
            return;
        }
        if (!prelimResult.result().equalsIgnoreCase("tesSUCCESS")) {
            var msg = String.format("Ledger submit failed with result %s %s", prelimResult.result(), prelimResult.engineResultMessage().get());
            xrplTransaction.refreshTransmission(new LedgerException(msg));
            raiseFailure(t);
            return;
        }

        onSuccess.apply(signed.hash().value());
        raiseSuccess(t);
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

    private void raiseSuccess(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t) {
        for (var l : stateListener) {
            l.onSuccess(t);
        }
    }

    private void raiseFailure(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t) {
        for (var l : stateListener) {
            l.onFailure(t);
        }
    }
}
