package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;

import java.util.ArrayList;
import java.util.Optional;

public class MoneyBag {
    private ArrayList<Money> amounts = new ArrayList<>();

    public Optional<Money> get(Currency ccy) {
        for (var amt : amounts) {
            if (amt.getCcy().equals(ccy)) {
                return Optional.of(amt);
            }
        }
        return Optional.empty();
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
