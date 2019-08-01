package com.zendesk.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Class responsible for parsing receive IPM payloads into an instance of {@link IpmPayload}
 */
@ConnectScope
class IpmPayloadParser extends PayloadParser<IpmPayload> {

    /**
     * Creates an instance of {@link IpmPayloadParser}
     */
    @Inject
    IpmPayloadParser(Gson gson) {
        super(IpmPayload.class, gson);
    }

    /**
     * Parse the given map of push data into an instance of {@link IpmPayload}
     *
     * @param data the {@link Map} received in the push payload
     * @return an instance of {@link IpmPayload}, or null if the payload couldn't be parsed
     */
    @Nullable
    IpmPayload parse(@NonNull final Map<String, String> data) {
        JsonObject jsonObject = new JsonObject();
        Map<String, String> dataCopy = new HashMap<>(data);

        for (String fieldName: getPayloadFields()) {
            jsonObject.addProperty(fieldName, dataCopy.get(fieldName));
            dataCopy.remove(fieldName);
        }

        try {
            jsonObject.get(ConnectNotification.Keys.TTL.getKey()).getAsLong();
            return gson.fromJson(jsonObject, IpmPayload.class);

        } catch (UnsupportedOperationException | NumberFormatException exception) {
            return null;
        }
    }
}
