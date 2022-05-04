package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import java.util.ArrayList;

public enum ValidationState {
    Ok(0),
    Info(1),
    Warning(2),
    Error(3),
    ;

    private final int level;

    ValidationState(int level) {
        this.level = level;
    }

    public boolean higherThan(ValidationState s) {
        var ordered = new ArrayList<ValidationState>();
        for (var item : ValidationState.values()) {
            ordered.add(item);
        }
        return ordered.indexOf(this) > ordered.indexOf(s);
    }

    public int getLevel() {
        return level;
    }
}
