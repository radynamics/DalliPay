package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.iso20022.Payment;

public interface PaymentListener {
    void onRemove(Payment p);
}
