package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.iso20022.Payment;

public interface PaymentPathFinder {
    PaymentPath[] find(CurrencyConverter currencyConverter, Payment p);
}
