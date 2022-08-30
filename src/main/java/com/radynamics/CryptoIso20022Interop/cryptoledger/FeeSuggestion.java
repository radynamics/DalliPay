package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;

public class FeeSuggestion {
    private Money low;
    private Money medium;
    private Money high;

    public FeeSuggestion(Money low, Money medium, Money high) {
        this.low = low;
        this.medium = medium;
        this.high = high;
    }

    public static FeeSuggestion None(String ccy) {
        var zero = Money.of(0, new Currency(ccy));
        return new FeeSuggestion(zero, zero, zero);
    }

    public Money getLow() {
        return low;
    }

    public Money getMedium() {
        return medium;
    }

    public Money getHigh() {
        return high;
    }

    @Override
    public String toString() {
        return String.format("{low=%s, medium=%s, high=%s}", low, medium, high);
    }
}
