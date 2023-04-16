package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplPriceOracle;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

public final class ExchangeRateProviderFactory {
    public static final ExchangeRateProvider create(String id, Ledger ledger) {
        return create(id, ledger, null);
    }

    public static final ExchangeRateProvider create(String id, Ledger ledger, NetworkInfo network) {
        switch (id.toLowerCase()) {
            case DemoExchange.ID:
                return new DemoExchange();
            case Bitstamp.ID:
                return new Bitstamp(ledger);
            case Coinbase.ID:
                return new Coinbase(ledger);
            case XrplPriceOracle.ID:
                return new XrplPriceOracle(network);
            default:
                throw new NotImplementedException(String.format("Exchange %s unknown.", id));
        }
    }

    public static ExchangeRateProvider[] allExchanges(Ledger ledger) {
        var list = new ArrayList<ExchangeRateProvider>();
        list.add(create(Bitstamp.ID, ledger));
        list.add(create(Coinbase.ID, ledger));
        return list.toArray(new ExchangeRateProvider[0]);
    }
}
