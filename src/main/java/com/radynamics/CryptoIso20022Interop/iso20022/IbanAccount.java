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
}
