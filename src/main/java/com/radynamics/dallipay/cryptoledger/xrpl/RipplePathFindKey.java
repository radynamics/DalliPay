package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Key;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.exchange.Money;

public class RipplePathFindKey implements Key {
    private final Wallet sender;
    private final Wallet receiver;
    private final Money amount;

    public RipplePathFindKey(Wallet sender, Wallet receiver, Money amount) {
        if (sender == null) throw new IllegalArgumentException("Parameter 'sender' cannot be null");
        if (receiver == null) throw new IllegalArgumentException("Parameter 'receiver' cannot be null");
        if (amount == null) throw new IllegalArgumentException("Parameter 'amount' cannot be null");
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    @Override
    public String get() {
        return String.format("%s_%s_%s", sender.getPublicKey(), receiver.getPublicKey(), amount.getNumber() + amount.getCcy().getCode());
    }
}
