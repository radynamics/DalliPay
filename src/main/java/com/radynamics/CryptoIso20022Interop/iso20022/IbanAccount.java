package com.radynamics.CryptoIso20022Interop.iso20022;

public class IbanAccount implements Account {
    private final String unformatted;

    public IbanAccount(String unformatted) {
        if (unformatted == null) throw new IllegalArgumentException("Parameter 'unformatted' cannot be null");
        this.unformatted = unformatted;
    }

    @Override
    public String getUnformatted() {
        return unformatted;
    }
}
