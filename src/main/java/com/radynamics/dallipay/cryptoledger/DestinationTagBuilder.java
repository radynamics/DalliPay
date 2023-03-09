package com.radynamics.dallipay.cryptoledger;

import com.google.common.primitives.UnsignedInteger;

public interface DestinationTagBuilder {

    boolean isValid(String value);

    DestinationTagBuilder from(String value);

    UnsignedInteger build();
}
