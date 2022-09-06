package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;

import java.util.ArrayList;

public class MoneyBag {
    private ArrayList<Money> amounts = new ArrayList<>();

    public Money get(String ccy) {
        for (var amt : amounts) {
            if (amt.getCcy().getCode().equals(ccy)) {
                return amt;
            }
        }
        return Money.zero(new Currency(ccy));
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    public void set(Money amt) {
        amounts.removeIf(o -> o.getCcy().equals(amt.getCcy()));
        amounts.add(amt);
    }

    public void replaceBy(MoneyBag bag) {
        amounts = bag.amounts;
    }

    public Money[] all() {
        return amounts.toArray(new Money[0]);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (var amt : all()) {
            sb.append(String.format("%s %s", amt.getNumber(), amt.getCcy().getCode()));
        }
        return sb.toString();
    }
}
