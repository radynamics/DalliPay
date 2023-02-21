package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

public class OAuth2Exception extends Exception {
    public OAuth2Exception(String errorMessage) {
        super(errorMessage);
    }

    public OAuth2Exception(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
