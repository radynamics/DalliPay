package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class FinalizePsbtResult {
    private final String hex;
    private final boolean complete;

    public FinalizePsbtResult(String hex, boolean complete) {
        this.hex = hex;
        this.complete = complete;
    }

    public String hex() {
        return hex;
    }

    public boolean complete() {
        return complete;
    }
}
