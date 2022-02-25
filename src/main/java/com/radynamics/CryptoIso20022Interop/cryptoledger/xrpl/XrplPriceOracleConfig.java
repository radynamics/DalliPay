package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;

import java.util.HashSet;

public class XrplPriceOracleConfig {
    private final HashSet<IssuedCurrency> issuedCurrencies = new HashSet<>();

    public void load() {
        issuedCurrencies.add(new IssuedCurrency(new CurrencyPair("XRP", "USD"), new Wallet("r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE"), new Wallet("rXUMMaPpZqPutoRszR29jtC8amWq3APkx")));
        issuedCurrencies.add(new IssuedCurrency(new CurrencyPair("XRP", "JPY"), new Wallet("r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE"), new Wallet("rrJPYwVRyWFcwfaNMm83QEaCexEpKnkEg")));
    }

    public IssuedCurrency[] issuedCurrencies() {
        return issuedCurrencies.toArray(new IssuedCurrency[0]);
    }
}
