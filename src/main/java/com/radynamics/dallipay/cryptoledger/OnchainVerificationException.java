package com.radynamics.dallipay.cryptoledger;

public class OnchainVerificationException extends Exception {
    public OnchainVerificationException(String errorMessage) {
        this(errorMessage, null);
    }

    public OnchainVerificationException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}