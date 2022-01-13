package com.radynamics.CryptoIso20022Interop.cryptoledger;

import okhttp3.HttpUrl;

public class NetworkInfo {
    private Network type;
    private HttpUrl url;

    public NetworkInfo(Network type, HttpUrl url) {
        this.type = type;
        this.url = url;
    }

    public Network getType() {
        return type;
    }

    public HttpUrl getUrl() {
        return url;
    }
}
