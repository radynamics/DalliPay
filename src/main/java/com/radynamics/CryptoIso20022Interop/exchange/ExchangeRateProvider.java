package com.radynamics.CryptoIso20022Interop.exchange;

public interface ExchangeRateProvider {
    String getId();

    String getDisplayText();

    void load();

    ExchangeRate[] rates();
}
