package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.iso20022.Payment;

public class AmountRefresher {
    private final Payment[] payments;

    public AmountRefresher(Payment[] payments) {
        this.payments = payments;
    }

    public void refresh(ExchangeRateProvider provider) {
        provider.load();
        for (var p : payments) {
            var ccyPair = p.createCcyPair();
            var r = ExchangeRate.getOrNull(provider.latestRates(), ccyPair);
            if (r != null) {
                p.setExchangeRate(r);
            }
        }
        refresh();
    }

    public void refresh() {
        for (var p : payments) {
            if (p.getExchangeRate() != null && p.getExchangeRate().isUndefined()) {
                p.setExchangeRate(null);
            }
            p.refreshAmounts();
        }
    }
}
