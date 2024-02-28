package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerException;
import com.radynamics.dallipay.cryptoledger.OnchainVerificationException;
import com.radynamics.dallipay.cryptoledger.OnchainVerifier;
import com.radynamics.dallipay.cryptoledger.generic.Transaction;
import com.radynamics.dallipay.cryptoledger.signing.*;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.cryptoledger.xrpl.api.Convert;
import com.radynamics.dallipay.cryptoledger.xrpl.api.PaymentBuilder;
import com.radynamics.dallipay.ui.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
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

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public XummSigner(Ledger ledger, String apiKey) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (apiKey == null) throw new IllegalArgumentException("Parameter 'apiKey' cannot be null");
        this.ledger = ledger;
        this.apiKey = apiKey;
        this.observer.addStateListener(this);

        info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("xumm.title"));
        info.setDescription(res.getString("xumm.desc"));
        info.setDetailUri(URI.create("https://xumm.app"));
        info.setOrder(100);
        info.setIcon(Utils.getScaled("img/xumm.png", 64, 64));
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
                var ccy = t.getAmount().getCcy();
                var sendNativeCcy = t.getLedger().getNativeCcySymbol().equals(ccy.getCode());
                var senderAndReceiverHoldCurrency = t.getSenderWallet().getBalances().get(ccy).isPresent()
                        && t.getReceiverWallet().getBalances().get(ccy).isPresent();
                // Direct IOU payments don't work in Xaman if pathfinding is set to true ("No payment options found").
                var pathfinding = !sendNativeCcy && !senderAndReceiverHoldCurrency;

                var builder = PaymentBuilder.builder().payment(t).build();
                submit(t, PayloadConverter.toJson(builder.build(), t.getLedger().getNetwork()), pathfinding);
            } catch (LedgerException e) {
                t.refreshTransmission(e);
                raiseFailure(t);
            }
        }
    }

    @Override
    public void onExpired(Transaction t) {
        t.refreshTransmission(new XummException(res.getString("xumm.expired")));
        raiseFailure(t);
    }

    @Override
    public void onAccepted(Transaction t, String txid) {
        t.setId(txid);
        t.setBooked(ZonedDateTime.now());

        if (verifier.verify(txid, t)) {
            t.setBlock(Convert.toLedgerBlock(verifier.getOnchainTransaction().getBlock()));
            t.refreshTransmission();
            raiseSuccess(t);
        } else {
            t.refreshTransmission(new OnchainVerificationException(res.getString("verifyFailed")));
            raiseFailure(t);
        }
    }

    @Override
    public void onRejected(Transaction t) {
        t.refreshTransmission(new XummException(res.getString("xumm.rejected")));
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

    private void submit(Transaction t, JSONObject json, boolean pathfinding) {
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
                .thenRunAsync(() -> submitAndObserve(t, json, pathfinding))
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

    private void submitAndObserve(Transaction t, JSONObject json, boolean pathfinding) {
        try {
            api.setAccessToken(storage.getAccessToken());
            api.addListener(() -> {
                log.info("Xumm accessToken expired.");
                // Re-authenticate if used accessToken expired.
                authenticate(t)
                        .thenRunAsync(() -> submitAndObserve(t, json, pathfinding));
            });

            var sendResponse = api.submit(json, pathfinding);
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

    @Override
    public boolean supportIssuedTokens() {
        return true;
    }

    @Override
    public boolean supportsPathFinding() {
        return true;
    }

    @Override
    public boolean supportsPayload() {
        return true;
    }

    @Override
    public void deleteSettings() {
        storage.setAccessToken(null);
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
