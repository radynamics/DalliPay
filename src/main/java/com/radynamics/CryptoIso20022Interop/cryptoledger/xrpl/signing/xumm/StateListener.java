package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

public interface StateListener<T> {
    void onOpened(T payload);

    void onExpired(T payload);

    void onAccepted(T payload, String txid);

    void onRejected(T payload);

    void onConnectionClosed(T payload, int code, String reason);

    void onException(T payload, Exception e);
}
