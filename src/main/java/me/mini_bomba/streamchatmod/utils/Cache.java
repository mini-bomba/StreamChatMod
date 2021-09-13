package me.mini_bomba.streamchatmod.utils;


import java.util.*;

/**
 * A Threadsafe cache. Basically a map with a size limit that automatically forgets the last set/queried value after max size is exceeded.
 * @param <K> Type of the keys
 * @param <V> Type of the values
 */
public class Cache<K, V> {
    public final int maxSize;
    private final Map<K, V> map;
    private final LinkedList<K> queue;

    /**
     * @param maxSize maximum size of the cache
     */
    public Cache(int maxSize) {
        if (maxSize < 1) throw new IllegalArgumentException("maxSize must be higher than 0!");
        this.maxSize = maxSize;
        this.map = new HashMap<>();
        this.queue = new LinkedList<>();
    }

    /**
     * Get a value from the cache<br>
     * Causes the queried key to become "refreshed" (moved back on top of the recently used list)<br>
     * As this method may throw, it's recommended to use the {@link #contains} method to check if the value exists
     * @param key Key for the value
     * @throws NoSuchElementException No value with the given key is in the cache
     * @return The value
     */
    public V get(K key) {
        synchronized (map) {
            if (!map.containsKey(key)) throw new NoSuchElementException();
            queue.remove(key);
            queue.add(key);
            return map.get(key);
        }
    }

    /**
     * Get a value from the cache<br>
     * Causes the queried key to become "refreshed" (moved back on top of the recently used list)<br>
     * Unlike {@link #get}, this method returns an optional-optional and will not throw on a non-existent or forgotten value.
     * @param key
     * @return
     */
    public Optional<Optional<V>> getOptional(K key) {
        /*
         * Reasoning behind the nested Optional:
         * • The value may not be in the cache
         * • The value may be null
         * • We need to differentiate between a null value and a non-existent value.
         * • The value of the Optional cannot be null, therefore we need to use one Optional for value existence, and another for the value itself.
         */
        synchronized (map) {
            if (!map.containsKey(key)) return Optional.empty();
            queue.remove(key);
            queue.add(key);
            return Optional.of(Optional.ofNullable(map.get(key)));
        }
    }

    /**
     * Put a value into the cache<br>
     * The value will be forgotten if the value is not queried often and the set maxSize is exceeded.
     * @param key Key for the value
     * @param value The value
     */
    public void put(K key, V value) {
        synchronized (map) {
            map.put(key, value);
            queue.remove(key);
            queue.add(key);
            if (queue.size() > maxSize) {
                K removedKey = queue.remove();
                map.remove(removedKey);
            }
        }
    }

    /**
     * Removes a value from the cache immediately
     * @param key Key for the value to remove
     * @return true if a value was removed
     */
    public boolean remove(K key) {
        boolean res;
        synchronized (map) {
            res = map.remove(key) != null;
            res = queue.remove(key) || res;
        }
        return res;
    }

    /**
     * Checks if the cache contains a value with a given key
     * @param key Key for the value
     * @return true if the cache contains that value
     */
    public boolean contains(K key) {
        synchronized (map) {
            return map.containsKey(key);
        }
    }

    /**
     * Checks the current size of the cache
     * @return number of elements in the cache
     */
    public int getSize() {
        synchronized (map) {
            return map.size();
        }
    }
}
