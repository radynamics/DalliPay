package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

public class PayString {
    private final String value;

    private static final String DELIMITER_NAME = "$";

    public PayString(String value) {
        this.value = value;
    }

    public static boolean matches(String value) {
        if (value == null) return false;
        // Eg. user$domain.com
        var indexSeparator = value.indexOf(DELIMITER_NAME);
        var indexDot = value.indexOf(".");
        return indexSeparator != -1 && indexDot != -1
                && indexSeparator > 0
                && indexDot > (indexSeparator + 1);
    }

    public static PayString create(String value) {
        if (!matches(value)) return null;
        return new PayString(value);
    }

    public String getValue() {
        return value;
    }

    public String name() {
        return value.substring(0, value.indexOf(DELIMITER_NAME));
    }

    public String domain() {
        return value.substring(value.indexOf(DELIMITER_NAME) + DELIMITER_NAME.length());
    }

    @Override
    public String toString() {
        return "Value: " + value;
    }
}
