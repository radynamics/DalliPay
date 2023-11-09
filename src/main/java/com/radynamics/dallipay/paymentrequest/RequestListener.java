package com.radynamics.dallipay.paymentrequest;

import java.net.URI;

public interface RequestListener {
    void onPaymentRequest(URI requestUri);
}
