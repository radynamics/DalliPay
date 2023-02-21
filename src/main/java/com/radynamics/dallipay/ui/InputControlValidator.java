package com.radynamics.dallipay.ui;

public interface InputControlValidator {
    boolean isValid(Object value);

    String getValidExampleInput();
}
