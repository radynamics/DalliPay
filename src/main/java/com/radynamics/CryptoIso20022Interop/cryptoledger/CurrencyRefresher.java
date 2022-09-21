package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.Arrays;

public class CurrencyRefresher {
    private final MoneyBag senderBalances;

    public CurrencyRefresher(MoneyBag senderBalances) {
        this.senderBalances = senderBalances;
    }

    public void refresh(Payment p) {
        var available = Arrays.stream(senderBalances.all()).map(Money::getCcy).toArray(Currency[]::new);
        Currency ccyAnyIssuer = findSameCode(available, p.getUserCcy());
        if (ccyAnyIssuer == null) {
            return;
        }

        p.setAmount(Money.of(p.getAmount(), ccyAnyIssuer));
    }

    private static Currency findSameCode(Currency[] available, Currency ccy) {
        for (var o : available) {
            if (o.sameCode(ccy)) {
                return o;
            }
        }

        return null;
    }
}
