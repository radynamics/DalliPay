package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

import com.radynamics.dallipay.cryptoledger.Key;

public class PayStringKey implements Key {
    private final PayString payString;

    public PayStringKey(PayString payString) {
        if (payString == null) throw new IllegalArgumentException("Parameter 'payString' cannot be null");
        this.payString = payString;
    }

    @Override
    public String get() {
        return payString.getValue();
    }
}
