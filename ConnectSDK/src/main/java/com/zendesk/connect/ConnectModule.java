package com.zendesk.connect;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.work.WorkManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;

@Module
abstract class ConnectModule {


    /**
     * Provides the application {@link Context}
     *
     * @param application the {@link Application} instance
     * @return the application {@link Context}
     */
    @Provides
    static Context provideApplicationContext(Application application) {
        return application.getApplicationContext();
    }

    /**
     * Provides a {@link PackageManager} instance to find global package information.
     *
     * @param context the application context
     * @return an instance of {@link PackageManager}
     */
    @Provides
    static PackageManager providePackageManager(Context context) {
        return context.getPackageManager();
    }

    /**
     * Provides an implementation of the {@link ConnectClient}
     *
     * @return An implementation of {@link ConnectClient}
     */
    @Provides
    static ConnectClient provideConnectClient(DefaultConnectClient defaultConnectClient) {
        return defaultConnectClient;
    }

    /**
     * Provides an instance of {@link WorkManager}
     *
     * @return an instance of {@link WorkManager}
     */
    @Provides
    @ConnectScope
    static WorkManager provideWorkManager() {
        return WorkManager.getInstance();
    }

    /**
     * Provides an instance of {@link FirebaseInstanceId}
     *
     * @return an instance of {@link FirebaseInstanceId}
     */
    @Provides
    @ConnectScope
    static FirebaseInstanceId provideFirebaseInstanceId() {
        return FirebaseInstanceId.getInstance();
    }

    /**
     * Provides an instance of {@link Gson}
     *
     * @return An instance of {@link Gson}
     */
    @Provides
    @ConnectScope
    static Gson provideGson() {
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
    static GsonConverter<String> provideGsonConverter(Gson gson) {
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
    static GsonConverter<User> provideUserConverter(Gson gson) {
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
    static GsonConverter<Event> provideEventConverter(Gson gson) {
        return new GsonConverter<>(gson, Event.class);
    }

}
