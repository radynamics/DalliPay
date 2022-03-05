package com.radynamics.CryptoIso20022Interop.iso20022;

public class IbanAccount implements Account {
    private final String unformatted;

    public static IbanAccount Empty = new IbanAccount();

    private IbanAccount() {
        this.unformatted = "";
    }

    public IbanAccount(String unformatted) {
        super();
        if (unformatted == null) throw new IllegalArgumentException("Parameter 'unformatted' cannot be null");
        if (unformatted.length() == 0) throw new IllegalArgumentException("Parameter 'unformatted' cannot be empty");
        this.unformatted = unformatted.replaceAll(" ", "");
    }

    @Override
    public String getUnformatted() {
        return unformatted;
    }

    public String getFormatted() {
        final int length = unformatted.length();
        final int lastPossibleBlock = length - 4;
        final StringBuilder sb = new StringBuilder(length + (length - 1) / 4);
        int i;
        for (i = 0; i < lastPossibleBlock; i += 4) {
            sb.append(unformatted, i, i + 4);
            sb.append(' ');
        }
        sb.append(unformatted, i, length);
        return sb.toString();
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }

        final int IBAN_MIN_SIZE = 15;
        final int IBAN_MAX_SIZE = 34;
        final long IBAN_MAX = 999999999;
        final long IBAN_MODULUS = 97;

        var unformatted = value.trim().replaceAll(" ", "");
        if (unformatted.length() < IBAN_MIN_SIZE || unformatted.length() > IBAN_MAX_SIZE) {
            return false;
        }

        var reformat = unformatted.substring(4) + unformatted.substring(0, 4);
        long total = 0;
        for (int i = 0; i < reformat.length(); i++) {
            var charValue = Character.getNumericValue(reformat.charAt(i));
            if (charValue < 0 || charValue > 35) {
                return false;
            }

            total = (charValue > 9 ? total * 100 : total * 10) + charValue;
            if (total > IBAN_MAX) {
                total = (total % IBAN_MODULUS);
            }
        }

        return (total % IBAN_MODULUS) == 1;
    }

    @Override
    public String toString() {
        return unformatted;
    }
}
