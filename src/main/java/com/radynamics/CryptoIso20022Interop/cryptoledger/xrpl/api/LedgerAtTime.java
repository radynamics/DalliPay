package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.time.LocalDateTime;

public class LedgerAtTime {
    private LocalDateTime pointInTime;
    private LedgerIndex index;

    public LedgerAtTime(LocalDateTime pointInTime, LedgerIndex index) {
        this.pointInTime = pointInTime;
        this.index = index;
    }

    public LocalDateTime getPointInTime() {
        return pointInTime;
    }

    public LedgerIndex getLedgerIndex() {
        return index;
    }
}
