package com.radynamics.CryptoIso20022Interop.exchange;

import org.apache.commons.lang3.NotImplementedException;

public final class ExchangeFactory {
    public static final Exchange create(String id) {
        switch (id.toLowerCase()) {
            case "demo":
                return new DemoExchange();
            default:
                throw new NotImplementedException(String.format("Exchange %s unknown.", id));
        }
    }
}
