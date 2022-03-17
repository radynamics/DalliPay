package com.radynamics.CryptoIso20022Interop.update;

import java.net.URI;

public class UpdateInfo {
    private final String version;
    private final URI uri;

    public UpdateInfo(String version, URI url) {
        this.version = version;
        this.uri = url;
    }

    public String getVersion() {
        return version;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return this.version;
    }
}
