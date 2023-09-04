package com.radynamics.dallipay.cryptoledger.xrpl.api;

public class LedgerAtTimeException extends Exception {
    public LedgerAtTimeException(String errorMessage) {
        super(errorMessage);
    }

    public LedgerAtTimeException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
