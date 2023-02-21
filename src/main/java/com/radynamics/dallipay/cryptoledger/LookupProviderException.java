package com.radynamics.dallipay.cryptoledger;

public class LookupProviderException extends Exception {
    public LookupProviderException(String errorMessage) {
        this(errorMessage, null);
    }

    public LookupProviderException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}