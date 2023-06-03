package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.iso20022.Payment;

public interface PaymentPath {
    int getRank();

    void apply(Payment p);

    boolean isSet(Payment p);

    String getDisplayText();

    boolean isVolatile();

    Currency getCcy();
}
