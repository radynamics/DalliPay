package com.radynamics.dallipay.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

public final class LedgerFactory {
    public static final Ledger create(String id) {
        return create(LedgerId.of(id));
    }

    public static Ledger create(LedgerId ledgerId) {
        switch (ledgerId) {
            case Xrpl:
                return new com.radynamics.dallipay.cryptoledger.xrpl.Ledger();
            case Xahau:
                return new com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger();
            default:
                throw new NotImplementedException(String.format("Ledger %s unknown.", ledgerId));
        }
    }

    public static Ledger[] all() {
        var ledgerIds = LedgerId.values();
        var result = new Ledger[ledgerIds.length];
        for (var i = 0; i < ledgerIds.length; i++) {
            result[i] = create(ledgerIds[i]);
        }
        return result;
    }
}
