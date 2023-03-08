package com.radynamics.dallipay.cryptoledger;

public interface DestinationTagBuilder {

    boolean isValid(String value);

    DestinationTagBuilder from(String value);

    Integer build();
}
