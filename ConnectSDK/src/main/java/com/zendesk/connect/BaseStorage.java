package com.zendesk.connect;

interface BaseStorage {

    /**
     * Store a given key/value pair. Will overwrite existing values if key is reused.
     *
     * @param key: A unique key
     * @param value: The value to be stored
     */
    void put(String key, String value);

    /**
     * Store a given key/value pair. Will overwrite existing values if key is reused.
     * Implementation should handle serialisation.
     *
     * @param key: A unique key
     * @param object: The object to be stored
     */
    void put(String key, Object object);

    /**
     * Gets a stored String from storage. Returns null if it doesn't exist
     *
     * @param key: The key the value is stored against
     * @return The stored String value, or null if it doesn't exist
     */
    String get(String key);

    /**
     * Gets a stored Object from storage. Returns null if it doesn't exist
     *
     * @param key: The key the value is stored against
     * @param clazz: The class the object is expected to be
     * @return The deserialised object, or null if it doesn't exist
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Removes a stored value for the provided key
     *
     * @param key: The key to be removed
     */
    void remove(String key);

    /**
     * Clears everything from storage
     */
    void clear();

}
