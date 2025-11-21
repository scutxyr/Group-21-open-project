package com.taobao.arthas.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A tiny TTL cache based only on JDK collections.
 * - Thread-safe
 * - TTL expiration
 * - Simple max-size protection (clear when full)
 *
 * NOTE: If no one references this class, it will NOT affect runtime behavior.
 */
public class TtlCache<K, V> {

    private final long ttlMillis;
    private final int maxSize;
    private final Map<K, Entry<V>> map = new ConcurrentHashMap<>();

    public TtlCache(long ttlMillis, int maxSize) {
        this.ttlMillis = ttlMillis;
        this.maxSize = maxSize;
    }

    public V get(K key) {
        Entry<V> e = map.get(key);
        if (e == null) {
            return null;
        }
        if (System.currentTimeMillis() - e.time > ttlMillis) {
            map.remove(key);
            return null;
        }
        return e.value;
    }

    public void put(K key, V value) {
        if (map.size() >= maxSize) {
            // simplest safe fallback for course project
            map.clear();
        }
        map.put(key, new Entry<>(value));
    }

    public void clear() {
        map.clear();
    }

    private static class Entry<V> {
        final V value;
        final long time = System.currentTimeMillis();
        Entry(V value) {
            this.value = value;
        }
    }
}
