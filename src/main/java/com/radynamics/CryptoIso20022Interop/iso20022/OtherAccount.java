package com.radynamics.CryptoIso20022Interop.iso20022;

public class OtherAccount implements Account {
    private final String unformatted;

    public OtherAccount(String unformatted) {
        if (unformatted == null) throw new IllegalArgumentException("Parameter 'unformatted' cannot be null");
        this.unformatted = unformatted;
    }

    @Override
    public String getUnformatted() {
        return unformatted;
    }

    @Override
    public String toString() {
        return unformatted;
    }
}
