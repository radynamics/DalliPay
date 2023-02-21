package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.Money;

public class Fee {
    private final Money amount;
    private final FeeType type;

    public Fee(Money amount, FeeType type) {
        if (amount == null) throw new IllegalArgumentException("Parameter 'amount' cannot be null");
        this.amount = amount;
        this.type = type;
    }

    public Money getAmount() {
        return amount;
    }

    public FeeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", amount, type);
    }
}
