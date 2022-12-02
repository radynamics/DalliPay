package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

public class SwissQrReference {
    private static final int[] MOD10 = {0, 9, 4, 6, 8, 2, 7, 1, 3, 5};

    public static boolean isValid(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }
        var unformatted = value.replace(" ", "");
        if (unformatted.length() < 27) {
            unformatted = "00000000000000000000000000".substring(0, 27 - unformatted.length()) + unformatted;
        }

        if (unformatted.length() != 27) {
            return false;
        }

        if (!isNumeric(unformatted)) {
            return false;
        }

        return calculateMod10(unformatted) == 0;
    }

    private static boolean isNumeric(String value) {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    private static int calculateMod10(String value) {
        int len = value.length();
        int carry = 0;
        for (int i = 0; i < len; i++) {
            int digit = value.charAt(i) - '0';
            carry = MOD10[(carry + digit) % 10];
        }
        return (10 - carry) % 10;
    }
}
