package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;

public class Currency {
    private final String ccy;
    private final Wallet issuer;

    public Currency(String ccy) {
        this(ccy, null);
    }

    public Currency(String ccy, Wallet issuer) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        this.ccy = ccy;
        this.issuer = issuer;
    }

    public String getCcy() {
        return ccy;
    }

    public Wallet getIssuer() {
        return issuer;
    }

    @Override
    public String toString() {
        return issuer == null ? ccy : String.format("%s (%s)", ccy, issuer.getPublicKey());
    }
}
