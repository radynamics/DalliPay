package com.radynamics.dallipay.cryptoledger.signing;

public class SigningException extends Exception {
    public SigningException(String errorMessage) {
        this(errorMessage, null);
    }

    public SigningException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}