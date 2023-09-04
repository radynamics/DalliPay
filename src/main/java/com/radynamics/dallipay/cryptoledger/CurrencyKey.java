package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.Currency;

public class CurrencyKey implements Key {
    private final Currency ccy;

    public CurrencyKey(Currency ccy) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        this.ccy = ccy;
    }

    @Override
    public String get() {
        return ccy.getIssuer() == null ? ccy.getCode() : String.format("%s_%s", ccy.getCode(), ccy.getIssuer().getPublicKey());
    }
}
