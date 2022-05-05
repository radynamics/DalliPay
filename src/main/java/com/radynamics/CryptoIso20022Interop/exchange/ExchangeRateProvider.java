package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.ZonedDateTime;

public interface ExchangeRateProvider {
    String getId();

    String getDisplayText();

    CurrencyPair[] getSupportedPairs();

    boolean supportsRateAt();

    void init();

    void load();

    ExchangeRate[] latestRates();

    ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime);
}
