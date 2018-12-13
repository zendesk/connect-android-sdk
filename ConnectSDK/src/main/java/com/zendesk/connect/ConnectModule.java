package com.zendesk.connect;

import android.content.Context;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;

@Module
class ConnectModule {

    private final Context context;

    ConnectModule(Context context) {
        this.context = context;
    }

    /**
     * Provides an instance of the application context
     *
     * @return an instance of {@link Context}
     */
    @Provides
    @ConnectScope
    Context provideApplicationContext() {
        return context.getApplicationContext();
    }

    /**
     * Provides an implementation of the {@link ConnectClient}
     *
     * @return An implementation of {@link ConnectClient}
     */
    @Provides
    @ConnectScope
    ConnectClient provideConnectClient(StorageController storageController,
                                       BaseQueue<User> userQueue,
                                       BaseQueue<Event> eventQueue,
                                       ConnectScheduler scheduler,
                                       PushProvider pushProvider,
                                       ConnectInstanceId instanceId) {
        return new DefaultConnectClient(storageController, userQueue, eventQueue, scheduler, pushProvider, instanceId);
    }

    /**
     * Provides an instance of {@link ConnectScheduler}
     *
     * @return an instance of {@link ConnectScheduler}
     */
    @Provides
    @ConnectScope
    ConnectScheduler provideConnectScheduler() {
        FirebaseJobDispatcher dispatcher =  new FirebaseJobDispatcher(new GooglePlayDriver(context));
        return new ConnectScheduler(dispatcher);
    }

    /**
     * Provides an instance of {@link ConnectInstanceId}
     *
     * @return an instance of {@link ConnectInstanceId}
     */
    @Provides
    @ConnectScope
    ConnectInstanceId provideConnectInstanceId() {
        return new ConnectInstanceId(FirebaseInstanceId.getInstance());
    }

    /**
     * Provides an instance of {@link Gson}
     *
     * @return An instance of {@link Gson}
     */
    @Provides
    @ConnectScope
    Gson provideGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    /**
     * Provides an instance of a {@link GsonConverter} for converting Strings
     *
     * @param gson an instance of {@link Gson}
     * @return as instance of {@link GsonConverter}
     */
    @Provides
    @ConnectScope
    GsonConverter<String> provideGsonConverter(Gson gson) {
        return new GsonConverter<>(gson, String.class);
    }

    /**
     * Provides an instance of a {@link GsonConverter} for converting {@link User} objects
     *
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link GsonConverter}
     */
    @Provides
    @ConnectScope
    GsonConverter<User> provideUserConverter(Gson gson) {
        return new GsonConverter<>(gson, User.class);
    }

    /**
     * Provides an instance of a {@link GsonConverter} for converting {@link Event} objects
     *
     * @param gson an instance of {@link Gson}
     * @return an instance of {@link GsonConverter}
     */
    @Provides
    @ConnectScope
    GsonConverter<Event> provideEventConverter(Gson gson) {
        return new GsonConverter<>(gson, Event.class);
    }

}
