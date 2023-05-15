package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Key;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.exchange.Currency;

public class RipplePathFindKey implements Key {
    private final Wallet sender;
    private final Wallet receiver;
    private final Currency ccy;

    public RipplePathFindKey(Wallet sender, Wallet receiver, Currency ccy) {
        if (sender == null) throw new IllegalArgumentException("Parameter 'sender' cannot be null");
        if (receiver == null) throw new IllegalArgumentException("Parameter 'receiver' cannot be null");
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        this.sender = sender;
        this.receiver = receiver;
        this.ccy = ccy;
    }

    @Override
    public String get() {
        var ccyKey = ccy.getIssuer() == null
                ? ccy.getCode()
                : String.format("%s_%s", ccy.getCode(), ccy.getIssuer().getPublicKey());
        return String.format("%s_%s_%s", sender.getPublicKey(), receiver.getPublicKey(), ccyKey);
    }
}
