package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletCompare;

import java.util.Objects;

public class Currency {
    private final String code;
    private final Wallet issuer;

    public Currency(String ccy) {
        this(ccy, null);
    }

    public Currency(String ccy, Wallet issuer) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.length() == 0) throw new IllegalArgumentException("Parameter 'ccy' cannot be empty");
        this.code = ccy;
        this.issuer = issuer;
    }

    public boolean sameCode(Currency ccy) {
        if (ccy == null) return false;
        return code.equals(ccy.getCode());
    }

    public Currency withoutIssuer() {
        return new Currency(code);
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
        return Objects.equals(code, currency.code) && WalletCompare.isSame(issuer, currency.issuer);
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
