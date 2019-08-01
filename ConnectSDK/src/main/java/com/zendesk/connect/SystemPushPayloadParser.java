package com.zendesk.connect;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import static com.zendesk.connect.ConnectNotification.Keys.PAYLOAD;

/**
 * Class responsible for parsing receive push payloads into an instance of {@link SystemPushPayload}
 */
@ConnectScope
class SystemPushPayloadParser extends PayloadParser<SystemPushPayload> {

    /**
     * Creates an instance of {@link SystemPushPayloadParser}
     */
    @Inject
    SystemPushPayloadParser(Gson gson) {
        super(SystemPushPayload.class, gson);
    }

    /**
     * Parses the given {@link Map} of data received in a push payload into an instance of
     * {@link SystemPushPayload}.
     *
     * @param data the {@link Map} received in the push payload
     * @return an instance of {@link SystemPushPayload}
     */
    SystemPushPayload parse(@NonNull final Map<String, String> data) {
        JsonObject jsonObject = new JsonObject();
        Map<String, String> dataCopy = new HashMap<>(data);

        // Use the serialized names to extract and remove connect values out from the message payload
        for (String fieldName: getPayloadFields()) {
            if (data.containsKey(fieldName)) {
                jsonObject.addProperty(fieldName, dataCopy.get(fieldName));
                dataCopy.remove(fieldName);
            }
        }

        // Put the remaining values into a map of custom fields for the integrator to use
        Map<String, Object> customProperties = new HashMap<>();
        for (String key: dataCopy.keySet()) {
            Object value = dataCopy.get(key);
            if (value != null) {
                customProperties.put(key, value);
            }
        }

        // Add the custom fields as a json tree so Gson can parse it as a map
        jsonObject.add(PAYLOAD.getKey(), gson.toJsonTree(customProperties));

        return gson.fromJson(jsonObject, SystemPushPayload.class);
    }

}
