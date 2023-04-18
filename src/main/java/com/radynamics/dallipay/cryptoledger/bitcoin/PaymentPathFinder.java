package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.radynamics.dallipay.cryptoledger.LedgerNativeCcyPath;
import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.ArrayList;

public class PaymentPathFinder implements com.radynamics.dallipay.cryptoledger.PaymentPathFinder {
    public PaymentPath[] find(CurrencyConverter currencyConverter, Payment p) {
        if (p == null) throw new IllegalArgumentException("Parameter 'p' cannot be null");

        var list = new ArrayList<PaymentPath>();

        var ledgerCcy = new Currency(p.getLedger().getNativeCcySymbol());
        list.add(new LedgerNativeCcyPath(currencyConverter, ledgerCcy));

        return list.toArray(new PaymentPath[0]);
    }
}
