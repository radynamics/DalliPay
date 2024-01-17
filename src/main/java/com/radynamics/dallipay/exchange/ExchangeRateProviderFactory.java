package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.generic.CryptoPriceOracle;
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
            case Bitrue.ID:
                return new Bitrue(ledger);
            case ManualRateProvider.ID:
                return new ManualRateProvider();
            case XrplPriceOracle.ID:
                var livenet = Arrays.stream(ledger.getDefaultNetworkInfo()).filter(NetworkInfo::isLivenet).findFirst().orElseThrow();
                return new XrplPriceOracle(livenet);
            case CryptoPriceOracle.ID:
                return new CryptoPriceOracle(new Currency(ledger.getNativeCcySymbol()));
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

    public static ExchangeRateProvider[] allPriceOracles(Ledger ledger) {
        var list = new ArrayList<ExchangeRateProvider>();
        for (var id : ledger.getHistoricExchangeRateProviders()) {
            list.add(create(id, ledger, ledger.getNetwork()));
        }
        return list.toArray(new ExchangeRateProvider[0]);
    }

    public static ExchangeRateProvider defaultPriceOracle(Ledger ledger) {
        return Arrays.stream(allPriceOracles(ledger)).findFirst().orElse(null);
    }

    public static boolean supports(Ledger ledger, String id) {
        return Arrays.asList(ledger.getExchangeRateProviders()).contains(id);
    }
}
