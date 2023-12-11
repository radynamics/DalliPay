package com.radynamics.dallipay.cryptoledger;

public class NetworkId {
    private final String key;
    private final String displayText;

    public NetworkId(String key, String displayText) {
        this.key = key;
        this.displayText = displayText;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayText() {
        return displayText;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", key, displayText);
    }
}
