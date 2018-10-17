package io.outbound.sdk;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.gson.Gson;
import com.zendesk.connect.BaseQueue;
import com.zendesk.connect.Connect;
import com.zendesk.connect.Tls1Dot2SocketFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class RequestHandler extends WorkerThread {
    private enum Status {
        RETRY, DONE, SUCCESS
    }

    private static String TAG = BuildConfig.APPLICATION_ID;

    private String apiKey;
    private Application app;
    private OkHttpClient httpClient;
    private BaseQueue<String> queue;
    private Gson gson;
    private boolean ready = false;
    private boolean connected = true;

    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean processingScheduled = new AtomicBoolean(false);

    public RequestHandler(String name, Application app, String apiKey, Gson gson) {
        super(name);

        this.apiKey = apiKey;
        this.httpClient = Tls1Dot2SocketFactory
                .enableTls1Dot2OnPreLollipop(new OkHttpClient.Builder()).build();
        this.app = app;
        this.queue = Connect.INSTANCE.outboundQueue();
        this.gson = gson;
    }

    /**
     * If the SDK was initialised using initForTesting then this constructor will be used to add
     * an interceptor allowing us to redirect requests to a mock web server url.
     *
     * @param name name for the handler
     * @param app the host application
     * @param apiKey Connect private api key
     * @param gson an instance of Gson for serialization
     * @param testClient OkHttpClient for testing
     */
    @VisibleForTesting
    RequestHandler(String name, Application app, String apiKey, Gson gson, OkHttpClient testClient) {
        this(name, app, apiKey, gson);
        this.httpClient = testClient;
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
        queue.add(gson.toJson(request));
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
        ConnectivityManager manager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null && manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isConnectedOrConnecting() && ready;
        }
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
            /*
            * We get a snapshot of the outboundQueue size because we will be adding
            * requests back into the outboundQueue if a retry is needed and we don't
            * want to be looping forever if a request constantly needs to
            * retried at a given moment in time (e.g. bad network connection)
            * */
            int queueSize = queue.size();
            for (int i = 0; i < queueSize; i++) {
                if (!canProcess()) {
                    break;
                }

                /*
                * We are peeking and removing items 1 at a time here just because of
                * how the existing logic for Outbound SDK works. It isn't aware of the
                * batching endpoints and refactoring to allow them would leave a lot of
                * room for error without tests.
                * */
                OutboundRequest request = gson
                        .fromJson(queue.peek(1).get(0), OutboundRequest.class);

                queue.remove(1);

                if (request != null) {
                    request.incAttempts();
                    if (sendQueuedRequest(request) == Status.RETRY) {
                        queue.add(gson.toJson(request));
                    }
                }
            }
            processing.set(false);

            if (queue.size() > 0) {
                schedule();
            }
        }
    }
}
