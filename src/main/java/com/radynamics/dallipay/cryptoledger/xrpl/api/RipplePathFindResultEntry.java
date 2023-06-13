package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.radynamics.dallipay.exchange.Money;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;

public class RipplePathFindResultEntry {
    private final RipplePathFindResult result;
    private final Money amount;

    public RipplePathFindResultEntry(RipplePathFindResult result, Money amount) {
        if (amount == null) throw new IllegalArgumentException("Parameter 'amount' cannot be null");
        this.result = result;
        this.amount = amount;
    }

    public RipplePathFindResult getResult() {
        return result;
    }

    public Money getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("Amount: %s", amount);
    }
}
