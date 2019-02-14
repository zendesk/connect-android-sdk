package com.zendesk.connect;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zendesk.logger.Logger;

import javax.inject.Inject;

/**
 * An implementation of {@link BaseStorage} using {@link SharedPreferences} for general storage.
 * Uses a basic {@link Gson} object for serialisation / deserialisation.
 */
class SharedPreferencesStorage implements BaseStorage {

    private static final String LOG_TAG = "SharedPrefsStorage";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Inject
    SharedPreferencesStorage(SharedPreferences sharedPreferences, Gson gson) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
    }

    @Override
    public void put(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public void put(String key, Object object) {
        String value = gson.toJson(object);
        put(key, value);
    }

    @Override
    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            String storedValue = get(key);
            return storedValue == null ? null : gson.fromJson(storedValue, clazz);
        } catch (JsonSyntaxException e) {
            Logger.e(LOG_TAG, "Unable to deserialise JSON String into type %s",
                    clazz.getSimpleName(), e);
            return null;
        }
    }

    @Override
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    @Override
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
