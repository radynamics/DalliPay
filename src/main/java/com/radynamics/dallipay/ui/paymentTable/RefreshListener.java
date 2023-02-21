package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.iso20022.Payment;

public interface RefreshListener {
    void onRefresh(Payment p);
}
