package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.ZonedDateTime;

public class DemoExchange implements ExchangeRateProvider {
    private ExchangeRate[] exchangeRates;

    public static final String ID = "demo";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "Demo Exchange (fixed rates)";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        return new CurrencyPair[0];
    }

    @Override
    public boolean supportsRateAt() {
        return false;
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public void load() {
        exchangeRates = new ExchangeRate[3];
        exchangeRates[0] = new ExchangeRate("XRP", "USD", 0.843332, ZonedDateTime.now());
        exchangeRates[1] = new ExchangeRate("XRP", "EUR", 0.69946, ZonedDateTime.now());
        exchangeRates[2] = new ExchangeRate("XRP", "CHF", 0.78825, ZonedDateTime.now());
    }

    @Override
    public ExchangeRate[] latestRates() {
        return exchangeRates;
    }

    @Override
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime) {
        return null;
    }
}
