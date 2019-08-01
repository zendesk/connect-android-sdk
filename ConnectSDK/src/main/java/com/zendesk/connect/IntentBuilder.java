package com.zendesk.connect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

import androidx.annotation.VisibleForTesting;

import javax.inject.Inject;

/**
 * Build class to assist in creating {@link Intent} objects and to abstract away Android classes
 * for testing.
 */
class IntentBuilder {

    @VisibleForTesting
    Intent initialIntent;
    @VisibleForTesting
    Intent builderIntent;

    /**
     * Creates an instance of {@link IntentBuilder}
     */
    @Inject
    IntentBuilder() {
        this.initialIntent = new Intent();
        this.builderIntent = initialIntent;
    }

    /**
     * Creates an instance of {@link IntentBuilder} with the given {@link Intent}
     *
     * @param intent an instance of {@link Intent} to be used by the builder
     */
    IntentBuilder(Intent intent) {
        this.initialIntent = intent;
        this.builderIntent = initialIntent;
    }

    /**
     * Returns a new instance of an {@link IntentBuilder}, utilising the given {@link Intent} as its
     * {@link #initialIntent} and {@link #builderIntent}
     *
     * @param intent an instance of {@link Intent} to be used by the builder
     * @return a new instance of {@link IntentBuilder}
     */
    IntentBuilder from(Intent intent) {
        return new IntentBuilder(intent);
    }

    /**
     * Sets the action for this builderIntent
     *
     * @param action the action name
     * @return the builder
     */
    IntentBuilder withAction(String action) {
        builderIntent.setAction(action);
        return this;
    }

    /**
     * Sets the data for this builderIntent
     *
     * @param data the data as a {@link Uri}
     * @return the builder
     */
    IntentBuilder withData(Uri data) {
        builderIntent.setData(data);
        return this;
    }

    /**
     * Parses the string argument as a URI and sets the data for this builderIntent.
     *
     * @param url the data as a {@link String} url
     * @return the builder
     */
    IntentBuilder withData(String url) {
        Uri data = parseUrl(url);
        return withData(data);
    }

    /**
     * Sets the flags for this builderIntent
     *
     * @param flags the flags for the builderIntent
     * @return the builder
     */
    IntentBuilder withFlags(int flags) {
        builderIntent.setFlags(flags);
        return this;
    }

    /**
     * Sets the package name for the builderIntent
     *
     * @param packageName the package name
     * @return the builder
     */
    IntentBuilder withPackageName(String packageName) {
        builderIntent.setPackage(packageName);
        return this;
    }

    /**
     * Sets the class to the launched by this builderIntent
     *
     * @param context an instance of {@link Context}
     * @param className the name of the class to be launched
     * @return the builder
     */
    IntentBuilder withClassName(Context context, String className) {
        builderIntent.setClassName(context, className);
        return this;
    }

    /**
     * Adds a parcelable extra to the builderIntent
     *
     * @param name the key for accessing this extra
     * @param parcelable the {@link Parcelable} to be added
     * @return the builder
     */
    IntentBuilder withExtra(String name, Parcelable parcelable) {
        builderIntent.putExtra(name, parcelable);
        return this;
    }

    /**
     * Builds the {@link Intent} object with the provided parameters and resets the builder intent
     * to be the initial intent for future builder interactions.
     *
     * @return the constructed {@link Intent}
     */
    Intent build() {
        Intent builtIntent = builderIntent;
        builderIntent = initialIntent;
        return builtIntent;
    }

    /**
     * Attempts to parse a string url as a {@link Uri}
     *
     * @param url the string url to parse
     * @return a {@link Uri}, or null if the url couldn't be parsed
     */
    @VisibleForTesting
    Uri parseUrl(String url) {
        if (url == null) {
            return null;
        }
        return Uri.parse(url);
    }
}
