package com.radynamics.CryptoIso20022Interop.exchange;

public class DemoExchange implements Exchange {
    private ExchangeRate[] exchangeRates;

    public static final String ID = "demo";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void load() {
        exchangeRates = new ExchangeRate[3];
        exchangeRates[0] = new ExchangeRate("XRP", "USD", 0.843332);
        exchangeRates[1] = new ExchangeRate("XRP", "EUR", 0.69946);
        exchangeRates[2] = new ExchangeRate("XRP", "CHF", 0.78825);
    }

    @Override
    public ExchangeRate[] rates() {
        return exchangeRates;
    }
}
