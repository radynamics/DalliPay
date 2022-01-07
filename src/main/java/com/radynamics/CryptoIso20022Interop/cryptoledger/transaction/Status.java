package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import java.util.ArrayList;

public enum Status {
    Ok,
    Info,
    Warning,
    Error,
    ;

    public boolean higherThan(Status s) {
        var ordered = new ArrayList<Status>();
        for (var item : Status.values()) {
            ordered.add(item);
        }
        return ordered.indexOf(this) > ordered.indexOf(s);
    }
}
