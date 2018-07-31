package io.outbound.sdk;

import android.os.Handler;
import android.os.HandlerThread;

class WorkerThread extends HandlerThread {
    protected Handler handler;

    public WorkerThread(String name) {
        super(name);
    }

    public void post(Runnable run) {
        ensureInitialized();
        handler.post(run);
    }

    public void postDelayed(Runnable run, long delay) {
        ensureInitialized();
        handler.postDelayed(run, delay);
    }

    protected void ensureInitialized() {
        if (handler == null) {
            init();
        }
    }

    protected synchronized void init() {
        handler = new Handler(getLooper());
    }
}
