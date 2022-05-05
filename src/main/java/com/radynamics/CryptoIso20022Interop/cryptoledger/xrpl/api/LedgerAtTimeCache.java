package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.time.ZonedDateTime;
import java.util.HashMap;

public class LedgerAtTimeCache {
    final static Logger log = LogManager.getLogger(LedgerAtTimeCache.class);
    private HashMap<UnsignedInteger, LedgerAtTime> items = new HashMap<>();

    public LedgerAtTime add(ZonedDateTime pointInTime, LedgerIndex index) {
        var item = new LedgerAtTime(pointInTime, index);
        items.put(index.unsignedIntegerValue(), item);
        return item;
    }

    public LedgerAtTime find(LedgerIndex index) {
        if (items.containsKey(index.unsignedIntegerValue())) {
            var item = items.get(index.unsignedIntegerValue());
            log.trace(String.format("CACHE hit %s (%s)", item.getPointInTime(), item.getLedgerIndex().unsignedIntegerValue()));
            return item;
        }
        return null;
    }

    public LedgerAtTime find(ZonedDateTime dt) {
        for (var e : items.entrySet()) {
            var item = e.getValue();
            if (item.getPointInTime() == dt) {
                log.trace(String.format("CACHE hit %s (%s)", item.getPointInTime(), item.getLedgerIndex().unsignedIntegerValue()));
                return item;
            }
        }
        return null;
    }
}
