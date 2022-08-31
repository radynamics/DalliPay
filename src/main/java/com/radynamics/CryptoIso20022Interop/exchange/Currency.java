package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;

import java.util.Objects;

public class Currency {
    private final String code;
    private final Wallet issuer;

    public Currency(String ccy) {
        this(ccy, null);
    }

    public Currency(String ccy, Wallet issuer) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        this.code = ccy;
        this.issuer = issuer;
    }

    public String getCode() {
        return code;
    }

    public Wallet getIssuer() {
        return issuer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(code, currency.code) && Objects.equals(issuer, currency.issuer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, issuer);
    }

    @Override
    public String toString() {
        return issuer == null ? code : String.format("%s (%s)", code, issuer.getPublicKey());
    }
}
