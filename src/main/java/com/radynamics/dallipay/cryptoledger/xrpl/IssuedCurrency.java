package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.exchange.CurrencyPair;

public class IssuedCurrency {
    private final CurrencyPair pair;
    private final Wallet issuer;
    private final Wallet receiver;

    public IssuedCurrency(CurrencyPair pair, Wallet issuer, Wallet receiver) {
        this.pair = pair;
        this.issuer = issuer;
        this.receiver = receiver;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public Wallet getIssuer() {
        return issuer;
    }

    public Wallet getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
        return String.format("{%s}", pair);
    }
}
