package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Origin;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;

public class PaymentEdit {
    private final Payment payment;

    private PaymentEdit(Payment payment) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        this.payment = payment;
    }

    public static PaymentEdit is(Payment payment) {
        return new PaymentEdit(payment);
    }

    private boolean transmitting() {
        return payment.getTransmission() == TransmissionState.Waiting;
    }

    public boolean editable() {
        return payment.getOrigin() != Origin.Ledger && !transmitting();
    }

    public boolean exchangeRateEditable() {
        if (editable()) {
            return true;
        }

        // Edit exchange rates before export is allowed
        return payment.getOrigin() == Origin.Ledger && payment.getExchangeRate() == null || !payment.getExchangeRate().isNone();
    }

    public boolean accountMappingEditable() {
        return !transmitting();
    }

    public boolean removable() {
        return editable() && payment.getTransmission() != TransmissionState.Success && payment.getOrigin().isDeletable();
    }
}
