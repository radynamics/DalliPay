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

    public void add(Key key, T data) {
        cache.put(createKey(key), new CacheEntry<>(data));
    }

    private String createKey(Key key) {
        return String.format("%s_%s", prefix, key.get());
    }

    public boolean isPresent(Key key) {
        return getEntry(key) != null;
    }

    private CacheEntry<T> getEntry(Key key) {
        var needle = createKey(key);
        return cache.entrySet().stream().filter(o -> o.getKey().equals(needle)).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    public T get(Key key) {
        var o = getEntry(key);
        return o == null ? null : o.getValue();
    }

    public void evict(Key key) {
        var keyString = createKey(key);
        cache.entrySet().removeIf(entry -> entry.getKey().equals(keyString));
    }

    public synchronized void evictOutdated() {
        var oldSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().getAge().toSeconds() > maxAge.toSeconds());
        log.trace(String.format("Removed %s items", oldSize - cache.size()));
    }

    public void clear() {
        cache.clear();
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return String.format("%s entries", cache.size());
    }
}
