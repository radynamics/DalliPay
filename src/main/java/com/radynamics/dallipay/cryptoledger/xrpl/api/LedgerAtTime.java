package com.radynamics.dallipay.cryptoledger.xrpl.api;

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

    @Override
    public String toString() {
        return "%s -> %s".formatted(pointInTime, index);
    }
}
