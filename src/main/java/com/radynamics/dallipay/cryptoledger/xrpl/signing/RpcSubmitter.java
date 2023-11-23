package com.radynamics.dallipay.cryptoledger.xrpl.signing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionStateListener;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.api.Convert;
import com.radynamics.dallipay.cryptoledger.xrpl.api.PaymentBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.ImmutableTransactionRequestParams;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RpcSubmitter implements TransactionSubmitter {
    private final static Logger log = LogManager.getLogger(RpcSubmitter.class);
    private final Ledger ledger;
    private final PrivateKeyProvider privateKeyProvider;
    private OnchainVerifier verifier;
    private final TransactionSubmitterInfo info;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public final static String Id = "rpcSubmitter";

    public RpcSubmitter(Ledger ledger, PrivateKeyProvider privateKeyProvider) {
        this.ledger = ledger;
        this.privateKeyProvider = privateKeyProvider;

        info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("rpc.title"));
        info.setDescription(res.getString("rpc.desc"));
        info.setNotRecommended(true);
    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public void submit(com.radynamics.dallipay.cryptoledger.Transaction[] transactions) {
        var xrplClient = new XrplClient(ledger.getNetwork().getUrl());
        var sendingWallets = PaymentUtils.distinctSendingWallets(transactions);
        // Process by sending wallet to keep sequence number handling simple (prevent terPRE_SEQ).
        for (var sendingWallet : sendingWallets) {
            var trxByWallet = PaymentUtils.fromSender(sendingWallet, transactions);
            var sequences = new ImmutablePair<>(UnsignedInteger.ZERO, UnsignedInteger.ZERO);
            for (var trx : trxByWallet) {
                var t = (Transaction) trx;
                try {
                    sequences = submit(xrplClient, t, sequences);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    t.refreshTransmission(e);
                    raiseFailure(t);
                }
            }
        }
    }

    private ImmutablePair<UnsignedInteger, UnsignedInteger> submit(XrplClient xrplClient, Transaction t, ImmutablePair<UnsignedInteger, UnsignedInteger> sequences) throws Exception {
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

        var transactionHash = submit(xrplClient, builder);
        if (transactionHash != null) {
            t.setId(transactionHash);
            t.setBooked(ZonedDateTime.now());

            if (verifier.verify(transactionHash, t)) {
                t.setBlock(Convert.toLedgerBlock(verifier.getOnchainTransaction().getBlock()));
                t.refreshTransmission();
                raiseSuccess(t);
            } else {
                t.refreshTransmission(new OnchainVerificationException(res.getString("verifyFailed")));
                raiseFailure(t);
            }
        }

        return new ImmutablePair<>(previousLastLedgerSequence, accountSequenceOffset);
    }

    private String submit(XrplClient xrplClient, ImmutablePayment.Builder builder) throws LedgerException, JsonRpcClientErrorException, JsonProcessingException {
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

        var transactionHash = signed.hash().value();

        var interval = Duration.ofMillis(500);
        wait(interval);

        final Duration timeout = Duration.ofSeconds(10);
        var remaining = timeout;
        while (!remaining.isNegative()) {
            if (isValidated(xrplClient, transactionHash)) {
                return transactionHash;
            }

            wait(interval);
            remaining = remaining.minus(interval);
        }

        throw new LedgerException("Transaction was submitted but was not validated within %s seconds.".formatted(timeout.toSeconds()));
    }

    private boolean isValidated(XrplClient xrplClient, String transactionHash) {
        var params = ImmutableTransactionRequestParams.builder()
                .transaction(Hash256.of(transactionHash));
        try {
            var result = xrplClient.transaction(params.build(), org.xrpl.xrpl4j.model.transactions.Transaction.class);
            return result.validated();
        } catch (JsonRpcClientErrorException e) {
            log.trace(e.getMessage(), e);
            return false;
        }
    }

    private void wait(Duration sleep) {
        try {
            Thread.sleep(sleep.toMillis());
        } catch (InterruptedException e) {
            // ignore
        }
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
    public TransactionSubmitterInfo getInfo() {
        return info;
    }

    public void setVerifier(OnchainVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    @Override
    public boolean supportIssuedTokens() {
        return true;
    }

    @Override
    public boolean supportsPathFinding() {
        return false;
    }

    @Override
    public void deleteSettings() {
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
