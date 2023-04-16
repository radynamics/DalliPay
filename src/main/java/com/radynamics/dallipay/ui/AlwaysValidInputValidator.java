package com.radynamics.dallipay.ui;

public class AlwaysValidInputValidator implements InputControlValidator {
    @Override
    public boolean isValid(Object value) {
        return true;
    }

    @Override
    public String getValidExampleInput() {
        return "";
    }
}
