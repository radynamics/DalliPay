package com.radynamics.dallipay.cryptoledger.xrpl;

import org.apache.commons.lang3.StringUtils;

public class DestinationTagBuilder implements com.radynamics.dallipay.cryptoledger.DestinationTagBuilder {
    private final Integer destinationTag;

    public DestinationTagBuilder() {
        this(null);
    }

    private DestinationTagBuilder(Integer destinationTag) {
        this.destinationTag = destinationTag;
    }

    @Override
    public boolean isValid(String value) {
        return StringUtils.isEmpty(value) || parse(value) != null;
    }

    private Integer parse(String value) {
        if (value == null || value.length() != 6) {
            return null;
        }

        var num = Integer.valueOf(value);
        return 100000 <= num && num <= 999999 ? num : null;
    }

    @Override
    public DestinationTagBuilder from(String value) {
        return new DestinationTagBuilder(parse(value));
    }

    @Override
    public Integer build() {
        return destinationTag;
    }
}
