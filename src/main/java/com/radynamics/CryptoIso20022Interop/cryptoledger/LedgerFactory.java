package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

public final class LedgerFactory {
    public static final Ledger create(String id) {
        return create(LedgerId.of(id));
    }

    public static Ledger create(LedgerId ledgerId) {
        switch (ledgerId) {
            case Xrpl:
                return new com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Ledger();
            default:
                throw new NotImplementedException(String.format("Ledger %s unknown.", ledgerId));
        }
    }
}
