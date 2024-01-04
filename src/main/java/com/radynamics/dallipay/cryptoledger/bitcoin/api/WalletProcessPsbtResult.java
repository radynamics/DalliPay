package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class WalletProcessPsbtResult {
    private final String psbt;
    private final boolean signed;
    private final boolean cancelled;

    public WalletProcessPsbtResult(String psbt, boolean complete, boolean cancelled) {
        this.psbt = psbt;
        this.signed = complete;
        this.cancelled = cancelled;
    }

    public String psbt() {
        return psbt;
    }

    public boolean signed() {
        return signed;
    }

    public boolean cancelled() {
        return cancelled;
    }
}
