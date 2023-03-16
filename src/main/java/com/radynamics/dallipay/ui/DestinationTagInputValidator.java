package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.DestinationTagBuilder;

import java.util.ResourceBundle;

public class DestinationTagInputValidator implements InputControlValidator {
    private final DestinationTagBuilder builder;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public DestinationTagInputValidator(DestinationTagBuilder builder) {
        if (builder == null) throw new IllegalArgumentException("Parameter 'builder' cannot be null");
        this.builder = builder;
    }

    @Override
    public boolean isValid(Object value) {
        return builder.isValid((String) value);
    }

    @Override
    public String getValidExampleInput() {
        return res.getString("DestinationTagInputValidator.exampleInput");
    }
}
