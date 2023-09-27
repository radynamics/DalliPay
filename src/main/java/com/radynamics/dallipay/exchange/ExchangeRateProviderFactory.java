package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplPriceOracle;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;

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
            case ManualRateProvider.ID:
                return new ManualRateProvider();
            case XrplPriceOracle.ID:
                return new XrplPriceOracle(network);
            default:
                throw new NotImplementedException(String.format("Exchange %s unknown.", id));
        }
    }

    public static ExchangeRateProvider[] allExchanges(Ledger ledger) {
        var list = new ArrayList<ExchangeRateProvider>();
        for (var id : ledger.getExchangeRateProviders()) {
            list.add(create(id, ledger));
        }
        return list.toArray(new ExchangeRateProvider[0]);
    }

    public static boolean supports(Ledger ledger, String id) {
        return Arrays.asList(ledger.getExchangeRateProviders()).contains(id);
    }
}
