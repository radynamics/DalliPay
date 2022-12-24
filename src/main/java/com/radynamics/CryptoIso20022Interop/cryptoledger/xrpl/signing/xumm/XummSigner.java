package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.EmptyPrivateKeyProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionStateListener;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class XummSigner implements TransactionSubmitter<ImmutablePayment.Builder> {
    private final static Logger log = LogManager.getLogger(XummSigner.class);

    private final XummApi api = new XummApi();
    private final PollingObserver<JSONObject> observer = new PollingObserver<>(api);
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();
    private Storage storage = new MemoryStorage();
    private final String apiKey;

    public final static String Id = "xummSigner";

    public XummSigner(String apiKey) {
        if (apiKey == null) throw new IllegalArgumentException("Parameter 'apiKey' cannot be null");
        this.apiKey = apiKey;
    }

    @Override
    public void submit(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t, ImmutablePayment.Builder builder, Function<String, Void> onSuccess) throws LedgerException {
        observer.addStateListener(new StateListener<>() {
            @Override
            public void onExpired(JSONObject payload) {
                ((Transaction) t).refreshTransmission(new XummException("Confirmation request expired"));
                raiseFailure(t);
            }

            @Override
            public void onAccepted(JSONObject payload, String txid) {
                onSuccess.apply(txid);
                raiseSuccess(t);
            }

            @Override
            public void onRejected(JSONObject payload) {
                ((Transaction) t).refreshTransmission(new XummException("Payment confirmation rejected"));
                raiseFailure(t);
            }

            @Override
            public void onException(JSONObject payload, Exception e) {
                ((Transaction) t).refreshTransmission(e);
                raiseFailure(t);
            }
        });
        submit(t, PayloadConverter.toJson(builder.build()));
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return new EmptyPrivateKeyProvider();
    }

    private void submit(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t, JSONObject json) {
        if (json == null) throw new IllegalArgumentException("Parameter 'json' cannot be null");

        var auth = new CompletableFuture<Void>();
        if (storage.getAccessToken() == null) {
            auth = authenticate(t);
        } else {
            auth.complete(null);
        }

        auth
                .thenRun(() -> submitAndObserve(t, json))
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
                    ((Transaction) t).refreshTransmission(e);
                    raiseFailure(t);
                    return null;
                });
    }

    private CompletableFuture<Void> authenticate(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t) {
        return XummPkce.authenticateAsync(apiKey, "CryptoIso20022Interop")
                .thenAccept(storage::setAccessToken)
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    ((Transaction) t).refreshTransmission(e);
                    raiseFailure(t);
                    return null;
                });
    }

    private void submitAndObserve(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t, JSONObject json) {
        try {
            api.setAccessToken(storage.getAccessToken());
            api.addListener(() -> {
                log.info("Xumm accessToken expired.");
                // Re-authenticate if used accessToken expired.
                authenticate(t)
                        .thenRun(() -> submitAndObserve(t, json));
            });

            var sendResponse = api.submit(json);

            observer.observe(json, UUID.fromString(sendResponse.getString("uuid")));
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
