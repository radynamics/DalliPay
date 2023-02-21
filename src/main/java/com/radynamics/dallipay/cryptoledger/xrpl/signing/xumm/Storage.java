package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

public interface Storage {
    String getAccessToken();

    void setAccessToken(String value);

    int getLocalHttpServerPort();
}
