package com.radynamics.dallipay.cryptoledger;

import java.time.Duration;
import java.util.Date;

public class CacheEntry<T> {
    private final Date created = new Date();
    private final T value;
    private Duration duration = Duration.ZERO;

    public CacheEntry(T value) {
        this.value = value;
    }

    public Duration getAge() {
        return Duration.ofMillis(new Date().getTime() - created.getTime());
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public T getValue() {
        return value;
    }
}
