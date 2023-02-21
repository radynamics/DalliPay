package com.radynamics.dallipay.cryptoledger;

public class ExpectedCurrency {
    private final Wallet issuer;

    public ExpectedCurrency(Wallet ccyIssuer) {
        if (ccyIssuer == null) throw new IllegalArgumentException("Parameter 'ccyIssuer' cannot be null");
        this.issuer = ccyIssuer;
    }

    public Wallet getIssuer() {
        return issuer;
    }

    public String getDisplayText() {
        return issuer.getPublicKey();
    }
}
