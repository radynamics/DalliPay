package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

public class HwiException extends Exception {
    public HwiException(String errorMessage) {
        this(errorMessage, null);
    }

    public HwiException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
