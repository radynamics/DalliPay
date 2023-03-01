package com.radynamics.dallipay.cryptoledger;

public class EndpointInfo {
    private NetworkInfo networkInfo;
    private String version;
    private String hostId;

    public static EndpointInfo builder() {
        return new EndpointInfo();
    }

    public EndpointInfo networkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
        return this;
    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public EndpointInfo serverVersion(String version) {
        this.version = version;
        return this;
    }

    public String getServerVersion() {
        return version;
    }

    public EndpointInfo hostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public String getHostId() {
        return hostId;
    }
}
