package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

public class KeyPool {
    private final String raw;
    private final String desc;

    public KeyPool(String raw, String desc) {
        this.raw = raw;
        this.desc = desc;
    }

    public String raw() {
        return raw;
    }

    public String desc() {
        return desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
