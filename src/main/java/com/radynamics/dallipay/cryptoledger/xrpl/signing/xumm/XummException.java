package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

public class XummException extends Exception {
    public XummException(String errorMessage) {
        super(errorMessage);
    }

    public XummException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
