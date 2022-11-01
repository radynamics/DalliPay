package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class LedgerNativeCcyPath implements PaymentPath {
    private final Currency ccy;
    private final CurrencyConverter currencyConverter;

    public LedgerNativeCcyPath(CurrencyConverter currencyConverter, Currency ccy) {
        if (currencyConverter == null) throw new IllegalArgumentException("Parameter 'currencyConverter' cannot be null");
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        this.currencyConverter = currencyConverter;
        this.ccy = ccy;
    }

    @Override
    public int getRank() {
        return 0;
    }

    @Override
    public void apply(Payment p) {
        var ccyWithoutIssuer = p.getUserCcy().withoutIssuer();
        p.setUserCcy(ccyWithoutIssuer);
        p.setExchangeRate(currencyConverter.get(new CurrencyPair(ccy, ccyWithoutIssuer)));
        p.setAmount(Money.of(p.getAmount(), ccyWithoutIssuer));
    }

    @Override
    public boolean isSet(Payment p) {
        return ccy.equals(p.getAmountTransaction().getCcy());
    }

    public Currency getCcy() {
        return ccy;
    }

    @Override
    public String getDisplayText() {
        return ccy.getCode();
    }

    @Override
    public String toString() {
        return String.format("Ccy: %s", ccy);
    }
}
