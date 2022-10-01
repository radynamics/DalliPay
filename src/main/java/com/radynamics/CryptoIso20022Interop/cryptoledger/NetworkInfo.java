package com.radynamics.CryptoIso20022Interop.cryptoledger;

import okhttp3.HttpUrl;

import java.time.ZonedDateTime;
import java.util.Locale;

public class NetworkInfo {
    private HttpUrl url;
    private String networkId;

    public static final String liveId = "livenet";
    public static final String testnetId = "testnet";

    public static NetworkInfo create(HttpUrl url) {
        return create(url, null);
    }

    public static NetworkInfo create(HttpUrl url, String networkId) {
        var o = new NetworkInfo();
        o.networkId = networkId;
        o.url = url;
        return o;
    }

    public String getShortText() {
        return getId().toUpperCase(Locale.ROOT);
    }

    public HttpUrl getUrl() {
        return url;
    }

    public String getId() {
        return networkId;
    }

    public boolean matches(String text) {
        if (liveId.equals(networkId) && "main".equalsIgnoreCase(text)) {
            return true;
        }
        if (testnetId.equals(networkId) && "test".equalsIgnoreCase(text)) {
            return true;
        }
        return false;
    }

    public ZonedDateTime historyAvailableSince() {
        return liveId.equals(networkId) ? ZonedDateTime.now().minusDays(40) : ZonedDateTime.now().minusDays(5);
    }
}
