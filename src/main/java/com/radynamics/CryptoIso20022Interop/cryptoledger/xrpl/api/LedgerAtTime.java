package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.time.ZonedDateTime;

public class LedgerAtTime {
    private ZonedDateTime pointInTime;
    private LedgerIndex index;

    public LedgerAtTime(ZonedDateTime pointInTime, LedgerIndex index) {
        this.pointInTime = pointInTime;
        this.index = index;
    }

    public ZonedDateTime getPointInTime() {
        return pointInTime;
    }

    public LedgerIndex getLedgerIndex() {
        return index;
    }
}
