package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

import org.apache.commons.lang3.Range;

public class KeyPool {
    private String desc;
    private Range<Integer> range;
    private Object timestamp;
    private boolean internal;
    private boolean keypool;
    private boolean active;
    private boolean watchonly;

    public String desc() {
        return desc;
    }

    public void desc(String desc) {
        this.desc = desc;
    }

    public Range<Integer> range() {
        return range;
    }

    public void range(Range<Integer> range) {
        this.range = range;
    }

    public Object timestamp() {
        return timestamp;
    }

    public void timestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public boolean internal() {
        return internal;
    }

    public void internal(boolean internal) {
        this.internal = internal;
    }

    public boolean keypool() {
        return keypool;
    }

    public void keypool(boolean keypool) {
        this.keypool = keypool;
    }

    public boolean active() {
        return active;
    }

    public void active(boolean active) {
        this.active = active;
    }

    public boolean watchonly() {
        return watchonly;
    }

    public void watchonly(boolean watchonly) {
        this.watchonly = watchonly;
    }

    @Override
    public String toString() {
        return desc;
    }
}
