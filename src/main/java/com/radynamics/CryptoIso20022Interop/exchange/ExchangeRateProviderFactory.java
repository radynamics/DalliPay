package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XummPriceOracle;
import org.apache.commons.lang3.NotImplementedException;

public final class ExchangeRateProviderFactory {
    public static final ExchangeRateProvider create(String id) {
        return create(id, null);
    }

    public static final ExchangeRateProvider create(String id, NetworkInfo network) {
        switch (id.toLowerCase()) {
            case DemoExchange.ID:
                return new DemoExchange();
            case Bitstamp.ID:
                return new Bitstamp();
            case Coinbase.ID:
                return new Coinbase();
            case XummPriceOracle.ID:
                return new XummPriceOracle(network);
            default:
                throw new NotImplementedException(String.format("Exchange %s unknown.", id));
        }
    }
}
