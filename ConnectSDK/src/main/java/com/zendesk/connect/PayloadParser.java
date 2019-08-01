package com.zendesk.connect;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for Connect payload parsers
 *
 * @param <T> the type of object being parsed
 */
abstract class PayloadParser<T> {

    private Set<String> payloadFields = null;
    private Class<T> clazz;

    protected Gson gson;

    /**
     * Creates an instance of {@link PayloadParser}
     *
     * @param clazz the {@link Class} of the parameterised type
     */
    PayloadParser(Class<T> clazz, Gson gson) {
        this.clazz = clazz;
        this.gson = gson;
    }

    /**
     * Gets the set of fields extracted from the payload class
     *
     * @return a {@link Set} of payload field names
     */
    Set<String> getPayloadFields() {
        if (payloadFields == null) {
            payloadFields = extractFieldNames();
        }
        return payloadFields;
    }

    /**
     * Extracts all of the {@link SerializedName} values from the specified {@link Class}
     *
     * @return a {@link Set} of field names expected in the push payload
     */
    @VisibleForTesting
    Set<String> extractFieldNames() {
        Set<String> payloadFields = new HashSet<>();
        for (Field field: clazz.getDeclaredFields()) {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (serializedName != null) {
                payloadFields.add(serializedName.value());
            }
        }
        return payloadFields;
    }
}
