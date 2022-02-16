package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

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
