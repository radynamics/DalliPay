package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerException;
import com.radynamics.dallipay.cryptoledger.OnchainVerifier;
import com.radynamics.dallipay.cryptoledger.signing.*;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.cryptoledger.xrpl.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.api.PaymentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class XummSigner implements TransactionSubmitter, StateListener<Transaction> {
    private final static Logger log = LogManager.getLogger(XummSigner.class);

    private final XummApi api = new XummApi();
    private final PollingObserver<Transaction> observer = new PollingObserver<>(api);
    private final TransactionSubmitterInfo info;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();
    private final Ledger ledger;
    private Storage storage = new MemoryStorage();
    private final String apiKey;
    private OnchainVerifier verifier;
    private CompletableFuture<Void> authentication;

    public final static String Id = "xummSigner";

    public XummSigner(Ledger ledger, String apiKey) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (apiKey == null) throw new IllegalArgumentException("Parameter 'apiKey' cannot be null");
        this.ledger = ledger;
        this.apiKey = apiKey;
        this.observer.addStateListener(this);

        info = new TransactionSubmitterInfo();
        info.setTitle("Xumm");
        info.setDescription("Use Xumm App on your smartphone to sign and send payments. All payments will need your confirmation in Xumm. Your private key and secrets are never exposed to this software.");
        info.setDetailUri(URI.create("https://xumm.app"));
        info.setRecommended(true);
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
        for (var trx : transactions) {
            var t = (Transaction) trx;
            try {
                var builder = PaymentBuilder.builder().payment(t).build();
                submit(t, PayloadConverter.toJson(builder.build()));
            } catch (LedgerException e) {
                t.refreshTransmission(e);
                raiseFailure(t);
            }
        }
    }

    @Override
    public void onExpired(Transaction t) {
        t.refreshTransmission(new XummException("Confirmation request expired"));
        raiseFailure(t);
    }

    @Override
    public void onAccepted(Transaction t, String txid) {
        t.setId(txid);
        t.setBooked(ZonedDateTime.now());

        if (verifier != null && !verifier.verify(txid, t)) {
            t.refreshTransmission(new XummException("Transaction was submitted but result on chain doesn't match sent payment. This may indicates a serious software issue or fraud."));
            raiseFailure(t);
            return;
        }

        t.refreshTransmission();
        raiseSuccess(t);
    }

    @Override
    public void onRejected(Transaction t) {
        t.refreshTransmission(new XummException("Payment confirmation rejected"));
        raiseFailure(t);
    }

    @Override
    public void onException(Transaction t, Exception e) {
        t.refreshTransmission(e);
        raiseFailure(t);
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return new EmptyPrivateKeyProvider();
    }

    @Override
    public TransactionSubmitterInfo getInfo() {
        return info;
    }

    private void submit(Transaction t, JSONObject json) {
        if (json == null) throw new IllegalArgumentException("Parameter 'json' cannot be null");

        var auth = new CompletableFuture<Void>();
        if (storage.getAccessToken() == null) {
            auth = authenticate(t);
        } else {
            var payload = JwtPayload.create(storage.getAccessToken());
            if (payload != null && payload.expired()) {
                auth = authenticate(t);
            } else {
                auth.complete(null);
            }
        }

        auth
                .thenRunAsync(() -> submitAndObserve(t, json))
                .thenRun(() -> {
                    try {
                        // Wait before stop observing
                        while (observer.countListening() > 0) {
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                })
                .whenComplete((result, throwable) -> observer.shutdown())
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    t.refreshTransmission(e);
                    raiseFailure(t);
                    return null;
                });
    }

    private synchronized CompletableFuture<Void> authenticate(Transaction t) {
        if (authentication != null) {
            return authentication;
        }

        authentication = XummPkce.authenticateAsync(apiKey, "DalliPay", storage.getLocalHttpServerPort())
                .thenAccept(storage::setAccessToken)
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    t.refreshTransmission(e);
                    raiseFailure(t);
                    return null;
                })
                .whenComplete((unused, throwable) -> authentication = null);
        return authentication;
    }

    private void submitAndObserve(Transaction t, JSONObject json) {
        try {
            api.setAccessToken(storage.getAccessToken());
            api.addListener(() -> {
                log.info("Xumm accessToken expired.");
                // Re-authenticate if used accessToken expired.
                authenticate(t)
                        .thenRunAsync(() -> submitAndObserve(t, json));
            });

            var sendResponse = api.submit(json);
            if (sendResponse == null) {
                return;
            }

            t.refreshTransmissionState(TransmissionState.Waiting);
            raiseProgressChanged(t);
            observer.observe(t, UUID.fromString(sendResponse.getString("uuid")));
        } catch (IOException | InterruptedException | XummException e) {
            throw new RuntimeException(e);
        }
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public OnchainVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(OnchainVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    private void raiseProgressChanged(Transaction t) {
        for (var l : stateListener) {
            l.onProgressChanged(t);
        }
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
