package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracle;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

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
            case XrplPriceOracle.ID:
                return new XrplPriceOracle(network);
            default:
                throw new NotImplementedException(String.format("Exchange %s unknown.", id));
        }
    }

    public static ExchangeRateProvider[] allExchanges() {
        var list = new ArrayList<ExchangeRateProvider>();
        list.add(create(Bitstamp.ID));
        list.add(create(Coinbase.ID));
        return list.toArray(new ExchangeRateProvider[0]);
    }
}
