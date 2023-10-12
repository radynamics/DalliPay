package com.radynamics.dallipay.cryptoledger;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.time.ZonedDateTime;

public class NetworkInfo {
    private HttpUrl url;
    private URI webSocketUri;
    private String networkId;
    private String displayName;
    private boolean isPredefined;

    private static final String liveId = "livenet";
    private static final String testnetId = "testnet";

    public static NetworkInfo create(HttpUrl url, String displayName) {
        return create(url, displayName, null);
    }

    public static NetworkInfo createLivenet(HttpUrl url, String displayName) {
        var ni = create(url, displayName, liveId);
        ni.isPredefined = true;
        return ni;
    }

    public static NetworkInfo createTestnet(HttpUrl url, String displayName) {
        var ni = create(url, displayName, testnetId);
        ni.isPredefined = true;
        return ni;
    }

    public static NetworkInfo create(HttpUrl url, String displayName, String networkId) {
        if (url == null) throw new IllegalArgumentException("Parameter 'url' cannot be null");
        if (displayName == null) throw new IllegalArgumentException("Parameter 'displayName' cannot be null");
        var o = new NetworkInfo();
        o.networkId = networkId;
        o.url = url;
        o.displayName = displayName;
        return o;
    }

    public static String createDisplayName(HttpUrl url) {
        return url.host();
    }

    public String getShortText() {
        return getDisplayName();
    }

    public HttpUrl getUrl() {
        return url;
    }

    public String getId() {
        return networkId;
    }

    public void setWebSocketUri(URI uri) {
        this.webSocketUri = uri;
    }

    public URI getWebSocketUri() {
        return this.webSocketUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPredefined() {
        return isPredefined;
    }

    public boolean matches(String text) {
        if (isLivenet() && "main".equalsIgnoreCase(text)) {
            return true;
        }
        if (isTestnet() && "testnet".equalsIgnoreCase(text)) {
            return true;
        }
        return false;
    }

    public ZonedDateTime historyAvailableSince() {
        return ZonedDateTime.now().minusDays(40);
    }

    public boolean isLivenet() {
        return liveId.equals(networkId);
    }

    public boolean isTestnet() {
        return testnetId.equals(networkId);
    }

    public boolean sameNet(NetworkInfo network) {
        if (network == null) throw new IllegalArgumentException("Parameter 'network' cannot be null");
        if (url.equals(network.getUrl())) return true;
        if (StringUtils.equals(networkId, network.networkId)) return true;
        return false;
    }

    public boolean sameAs(NetworkInfo network) {
        if (network == null) return false;
        return url.equals(network.url)
                && StringUtils.equals(networkId, network.networkId)
                && StringUtils.equals(displayName, network.displayName)
                && isPredefined == network.isPredefined;
    }
}
