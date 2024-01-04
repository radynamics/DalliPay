package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

public class Device {
    private final String type;
    private final String path;
    private final String model;
    private final String fingerprint;

    public Device(String type, String path, String model, String fingerprint) {
        this.type = type;
        this.path = path;
        this.model = model;
        this.fingerprint = fingerprint;
    }

    public String type() {
        return type;
    }

    public String path() {
        return path;
    }

    public String model() {
        return model;
    }

    public String fingerprint() {
        return fingerprint;
    }

    @Override
    public String toString() {
        return "%s, %s".formatted(fingerprint, model);
    }
}
