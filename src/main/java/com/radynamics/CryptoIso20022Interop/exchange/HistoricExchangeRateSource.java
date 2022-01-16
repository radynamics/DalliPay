package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.LocalDateTime;

public interface HistoricExchangeRateSource {
    String getId();

    String getDisplayText();

    CurrencyPair[] getSupportedPairs();

    ExchangeRate rateAt(CurrencyPair pair, LocalDateTime pointInTime);
}
