package com.radynamics.CryptoIso20022Interop.exchange;

public interface Exchange {
    String getId();

    void load();

    ExchangeRate[] rates();
}
