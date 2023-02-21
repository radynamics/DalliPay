package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

public class MemoryStorage implements Storage {
    private String accessToken;

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String value) {
        accessToken = value;
    }

    @Override
    public int getLocalHttpServerPort() {
        return XummPkce.defaultPort;
    }
}
