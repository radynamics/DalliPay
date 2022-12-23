package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PollingObserver<T> {
    private final static Logger log = LogManager.getLogger(PollingObserver.class);

    private final ArrayList<Observed> elements = new ArrayList<>();
    private final XummApi api;
    private Duration timeout = Duration.ofMinutes(10);
    private final ArrayList<StateListener<T>> stateListener = new ArrayList<>();

    public PollingObserver(XummApi api) {
        this.api = api;
    }

    public void observe(T payload, UUID payloadId) {
        var o = new Observed(payload, payloadId);
        elements.add(o);

        startPolling(o);
        startTimeoutTask(o);
    }

    private void startPolling(Observed o) {
        var task = new TimerTask() {
            public void run() {
                try {
                    processResponse(o, api.status(o.payloadId));
                } catch (IOException | InterruptedException | XummException e) {
                    raiseException(o.payload, e);
                }
            }
        };

        // Start polling after initial delay because we assume user needs at least that time to sign a transaction on smartphone.
        o.pollingTimer.scheduleAtFixedRate(task, 1000, 1000);
    }

    private void processResponse(Observed o, JSONObject json) {
        log.trace(json);

        // Handling according to https://xumm.readme.io/reference/get-payload
        var meta = json.getJSONObject("meta");
        var resolved = meta.getBoolean("resolved");
        var signed = meta.getBoolean("signed");
        var expired = meta.getBoolean("expired");

        if (resolved && !signed) {
            raiseRejected(o.payload);
            stopObserving(o);
            return;
        }

        if (expired) {
            raiseExpired(o.payload);
            stopObserving(o);
            return;
        }

        var response = json.optJSONObject("response");
        // Not yet signed, waiting for response
        if (response == null || response.isNull("dispatched_result")) {
            return;
        }

        var dispatchedResult = response.getString("dispatched_result");
        var success = dispatchedResult.equals("tesSUCCESS");

        if (!success) {
            raiseRejected(o.payload);
            stopObserving(o);
            return;
        }

        var txid = response.getString("txid");

        if (resolved && signed) {
            raiseAccepted(o.payload, txid);
            stopObserving(o);
            return;
        }
    }

    private void startTimeoutTask(Observed o) {
        var timeoutTask = new TimerTask() {
            public void run() {
                if (!elements.contains(o)) {
                    return;
                }

                stopObserving(o);
                raiseExpired(o.payload);
            }
        };
        o.timeoutTimer.schedule(timeoutTask, timeout.toMillis());
    }

    private void stopObserving(Observed o) {
        log.info("Stop observing " + o.payloadId);
        o.pollingTimer.cancel();
        o.timeoutTimer.cancel();
        elements.remove(o);
    }

    public int countListening() {
        return elements.size();
    }

    public void shutdown() {
        for (var i = elements.size() - 1; i >= 0; i--) {
            stopObserving(elements.get(i));
        }
    }

    public void addStateListener(StateListener<T> l) {
        stateListener.add(l);
    }

    private void raiseExpired(T payload) {
        for (var l : stateListener) {
            l.onExpired(payload);
        }
    }

    private void raiseAccepted(T payload, String txId) {
        for (var l : stateListener) {
            l.onAccepted(payload, txId);
        }
    }

    private void raiseRejected(T payload) {
        for (var l : stateListener) {
            l.onRejected(payload);
        }
    }

    private void raiseException(T payload, Exception e) {
        for (var l : stateListener) {
            l.onException(payload, e);
        }
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    private class Observed {
        public T payload;
        public UUID payloadId;
        public Timer timeoutTimer = new Timer();
        public Timer pollingTimer = new Timer();

        public Observed(T payload, UUID payloadId) {
            this.payload = payload;
            this.payloadId = payloadId;
        }
    }
}
