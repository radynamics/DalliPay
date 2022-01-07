package com.radynamics.CryptoIso20022Interop.iso20022;

public class IbanAccount implements Account {
    private final String unformatted;

    public IbanAccount(String unformatted) {
        if (unformatted == null) throw new IllegalArgumentException("Parameter 'unformatted' cannot be null");
        if (unformatted.length() == 0) throw new IllegalArgumentException("Parameter 'unformatted' cannot be empty");
        this.unformatted = unformatted.replaceAll(" ", "");
    }

    @Override
    public String getUnformatted() {
        return unformatted;
    }
}
