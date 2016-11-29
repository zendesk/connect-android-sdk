package io.outbound.sdk;

import android.app.Application;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;


class RequestHandler extends WorkerThread {
    private enum Status {
        RETRY, DONE, SUCCESS;
    }

    private static String TAG = BuildConfig.APPLICATION_ID;

    private String apiKey;
    private Application app;
    private OkHttpClient httpClient;
    private RequestStorage storage;
    private boolean ready = false;
    private boolean connected = true;

    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean processingScheduled = new AtomicBoolean(false);

    public RequestHandler(String name, Application app, String apiKey) {
        super(name);

        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
        this.app = app;
        this.storage = new RequestStorage(app);
    }

    public synchronized void setReadyState(boolean ready) {
        boolean wasReady = this.ready;
        this.ready = ready;
        if (!wasReady && this.ready) {
            schedule();
        }
    }

    public synchronized void setConnectionStatus(boolean connected) {
        boolean wasConnected = this.connected;
        this.connected = connected;
        if (!wasConnected && this.connected) {
            schedule();
        }
    }

    public void processAfterDelay(OutboundRequest request, long delayMillis) {
        ensureInitialized();
        request.incAttempts();
        handler.postDelayed(buildRunnableRequest(request), delayMillis);
    }

    public void processNow(OutboundRequest request) {
        ensureInitialized();
        request.incAttempts();
        handler.post(buildRunnableRequest(request));
    }

    public void queue(OutboundRequest request) {
        storage.add(request);
        schedule();
    }

    public Response sendRequest(OutboundRequest request) throws IOException {
        Request.Builder requestBuilder = request.getBuilder();
        requestBuilder.addHeader(OutboundRequest.HEADER_API_KEY, apiKey);
        requestBuilder.addHeader(OutboundRequest.HEADER_CLIENT, "Android/" + BuildConfig.VERSION_NAME);

        Request req = requestBuilder.build();
        return httpClient.newCall(req).execute();
    }

    private boolean canProcess() {
        return connected && ready;
    }

    private Status sendQueuedRequest(OutboundRequest request) {
        Response response;
        try {
            response = sendRequest(request);
        } catch (IOException e) {
            return Status.RETRY;
        }

        if (response.isSuccessful()) {
            try {
                // we have a separate try/catch here even though it is the same exception
                // because it is highly likely we will want to act different depending on what throws
                // the exception. for we have the same response but that will likely change once we've
                // encountered this a few times.
                request.onSuccess(response);
            } catch (IOException e) {
                return Status.RETRY;
            }
            return Status.SUCCESS;
        } else if (response.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            request.onError(response);
            return Status.RETRY;
        } else {
            request.onError(response);
            return Status.DONE;
        }
    }

    private Runnable buildRunnableRequest(final OutboundRequest request) {
        return new Runnable() {
            @Override
            public void run() {
                sendQueuedRequest(request);
            }
        };
    }

    private synchronized void schedule() {
        if (!canProcess()) {
            return;
        }

        if (processing.get()) {
            if (!processingScheduled.getAndSet(true)) {
                // if we're online, we wait 2s. if we're offline, we wait 10s.
                int wait = 2000;
                if (!connected) {
                    wait = 10000;
                }
                Log.i(TAG, "Already processing requests. Scheduling a run for " + (wait / 1000) + " seconds.");
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        process(true);
                    }
                }, wait);
            }
        } else {
            Log.i(TAG, "Processing requests.");
            ensureInitialized();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    process(false);
                }
            });
        }
    }

    private synchronized void process(boolean delayed) {
        if (delayed) {
            processingScheduled.set(false);
        }

        if (!processing.getAndSet(true)) {
            boolean moreInQueue = false;

            JSONArray requests = storage.getRequests();
            for (int i = 0; i < requests.length(); i++) {
                if (!canProcess()) {
                    break;
                }

                OutboundRequest request;
                try {
                    request = (OutboundRequest) requests.get(i);
                } catch (JSONException e) {
                    continue;
                }

                try {
                    storage.remove(request.getId());
                } catch (SQLiteException e) {
                    Log.e(TAG, "Error removing queued request from queue. Skipping to try later.", e);
                    moreInQueue = true;
                    continue;
                }

                request.incAttempts();
                if (sendQueuedRequest(request) == Status.RETRY) {
                    storage.add(request);
                }
            }
            processing.set(false);

            if (moreInQueue) {
                schedule();
            }
        }
    }
}
