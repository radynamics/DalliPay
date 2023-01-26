package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public interface PaymentPath {
    int getRank();

    void apply(Payment p);

    boolean isSet(Payment p);

    String getDisplayText();

    Currency getCcy();
}
