package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.PrivateKeyProvider;
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

public class InternalSubmitter implements TransactionSubmitter<ImmutablePayment.Builder> {
    private final Ledger ledger;
    private final XrplClient xrplClient;
    private final PrivateKeyProvider privateKeyProvider;

    public InternalSubmitter(Ledger ledger, XrplClient xrplClient, PrivateKeyProvider privateKeyProvider) {
        this.ledger = ledger;
        this.xrplClient = xrplClient;
        this.privateKeyProvider = privateKeyProvider;
    }

    @Override
    public String submit(ImmutablePayment.Builder builder) throws LedgerException {
        var publicKey = builder.build().account().value();
        var privateKey = privateKeyProvider.get(publicKey);
        if (privateKey == null) {
            return null;
        }

        var signed = sign(builder, privateKey);

        SubmitResult<Transaction> prelimResult;
        try {
            prelimResult = xrplClient.submit(signed);
        } catch (JsonRpcClientErrorException | JsonProcessingException e) {
            throw new LedgerException(e.getMessage(), e);
        }
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
}
