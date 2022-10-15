package com.radynamics.CryptoIso20022Interop.cryptoledger;

import okhttp3.HttpUrl;

import java.time.ZonedDateTime;
import java.util.Locale;

public class NetworkInfo {
    private HttpUrl url;
    private String networkId;

    private static final String liveId = "livenet";
    private static final String testnetId = "testnet";

    public static NetworkInfo create(HttpUrl url) {
        return create(url, null);
    }

    public static NetworkInfo createLivenet(HttpUrl url) {
        return create(url, liveId);
    }

    public static NetworkInfo createTestnet(HttpUrl url) {
        return create(url, testnetId);
    }


    public static NetworkInfo create(HttpUrl url, String networkId) {
        if (url == null) throw new IllegalArgumentException("Parameter 'url' cannot be null");
        var o = new NetworkInfo();
        o.networkId = networkId;
        o.url = url;
        return o;
    }

    public String getShortText() {
        return getId() == null ? "UNKNOWN" : getId().toUpperCase(Locale.ROOT);
    }

    public HttpUrl getUrl() {
        return url;
    }

    public String getId() {
        return networkId;
    }

    public boolean matches(String text) {
        if (isLivenet() && "main".equalsIgnoreCase(text)) {
            return true;
        }
        if (isTestnet() && "test".equalsIgnoreCase(text)) {
            return true;
        }
        return false;
    }

    public ZonedDateTime historyAvailableSince() {
        return isLivenet() ? ZonedDateTime.now().minusDays(40) : ZonedDateTime.now().minusDays(5);
    }

    public boolean isLivenet() {
        return liveId.equals(networkId);
    }

    public boolean isTestnet() {
        return testnetId.equals(networkId);
    }
}
