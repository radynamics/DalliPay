package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class MoneySums {
    private final ArrayList<Money> amounts = new ArrayList<>();

    public void plus(Money amt) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        amounts.add(amt);
    }

    public Money sum(String ccy) {
        var sum = Money.zero(new Currency(ccy));
        for (var amt : amounts) {
            if (amt.getCcy().getCode().equals(ccy)) {
                sum = sum.plus(amt);
            }
        }
        return sum;
    }

    public String[] currencies() {
        var set = new HashSet<String>();
        for (var amt : amounts) {
            set.add(amt.getCcy().getCode());
        }
        return set.toArray(new String[0]);
    }

    public Money[] sum() {
        var list = new ArrayList<Money>();
        for (var ccy : currencies()) {
            list.add(sum(ccy));
        }

        Collections.sort(list, Comparator.comparingDouble(o -> o.getNumber().doubleValue()));

        return list.toArray(new Money[0]);
    }

    @Override
    public String toString() {
        return String.format("size: %s", amounts.size());
    }
}
