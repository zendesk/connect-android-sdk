package android.os;

import androidx.annotation.NonNull;

/**
 * This is a shadow class for {@link AsyncTask} that forces it to run synchronously. It is meant to
 * facilitate testing code that rely on {@link AsyncTask}s since it can now be tested with Unit Tests.
 * <p>
 * It will call {@link AsyncTask#doInBackground} followed immediately by {@link AsyncTask#onPostExecute}.
 */
public abstract class AsyncTask<ParamsT, ProgressT, ResultT> {

    protected abstract ResultT doInBackground(ParamsT... params);

    protected void onPostExecute(ResultT result) {
    }

    protected void onProgressUpdate(ProgressT... values) {
    }

    /**
     * Calls {@link AsyncTask#doInBackground} followed immediately by {@link AsyncTask#onPostExecute}.
     *
     * @param params the parameters to passed to {@link AsyncTask#doInBackground}
     * @return this AsyncTask
     */
    public AsyncTask<ParamsT, ProgressT, ResultT> execute(ParamsT... params) {
        ResultT result = doInBackground(params);
        onPostExecute(result);
        return this;
    }

    /***
     * Calls {@link Runnable#run} immediately.
     *
     * @param runnable the {@link Runnable} to run
     */
    public static void execute(@NonNull Runnable runnable) {
        runnable.run();
    }
}
