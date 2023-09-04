package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

public class PayStringException extends Exception {
    public PayStringException(String errorMessage) {
        super(errorMessage);
    }

    public PayStringException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
