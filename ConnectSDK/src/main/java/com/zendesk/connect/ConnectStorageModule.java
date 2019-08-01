package com.zendesk.connect;

import android.content.Context;
import android.content.SharedPreferences;

import com.squareup.tape2.ObjectQueue;
import com.squareup.tape2.QueueFile;

import java.io.File;
import java.io.IOException;

import dagger.Module;
import dagger.Provides;

@Module
abstract class ConnectStorageModule {

    private static final String CONNECT_PREFERENCES_NAME = "connect_shared_preferences_storage";
    private static final String CONNECT_STRING_QUEUE_FILE = "connect_string_queue_file";
    private static final String CONNECT_USER_QUEUE_FILE = "connect_user_queue_file";
    private static final String CONNECT_EVENT_QUEUE_FILE = "connect_event_queue_file";

    /**
     * Provides an instance of {@link SharedPreferences}
     *
     * @return An instance of {@link SharedPreferences}
     */
    @Provides
    @ConnectScope
    static SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences(CONNECT_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Provides an implementation of {@link BaseStorage} using {@link SharedPreferences}
     *
     * @param sharedPreferencesStorage an instance of {@link SharedPreferencesStorage}
     * @return An instance of {@link SharedPreferencesStorage}
     */
    @Provides
    static BaseStorage provideSharedPreferencesStorage(SharedPreferencesStorage sharedPreferencesStorage) {
        return sharedPreferencesStorage;
    }

    /**
     * Provides a file backed {@link ObjectQueue} for use in queueing {@link String} objects.
     *
     * If a problem is encountered when creating the file backed queue object then the SDK
     * will fall back on an in-memory queue. This will mean degraded performance as some
     * events may be lost when the app is removed from device memory but should prevent
     * potential app crashes.
     *
     * @param gsonConverter converter used for serializing/deserializing {@link String}s for the queue
     * @return an instance of an {@link ObjectQueue}
     */
    @Provides
    @ConnectScope
    static ObjectQueue<String> provideObjectQueue(GsonConverter<String> gsonConverter, Context context) {
        File file = new File(context.getFilesDir(), CONNECT_STRING_QUEUE_FILE);
        QueueFile queueFile;
        try {
            queueFile = new QueueFile.Builder(file).build();
            return ObjectQueue.create(queueFile, gsonConverter);
        } catch (IOException e) {
            return ObjectQueue.createInMemory();
        }
    }

    /**
     * Provides a file backed {@link ObjectQueue} for use in queueing {@link User} objects.
     *
     * If a problem is encountered when creating the file backed queue object then the SDK
     * will fall back on an in-memory queue. This will mean degraded performance as some
     * events may be lost when the app is removed from device memory but should prevent
     * potential app crashes.
     *
     * @param userConverter converter used for serializing/deserializing {@link User}s for the queue
     * @return an instance of an {@link ObjectQueue}
     */
    @Provides
    @ConnectScope
    static ObjectQueue<User> provideUserObjectQueue(GsonConverter<User> userConverter, Context context) {
        File file = new File(context.getFilesDir(), CONNECT_USER_QUEUE_FILE);
        QueueFile queueFile;
        try {
            queueFile = new QueueFile.Builder(file).build();
            return ObjectQueue.create(queueFile, userConverter);
        } catch (IOException e) {
            return ObjectQueue.createInMemory();
        }
    }

    /**
     * Provides a file backed {@link ObjectQueue} for use in queueing {@link Event} objects.
     *
     * If a problem is encountered when creating the file backed queue object then the SDK
     * will fall back on an in-memory queue. This will mean degraded performance as some
     * events may be lost when the app is removed from device memory but should prevent
     * potential app crashes.
     *
     * @param eventConverter converter used for serializing/deserializing {@link Event}s for the queue
     * @return an instance of an {@link ObjectQueue}
     */
    @Provides
    @ConnectScope
    static ObjectQueue<Event> provideEventObjectQueue(GsonConverter<Event> eventConverter, Context context) {
        File file = new File(context.getFilesDir(), CONNECT_EVENT_QUEUE_FILE);
        QueueFile queueFile;
        try {
            queueFile = new QueueFile.Builder(file).build();
            return ObjectQueue.create(queueFile, eventConverter);
        } catch (IOException e) {
            return ObjectQueue.createInMemory();
        }
    }

    /**
     * Provides a {@link BaseQueue} for queueing {@link String} objects
     *
     * @param stringObjectQueue the {@link ObjectQueue} used by this {@link ConnectQueue}
     * @return an instance of a {@link BaseQueue}
     */
    @Provides
    @ConnectScope
    static BaseQueue<String> provideConnectQueue(ObjectQueue<String> stringObjectQueue) {
        return new ConnectQueue<>(stringObjectQueue);
    }

    /**
     * Provides a {@link BaseQueue} for queueing {@link User} objects
     *
     * @param userObjectQueue the {@link ObjectQueue} used by this {@link ConnectQueue}
     * @return an instance of a {@link BaseQueue}
     */
    @Provides
    @ConnectScope
    static BaseQueue<User> provideUserQueue(ObjectQueue<User> userObjectQueue) {
        return new ConnectQueue<>(userObjectQueue);
    }

    /**
     * Provides a {@link BaseQueue} for queueing {@link Event} objects
     *
     * @param eventObjectQueue the {@link ObjectQueue} used by this {@link ConnectQueue}
     * @return an instance of a {@link BaseQueue}
     */
    @Provides
    @ConnectScope
    static BaseQueue<Event> provideEventQueue(ObjectQueue<Event> eventObjectQueue) {
        return new ConnectQueue<>(eventObjectQueue);
    }

}
