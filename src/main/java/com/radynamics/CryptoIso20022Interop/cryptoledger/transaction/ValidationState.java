package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import java.util.ArrayList;

public enum ValidationState {
    Ok,
    Info,
    Warning,
    Error,
    ;

    public boolean higherThan(ValidationState s) {
        var ordered = new ArrayList<ValidationState>();
        for (var item : ValidationState.values()) {
            ordered.add(item);
        }
        return ordered.indexOf(this) > ordered.indexOf(s);
    }
}
