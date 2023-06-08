package com.radynamics.dallipay.cryptoledger.ethereum.api;

public class AlchemyException extends Exception {
    public AlchemyException(String errorMessage) {
        super(errorMessage);
    }

    public AlchemyException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
