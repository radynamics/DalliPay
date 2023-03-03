package com.radynamics.dallipay;

public enum ReturnCode {
    MandatoryUpdate(11);

    public final int value;

    ReturnCode(int value) {
        this.value = value;
    }
}
