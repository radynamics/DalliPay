package com.radynamics.dallipay.cryptoledger.xrpl;

import com.google.common.primitives.UnsignedInteger;
import org.apache.commons.lang3.StringUtils;

public class DestinationTagBuilder implements com.radynamics.dallipay.cryptoledger.DestinationTagBuilder {
    private final UnsignedInteger destinationTag;

    public DestinationTagBuilder() {
        this(null);
    }

    private DestinationTagBuilder(UnsignedInteger destinationTag) {
        this.destinationTag = destinationTag;
    }

    @Override
    public boolean isValid(String value) {
        return StringUtils.isEmpty(value) || parse(value) != null;
    }

    private UnsignedInteger parse(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }

        try {
            return UnsignedInteger.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public DestinationTagBuilder from(String value) {
        return new DestinationTagBuilder(parse(value));
    }

    @Override
    public UnsignedInteger build() {
        return destinationTag;
    }
}
