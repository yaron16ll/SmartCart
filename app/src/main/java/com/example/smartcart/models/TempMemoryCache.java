package com.example.smartcart.models;

import java.util.HashMap;
import java.util.Map;

public class TempMemoryCache {

    private static volatile TempMemoryCache instance = null;
    private final Map<String, String> memoryCache = new HashMap<>();

    private TempMemoryCache() {
        // Private constructor to enforce singleton
    }

    public static TempMemoryCache getInstance() {
        if (instance == null) {
            synchronized (TempMemoryCache.class) {
                if (instance == null) {
                    instance = new TempMemoryCache();
                }
            }
        }
        return instance;
    }

    /**
     * Stores a string value in memory.
     *
     * @param key   The key under which the value is stored.
     * @param value The string value to store.
     */
    public void putString(String key, String value) {
        memoryCache.put(key, value);
    }

    /**
     * Retrieves a string value from memory.
     *
     * @param key          The key whose value is to be retrieved.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The string value associated with the key, or the default value if not found.
     */
    public String getString(String key, String defaultValue) {
        return memoryCache.getOrDefault(key, defaultValue);
    }

    /**
     * Removes a key-value pair from the memory cache.
     *
     * @param key The key to remove.
     */
    public void remove(String key) {
        memoryCache.remove(key);
    }

    /**
     * Clears all data from the memory cache.
     */
    public void clear() {
        memoryCache.clear();
    }
}
