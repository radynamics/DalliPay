package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.FeeSuggestion;

public class FeeInfo {
    private final long minimum;
    private final long openLedger;
    private final long median;

    public FeeInfo(long minimum, long openLedger, long median) {
        this.minimum = minimum;
        this.openLedger = openLedger;
        this.median = median;
    }

    public FeeSuggestion createSuggestion() {
        var low = openLedger;
        var high = median;
        var medium = (low + high) / 2;
        return new FeeSuggestion(low, medium, high);
    }

    @Override
    public String toString() {
        return String.format("{min=%s, open=%s, med=%s}", minimum, openLedger, median);
    }
}
