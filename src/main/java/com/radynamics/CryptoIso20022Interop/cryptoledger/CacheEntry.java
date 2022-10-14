package com.radynamics.CryptoIso20022Interop.cryptoledger;

import java.time.Duration;
import java.util.Date;

public class CacheEntry<T> {
    private final Date created = new Date();
    private final T value;

    public CacheEntry(T value) {
        this.value = value;
    }

    public Duration getAge() {
        return Duration.ofMillis(new Date().getTime() - created.getTime());
    }

    public T getValue() {
        return value;
    }
}
