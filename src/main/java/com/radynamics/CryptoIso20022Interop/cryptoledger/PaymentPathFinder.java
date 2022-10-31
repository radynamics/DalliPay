package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public interface PaymentPathFinder {
    PaymentPath[] find(CurrencyConverter currencyConverter, Payment p);
}
