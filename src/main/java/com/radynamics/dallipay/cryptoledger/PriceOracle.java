package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.cryptoledger.xrpl.IssuedCurrency;

import java.util.ArrayList;
import java.util.List;

public class PriceOracle {
    private final String displayText;
    private final List<IssuedCurrency> issuedCurrencies = new ArrayList<>();

    public PriceOracle(String displayText) {
        if (displayText == null) throw new IllegalArgumentException("Parameter 'displayText' cannot be null");
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public List<IssuedCurrency> getIssuedCurrencies() {
        return issuedCurrencies;
    }

    public void add(IssuedCurrency value) {
        issuedCurrencies.add(value);
    }

    @Override
    public String toString() {
        return "%s, size: ".formatted(displayText, issuedCurrencies.size());
    }
}
