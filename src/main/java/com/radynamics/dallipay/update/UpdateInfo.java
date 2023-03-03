package com.radynamics.dallipay.update;

import java.net.URI;

public class UpdateInfo {
    private final String version;
    private final URI uri;
    private final boolean mandatory;

    public UpdateInfo(String version, URI url, boolean mandatory) {
        this.version = version;
        this.uri = url;
        this.mandatory = mandatory;
    }

    public String getVersion() {
        return version;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public String toString() {
        return this.version;
    }
}
