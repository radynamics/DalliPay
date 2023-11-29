package com.radynamics.dallipay.cryptoledger.bitcoin.api;

public class ApiException extends Exception {
    public ApiException(String errorMessage) {
        super(errorMessage);
    }

    public ApiException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
