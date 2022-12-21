package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

public class XummException extends Exception {
    public XummException(String errorMessage) {
        super(errorMessage);
    }

    public XummException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
