package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

public class LedgerIndexRange {
    private LedgerIndex start;
    private LedgerIndex end;

    private LedgerIndexRange(LedgerIndex start, LedgerIndex end) {
        this.start = start;
        this.end = end;
    }

    public static LedgerIndexRange of(LedgerIndex start, LedgerIndex end) {
        return new LedgerIndexRange(start, end);
    }

    public LedgerIndex getStart() {
        return start;
    }

    public LedgerIndex getEnd() {
        return end;
    }
}
