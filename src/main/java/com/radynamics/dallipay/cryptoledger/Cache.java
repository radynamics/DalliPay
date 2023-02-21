package com.radynamics.dallipay.cryptoledger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Cache<T> {
    private final static Logger log = LogManager.getLogger(Cache.class);
    private final HashMap<String, CacheEntry<T>> cache = new HashMap<>();
    private final String prefix;

    private final Duration maxAge;

    public Cache(String prefix) {
        this(prefix, Duration.ofSeconds(60));
    }

    public Cache(String prefix, Duration maxAge) {
        this.prefix = prefix;
        this.maxAge = maxAge;
    }

    public void add(Wallet wallet, T data) {
        cache.put(createKey(wallet), new CacheEntry<>(data));
    }

    private String createKey(Wallet wallet) {
        return String.format("%s_%s", prefix, wallet.getPublicKey());
    }

    public boolean isPresent(Wallet wallet) {
        return getEntry(wallet) != null;
    }

    private CacheEntry<T> getEntry(Wallet wallet) {
        var needle = createKey(wallet);
        return cache.entrySet().stream().filter(o -> o.getKey().equals(needle)).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    public T get(Wallet wallet) {
        var o = getEntry(wallet);
        return o == null ? null : o.getValue();
    }

    public void evict(Wallet wallet) {
        var key = createKey(wallet);
        cache.entrySet().removeIf(entry -> entry.getKey().equals(key));
    }

    public synchronized void evictOutdated() {
        var oldSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().getAge().toSeconds() > maxAge.toSeconds());
        log.trace(String.format("Removed %s items", oldSize - cache.size()));
    }

    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return String.format("%s entries", cache.size());
    }
}
