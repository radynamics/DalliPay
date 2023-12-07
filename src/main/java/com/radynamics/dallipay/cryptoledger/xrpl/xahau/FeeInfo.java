package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.FeeSuggestion;

public class FeeInfo {
    private final com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger ledger;
    private final long baseFee;

    public FeeInfo(com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger ledger, long baseFee) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        this.baseFee = baseFee;
    }

    public FeeSuggestion createSuggestion() {
        return new FeeSuggestion(ledger.dropsToXrp(baseFee), ledger.dropsToXrp(Math.round(baseFee * 1.1)), ledger.dropsToXrp(Math.round(baseFee * 1.5)));
    }

    @Override
    public String toString() {
        return String.format("baseFee: %s", baseFee);
    }
}
