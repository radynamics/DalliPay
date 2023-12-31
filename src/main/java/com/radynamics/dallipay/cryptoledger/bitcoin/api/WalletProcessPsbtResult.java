package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class WalletProcessPsbtResult {
    private final String psbt;
    private final boolean complete;

    public WalletProcessPsbtResult(String psbt, boolean complete) {
        this.psbt = psbt;
        this.complete = complete;
    }

    public String psbt() {
        return psbt;
    }

    public boolean complete() {
        return complete;
    }
}
