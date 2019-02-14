package com.zendesk.connect;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

/**
 * Build class to assist in creating {@link Intent} objects and to abstract away Android classes
 * for testing.
 */
class IntentBuilder {

    private Intent intent;

    IntentBuilder() {
        this.intent = new Intent();
    }

    IntentBuilder(Intent intent) {
        this.intent = intent;
    }

    /**
     * Sets the action for this intent
     *
     * @param action the action name
     * @return the builder
     */
    IntentBuilder withAction(String action) {
        intent.setAction(action);
        return this;
    }

    /**
     * Sets the data for this intent
     *
     * @param data the data as a {@link Uri}
     * @return the builder
     */
    IntentBuilder withData(Uri data) {
        intent.setData(data);
        return this;
    }

    /**
     * Parses the string argument as a URI and sets the data for this intent.
     *
     * @param url the data as a {@link String} url
     * @return the builder
     */
    IntentBuilder withData(String url) {
        Uri data = parseUrl(url);
        return withData(data);
    }

    /**
     * Sets the flags for this intent
     *
     * @param flags the flags for the intent
     * @return the builder
     */
    IntentBuilder withFlags(int flags) {
        intent.setFlags(flags);
        return this;
    }

    /**
     * Sets the package name for the intent
     *
     * @param packageName the package name
     * @return the builder
     */
    IntentBuilder withPackageName(String packageName) {
        intent.setPackage(packageName);
        return this;
    }

    /**
     * Builds the {@link Intent} object with the provided parameters
     *
     * @return the constructed {@link Intent}
     */
    Intent build() {
        return intent;
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
