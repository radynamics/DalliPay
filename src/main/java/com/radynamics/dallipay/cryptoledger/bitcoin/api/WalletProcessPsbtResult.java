package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class WalletProcessPsbtResult {
    private final String psbt;
    private final boolean signed;

    public WalletProcessPsbtResult(String psbt, boolean complete) {
        this.psbt = psbt;
        this.signed = complete;
    }

    public String psbt() {
        return psbt;
    }

    public boolean signed() {
        return signed;
    }
}
