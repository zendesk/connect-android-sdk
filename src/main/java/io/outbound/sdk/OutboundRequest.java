package io.outbound.sdk;

import android.content.ContentValues;
import android.support.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.UUID;

class OutboundRequest {
    public static final String HEADER_API_KEY = "X-Outbound-Key";
    public static final String HEADER_OUTBOUND_GUID = "X-Outbound-GUID";
    public static final String HEADER_CLIENT = "X-Outbound-Client";

    private static final MediaType JSON = MediaType.parse("application/json");

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";

    private static final String URL = "https://api.outbound.io";
    private static final String ENDPOINT_CONFIG = "/i/config/sdk/android/" + BuildConfig.VERSION_NAME;
    private static final String ENDPOINT_IDENTIFY = "/v2/identify";
    private static final String ENDPOINT_TRACK = "/v2/track";
    private static final String ENDPOINT_REGISTER = "/v2/gcm/register";
    private static final String ENDPOINT_DISABLE = "/v2/gcm/disable";
    private static final String ENDPOINT_RECEIVE = "/i/android/received";
    private static final String ENDPOINT_OPENED = "/i/android/opened";
    private static final String ENDPOINT_TRACKER = "/i/android/uninstall_tracker";
    private static final String ENDPOINT_PAIR = "/i/testsend/push/pair/android";

    private Type request;
    private String payload;
    private int attempts = 0;
    private int id;
    private String guid;

    public enum Type {
        IDENTIFY, TRACK, REGISTER, DISABLE, CONFIG, PAIR,
        RECEIVE, TRACKER, OPEN;

        public String toString() {
            switch (this) {
                case IDENTIFY:
                    return "identify";
                case TRACK:
                    return "track";
                case REGISTER:
                    return "register";
                case DISABLE:
                    return "disable";
                case CONFIG:
                    return "config";
                case PAIR:
                    return "pair";
                case RECEIVE:
                    return "receive";
                case TRACKER:
                    return "tracker";
                case OPEN:
                    return "open";
            }
            // i don't even think it is possibel for this to happen but we have to do something.
            throw new IllegalStateException("Attempting to get string for an unknown request type: " + this.name());
        }

        public static Type fromString(String label) {
            switch (label) {
                case "identify":
                    return IDENTIFY;
                case "track":
                    return TRACK;
                case "register":
                    return REGISTER;
                case "disable":
                    return DISABLE;
                case "config":
                    return CONFIG;
                case "pair":
                    return PAIR;
                case "receive":
                    return RECEIVE;
                case "tracker":
                    return TRACKER;
                case "open":
                    return OPEN;
            }
            throw new IllegalStateException("Attempting to cast an unknown request type: " + label);
        }

    }

    public OutboundRequest(Type req) {
        this.request = req;
    }

    public OutboundRequest(Type req, @Nullable String payload) {
        this.request = req;
        this.payload = payload;
    }

    public OutboundRequest(Type req, @Nullable String payload, int attempts) {
        this.request = req;
        this.payload = payload;
        this.attempts = attempts;

    }

    public OutboundRequest(int id, Type req, String payload, int attempts) {
        this.id = id;
        this.request = req;
        this.payload = payload;
        this.attempts = attempts;
    }

    public OutboundRequest(int id, Type req, String payload, String guid, int attempts) {
        this.id = id;
        this.request = req;
        this.payload = payload;
        this.attempts = attempts;
        this.guid = guid;
    }

    public int getId() {
        return id;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incAttempts() {
        this.attempts++;
    }

    public boolean is(Type type) {
        return this.request == type;
    }

    public ContentValues content() {
        ContentValues contentValues = new ContentValues();
        if (id != 0) {
            contentValues.put(RequestStorage.COLUMN_NAME_ID, id);
        }
        contentValues.put(RequestStorage.COLUMN_NAME_REQUEST, request.toString());
        contentValues.put(RequestStorage.COLUMN_NAME_PAYLOAD, payload);
        contentValues.put(RequestStorage.COLUMN_NAME_ATTEMPTS, attempts);
        contentValues.put(RequestStorage.COLUMN_NAME_GUID, guid);
        return contentValues;
    }

    public String endpoint() {
        switch (request) {
            case IDENTIFY:
                return URL + ENDPOINT_IDENTIFY;
            case TRACK:
                return URL + ENDPOINT_TRACK;
            case REGISTER:
                return URL + ENDPOINT_REGISTER;
            case DISABLE:
                return URL + ENDPOINT_DISABLE;
            case CONFIG:
                return URL + ENDPOINT_CONFIG;
            case PAIR:
                return URL + ENDPOINT_PAIR;
            case RECEIVE:
                return URL + ENDPOINT_RECEIVE;
            case OPEN:
                return URL + ENDPOINT_OPENED;
            case TRACKER:
                return URL + ENDPOINT_TRACKER;
        }

        throw new IllegalStateException("Requested endpoint for unknown request type: " + request.name());
    }

    public String method() {
        switch (request) {
            case IDENTIFY:
            case TRACK:
            case REGISTER:
            case DISABLE:
            case PAIR:
            case RECEIVE:
            case OPEN:
            case TRACKER:
                return METHOD_POST;
            case CONFIG:
                return URL + ENDPOINT_CONFIG;
        }

        throw new IllegalStateException("Requested method for unknown request type: " + request.name());
    }

    public void onError(Response response) {
        if (request == Type.CONFIG) {
            OutboundClient.getInstance().loadConfig(attempts);
        }
    }

    public void onSuccess(Response response) throws IOException {
        switch (request) {
            case CONFIG:
                OutboundClient.getInstance().setConfig(response.body().string());
                break;
        }
    }

    public Request.Builder getBuilder() {
        Request.Builder builder = new Request.Builder();
        builder.url(endpoint());

        RequestBody body = null;
        if (payload != null) {
            body = RequestBody.create(JSON, payload);
        }

        switch (method()) {
            case METHOD_POST:
                builder.post(body);
                break;
            case METHOD_GET:
                builder.get();
        }

        if (guid == null) {
            this.guid = getOutboundGuid();
        }

        builder.addHeader(HEADER_OUTBOUND_GUID, guid);
        return builder;
    }

    String getOutboundGuid() {
        return UUID.randomUUID().toString();
    }
}
