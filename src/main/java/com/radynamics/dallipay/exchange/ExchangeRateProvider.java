package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Block;

public interface ExchangeRateProvider {
    String getId();

    String getDisplayText();

    CurrencyPair[] getSupportedPairs();

    boolean supportsRateAt();

    void init();

    void load();

    ExchangeRate[] latestRates();

    ExchangeRate rateAt(CurrencyPair pair, Block block);
}
