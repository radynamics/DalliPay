package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;

import java.time.ZonedDateTime;
import java.util.ResourceBundle;

public class ManualRateProvider implements ExchangeRateProvider {
    public static final String ID = "manual";

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return res.getString("manualInput");
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
        // do nothing
    }

    @Override
    public ExchangeRate[] latestRates() {
        return new ExchangeRate[0];
    }

    @Override
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime, NetworkInfo blockNetwork, Block block) {
        return null;
    }
}
