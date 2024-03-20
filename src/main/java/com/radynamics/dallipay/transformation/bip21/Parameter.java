package com.radynamics.dallipay.transformation.bip21;

public class Parameter {
    private final String value;
    private final Boolean required;

    public Parameter(String value, Boolean required) {
        super();
        this.value = value;
        this.required = required;
    }

    public String getValue() {
        return value;
    }

    public Boolean isRequired() {
        return required;
    }
}
