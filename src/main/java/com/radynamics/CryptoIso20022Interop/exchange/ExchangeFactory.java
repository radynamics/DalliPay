package com.radynamics.CryptoIso20022Interop.exchange;

import org.apache.commons.lang3.NotImplementedException;

public final class ExchangeFactory {
    public static final ExchangeRateProvider create(String id) {
        switch (id.toLowerCase()) {
            case DemoExchange.ID:
                return new DemoExchange();
            case Bitstamp.ID:
                return new Bitstamp();
            default:
                throw new NotImplementedException(String.format("Exchange %s unknown.", id));
        }
    }
}
