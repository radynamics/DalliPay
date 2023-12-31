package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class WalletCreateFundedPsbtResult {
    private final String psbt;
    private final Double fee;
    private final int changepos;

    public WalletCreateFundedPsbtResult(String psbt, Double fee, Integer changepos) {
        this.psbt = psbt;
        this.fee = fee;
        this.changepos = changepos;
    }

    public String psbt() {
        return psbt;
    }

    public Double fee() {
        return fee;
    }

    public int changepos() {
        return changepos;
    }
}
