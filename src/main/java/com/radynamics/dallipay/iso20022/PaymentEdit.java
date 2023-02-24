package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;

public class PaymentEdit {
    private final Payment payment;
    private boolean editable = true;

    private PaymentEdit(Payment payment) {
        if (payment == null) throw new IllegalArgumentException("Parameter 'payment' cannot be null");
        this.payment = payment;
    }

    public static PaymentEdit create(Payment payment) {
        return new PaymentEdit(payment);
    }

    private boolean transmitting() {
        return payment.getTransmission() == TransmissionState.Waiting;
    }

    public boolean editable() {
        return editable && payment.getOrigin() != Origin.Ledger && !transmitting();
    }

    public boolean exchangeRateEditable() {
        if (!editable) {
            return false;
        }

        if (editable()) {
            return true;
        }

        // Edit exchange rates before export is allowed
        return payment.getOrigin() == Origin.Ledger && payment.getExchangeRate() == null || !payment.getExchangeRate().isNone();
    }

    public boolean accountMappingEditable() {
        return editable && !transmitting();
    }

    public boolean removable() {
        return editable() && payment.getTransmission() != TransmissionState.Success && payment.getOrigin().isDeletable();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
