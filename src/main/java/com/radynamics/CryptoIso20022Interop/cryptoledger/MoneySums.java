package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;

import java.util.ArrayList;
import java.util.HashSet;

public class MoneySums {
    private final ArrayList<Money> amounts = new ArrayList<>();

    public void plus(Money amt) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        amounts.add(amt);
    }

    public Money sum(Currency ccy) {
        var sum = Money.zero(ccy);
        for (var amt : amounts) {
            if (amt.getCcy().equals(ccy)) {
                sum = sum.plus(amt);
            }
        }
        return sum;
    }

    public Currency[] currencies() {
        var set = new HashSet<Currency>();
        for (var amt : amounts) {
            set.add(amt.getCcy());
        }
        return set.toArray(new Currency[0]);
    }

    public Money[] sum() {
        var list = new ArrayList<Money>();
        for (var ccy : currencies()) {
            list.add(sum(ccy));
        }
        return list.toArray(new Money[0]);
    }

    @Override
    public String toString() {
        return String.format("size: %s", amounts.size());
    }
}
