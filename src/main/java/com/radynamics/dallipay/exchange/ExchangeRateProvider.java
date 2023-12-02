package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;

import java.time.ZonedDateTime;

public interface ExchangeRateProvider {
    String getId();

    String getDisplayText();

    CurrencyPair[] getSupportedPairs();

    boolean supportsRateAt();

    void init(ConfigRepo repo);

    void load();

    ExchangeRate[] latestRates();

    ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime, NetworkInfo blockNetwork, Block block);
}
