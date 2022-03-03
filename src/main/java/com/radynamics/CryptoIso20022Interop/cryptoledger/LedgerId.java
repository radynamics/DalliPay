package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

public enum LedgerId {
    Xrpl(0, "xrpl");

    private final int numericId;
    private final String textId;

    LedgerId(int numericId, String textId) {
        this.numericId = numericId;
        this.textId = textId;
    }

    public static LedgerId of(int id) {
        for (var e : LedgerId.values()) {
            if (e.numericId() == id) {
                return e;
            }
        }
        throw new NotImplementedException(String.format("LedgerId %s unknown.", id));
    }

    public static LedgerId of(String id) {
        for (var e : LedgerId.values()) {
            if (e.textId().equals(id)) {
                return e;
            }
        }
        throw new NotImplementedException(String.format("LedgerId %s unknown.", id));
    }

    public int numericId() {
        return numericId;
    }

    public String textId() {
        return textId;
    }
}
