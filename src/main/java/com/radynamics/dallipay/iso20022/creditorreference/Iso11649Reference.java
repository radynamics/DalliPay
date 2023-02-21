package com.radynamics.dallipay.iso20022.creditorreference;

public class Iso11649Reference {
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        var unformatted = value.replace(" ", "");
        if (unformatted.length() < 5 || unformatted.length() > 25) {
            return false;
        }
        if (!unformatted.toLowerCase().startsWith("rf")) {
            return false;
        }

        if (!isAlphaNumeric(unformatted)) {
            return false;
        }

        if (!Character.isDigit(unformatted.charAt(2)) || !Character.isDigit(unformatted.charAt(3))) {
            return false;
        }

        return calculateMod97(unformatted) == 1;
    }

    private static boolean isAlphaNumeric(String value) {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            if (ch >= '0' && ch <= '9') {
                continue;
            }
            if (ch >= 'A' && ch <= 'Z') {
                continue;
            }
            if (ch >= 'a' && ch <= 'z') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Calculate the reference's modulo 97 checksum according to ISO11649 and IBAN
     * standard.
     * <p>
     * The string may only contains digits, letters ('A' to 'Z' and 'a' to 'z', no
     * accents). It must not contain white space.
     * </p>
     *
     * @param reference the reference
     * @return the checksum (0 to 96)
     * @throws IllegalArgumentException thrown if the reference contains an invalid character
     */
    private static int calculateMod97(String reference) {
        int len = reference.length();
        if (len < 5) {
            throw new IllegalArgumentException("Insufficient characters for checksum calculation");
        }

        var rearranged = reference.substring(4) + reference.substring(0, 4);
        int sum = 0;
        for (int i = 0; i < len; i++) {
            char ch = rearranged.charAt(i);
            if (ch >= '0' && ch <= '9') {
                sum = sum * 10 + (ch - '0');
            } else if (ch >= 'A' && ch <= 'Z') {
                sum = sum * 100 + (ch - 'A' + 10);
            } else if (ch >= 'a' && ch <= 'z') {
                sum = sum * 100 + (ch - 'a' + 10);
            } else {
                throw new IllegalArgumentException("Invalid character in reference: " + ch);
            }
            if (sum > 9999999) {
                sum = sum % 97;
            }
        }

        sum = sum % 97;
        return sum;
    }
}
