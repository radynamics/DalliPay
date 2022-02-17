package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class FeeSuggestion {
    private long low;
    private long medium;
    private long high;

    public FeeSuggestion(long low, long medium, long high) {
        this.low = low;
        this.medium = medium;
        this.high = high;
    }

    public static FeeSuggestion None() {
        return new FeeSuggestion(0, 0, 0);
    }

    public long getLow() {
        return low;
    }

    public long getMedium() {
        return medium;
    }

    public long getHigh() {
        return high;
    }

    @Override
    public String toString() {
        return String.format("{low=%s, medium=%s, high=%s}", low, medium, high);
    }
}
