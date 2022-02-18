package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.FeeSuggestion;

public class FeeInfo {
    private final long minimum;
    private final long openLedger;
    private final long median;
    private final double queuePercentage;

    public FeeInfo(long minimum, long openLedger, long median, double queuePercentage) {
        this.minimum = minimum;
        this.openLedger = openLedger;
        this.median = median;
        this.queuePercentage = queuePercentage;
    }

    public FeeSuggestion createSuggestion() {
        var loadFactor = 1 + queuePercentage;

        var lowValue = Math.round(minimum * 1.1) * loadFactor;
        var low = Long.min(Math.round(lowValue), 1000);

        var mediumValue = Long.max(minimum * 10, Long.min(minimum, openLedger)) * loadFactor;
        var medium = Long.min(Math.round(mediumValue), 10000);

        var highValue = Math.round(Long.max(median, openLedger)) * loadFactor;
        var high = Long.min(Math.round(highValue), 100000);

        return new FeeSuggestion(low, medium, high);
    }

    @Override
    public String toString() {
        return String.format("{min=%s, openLed=%s, med=%s}", minimum, openLedger, median);
    }
}
