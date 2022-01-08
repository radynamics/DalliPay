package com.radynamics.CryptoIso20022Interop.exchange;

public interface Exchange {
    String getId();
    String getDisplayText();

    void load();

    ExchangeRate[] rates();
}
