package com.radynamics.CryptoIso20022Interop.cryptoledger;

import org.apache.commons.lang3.NotImplementedException;

public final class LedgerFactory {
    public static final Ledger create(String id) {
        switch (id.toLowerCase()) {
            case com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Ledger.ID:
                return new com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Ledger();
            default:
                throw new NotImplementedException(String.format("Ledger %s unknown.", id));
        }
    }
}
