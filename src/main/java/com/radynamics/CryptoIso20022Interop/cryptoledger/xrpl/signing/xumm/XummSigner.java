package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class XummSigner {
    private final static Logger log = LogManager.getLogger(XummSigner.class);

    private final XummSigningObserver<JSONObject> observer = new XummSigningObserver<>();
    private final Storage storage = new MemoryStorage();
    private final String apiKey;

    public XummSigner(String apiKey) {
        if (apiKey == null) throw new IllegalArgumentException("Parameter 'apiKey' cannot be null");
        this.apiKey = apiKey;
    }

    public void submit(JSONObject json) {
        if (json == null) throw new IllegalArgumentException("Parameter 'json' cannot be null");

        var auth = new CompletableFuture<Void>();
        if (storage.getAccessToken() == null) {
            auth = authenticate();
        } else {
            auth.complete(null);
        }

        auth
                .thenRun(() -> submitAndObserve(json))
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
                    return null;
                });
    }

    private CompletableFuture<Void> authenticate() {

        return XummPkce.authenticateAsync(apiKey, "CryptoIso20022Interop")
                .thenAccept(storage::setAccessToken)
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    return null;
                });
    }

    private void submitAndObserve(JSONObject json) {
        try {
            var api = new XummApi(storage.getAccessToken());
            api.addListener(() -> {
                log.info("Xumm accessToken expired.");
                // Re-authenticate if used accessToken expired.
                authenticate()
                        .thenRun(() -> submitAndObserve(json));
            });

            var sendResponse = api.submit(json);

            observer.observe(json, URI.create(sendResponse.getJSONObject("refs").getString("websocket_status")));
        } catch (IOException | InterruptedException | XummException e) {
            throw new RuntimeException(e);
        }
    }

    public void addStateListener(StateListener<JSONObject> l) {
        observer.addStateListener(l);
    }
}
