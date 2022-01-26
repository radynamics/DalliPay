package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.LocalDateTime;

public interface ExchangeRateProvider {
    String getId();

    String getDisplayText();

    CurrencyPair[] getSupportedPairs();

    boolean supportsRateAt();

    void load();

    ExchangeRate[] rates();

    ExchangeRate rateAt(CurrencyPair pair, LocalDateTime pointInTime);
}
