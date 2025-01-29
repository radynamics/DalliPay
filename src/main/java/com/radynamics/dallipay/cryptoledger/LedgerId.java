package com.radynamics.dallipay.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Optional;

public enum LedgerId {
    Xrpl(0, "xrpl", "xrpl"),
    Xahau(1, "xahau", "xahau"),
    Bitcoin(2, "bitcoin", "bitcoin");

    private final int numericId;
    private final String textId;
    private final String externalId;

    LedgerId(int numericId, String textId, String externalId) {
        this.numericId = numericId;
        this.textId = textId;
        this.externalId = externalId;
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

    public static Optional<LedgerId> ofExternalId(String externalId) {
        for (var e : LedgerId.values()) {
            if (e.externalId().equals(externalId)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    public boolean sameAs(LedgerId ledgerId) {
        if (ledgerId == null) return false;
        return numericId() == ledgerId.numericId();
    }

    public int numericId() {
        return numericId;
    }

    public String textId() {
        return textId;
    }

    public String externalId() {
        return externalId;
    }
}
