package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

public interface StateListener<T> {
    void onExpired(T payload);

    void onAccepted(T payload, String txid);

    void onRejected(T payload);

    void onException(T payload, Exception e);
}
