package com.radynamics.dallipay.cryptoledger.xrpl.api;

import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;

import java.time.ZonedDateTime;

public class LedgerAtTime {
    private ZonedDateTime pointInTime;
    private LedgerSpecifier index;

    public LedgerAtTime(ZonedDateTime pointInTime, LedgerSpecifier index) {
        this.pointInTime = pointInTime;
        this.index = index;
    }

    public ZonedDateTime getPointInTime() {
        return pointInTime;
    }

    public LedgerSpecifier getLedgerSpecifier() {
        return index;
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(pointInTime, index);
    }
}
