package com.zendesk.connect;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.squareup.tape2.ObjectQueue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Concrete implementation of {@link ObjectQueue.Converter} for serializing and deserializing
 * objects from the object queue.
 *
 * @param <T> the type of the object
 */
class GsonConverter<T> implements ObjectQueue.Converter<T> {

    private Gson gson;
    private Class<T> clazz;

    /**
     * Creates this {@link GsonConverter}
     *
     * @param gson an instance of {@link Gson}
     * @param clazz the class target for deserialization
     */
    GsonConverter(Gson gson, Class<T> clazz) {
        this.gson = gson;
        this.clazz = clazz;
    }

    /**
     * Deserializes an array of {@link Byte}s in the given type
     *
     * @param bytes the {@link Byte}s to be deserialized
     * @return the deserialized object
     */
    @Override
    public T from(@NonNull byte[] bytes) {
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes));
        return gson.fromJson(reader, clazz);
    }

    /**
     * Serializes an object to the given {@link OutputStream}
     *
     * @param object the object to be serialized
     * @param outputStream the target {@link OutputStream} for the serialized object
     */
    @Override
    public void toStream(@NonNull T object, @NonNull OutputStream outputStream) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
            gson.toJson(object, writer);
        }
    }
}
