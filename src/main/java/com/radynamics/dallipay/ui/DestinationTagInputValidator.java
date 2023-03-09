package com.radynamics.dallipay.ui;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.DestinationTagBuilder;

public class DestinationTagInputValidator implements InputControlValidator {
    private final DestinationTagBuilder builder;

    public DestinationTagInputValidator(DestinationTagBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Parameter 'builder' cannot be null");
        this.builder = builder;
    }

    @Override
    public boolean isValid(Object value) {
        return builder.isValid((String) value);
    }

    public UnsignedInteger getValidOrNull(String text) {
        if (!builder.isValid(text)) {
            return null;
        }

        return builder.from(text).build();
    }

    @Override
    public String getValidExampleInput() {
        return "\"123456\"";
    }
}
