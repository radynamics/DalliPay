package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(ccy, currency.ccy) && Objects.equals(issuer, currency.issuer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccy, issuer);
    }

    @Override
    public String toString() {
        return issuer == null ? ccy : String.format("%s (%s)", ccy, issuer.getPublicKey());
    }
}
