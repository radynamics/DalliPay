package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.FeeSuggestion;

public class FeeInfo {
    private final long baseFee;

    public FeeInfo(long baseFee) {
        this.baseFee = baseFee;
    }

    public FeeSuggestion createSuggestion() {
        return new FeeSuggestion(Ledger.dropsToXrpPlus(baseFee), Ledger.dropsToXrpPlus(Math.round(baseFee * 1.1)), Ledger.dropsToXrpPlus(Math.round(baseFee * 1.5)));
    }

    @Override
    public String toString() {
        return String.format("baseFee: %s", baseFee);
    }
}
