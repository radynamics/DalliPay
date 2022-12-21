package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class XummSigningObserver<T> {
    private final ArrayList<Observed> elements = new ArrayList<>();
    private Duration timeout = Duration.ofMinutes(10);
    private final ArrayList<StateListener<T>> stateListener = new ArrayList<>();

    public void observe(T payload, URI ws) {
        var o = new Observed(payload);
        o.client = createWebSocketClient(ws, o);
        elements.add(o);

        o.client.connect();
    }

    private WebSocketClient createWebSocketClient(URI uri, Observed o) {
        return new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                startTimeoutTask(o);
            }

            @Override
            public void onMessage(String message) {
                var json = new JSONObject(message);
                if (!json.isNull("opened")) {
                    raiseOpened(o.payload);
                    return;
                }

                if (!json.isNull("expired")) {
                    stopObserving(o);
                    raiseExpired(o.payload);
                    return;
                }

                if (!json.isNull("signed")) {
                    stopObserving(o);
                    if (json.getBoolean("signed")) {
                        raiseAccepted(o.payload, json.getString("txid"));
                    } else {
                        raiseRejected(o.payload);
                    }
                    return;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                elements.remove(o);
                raiseConnectionClosed(o.payload, code, reason);
            }

            @Override
            public void onError(Exception e) {
                elements.remove(o);
                raiseException(o.payload, e);
            }
        };
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
        o.timeoutTimer = new Timer();
        o.timeoutTimer.schedule(timeoutTask, timeout.toMillis());
    }

    private void stopObserving(Observed o) {
        o.timeoutTimer.cancel();
        o.client.close();
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

    private void raiseOpened(T payload) {
        for (var l : stateListener) {
            l.onOpened(payload);
        }
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

    private void raiseConnectionClosed(T payload, int code, String reason) {
        for (var l : stateListener) {
            l.onConnectionClosed(payload, code, reason);
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
        public WebSocketClient client;
        public T payload;
        public Timer timeoutTimer;

        public Observed(T payload) {
            this.payload = payload;
        }
    }
}
