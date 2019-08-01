package com.zendesk.connect;

import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Collection of util methods for handling Android UI
 */
class UiUtils {

    /**
     * Hides the {@link androidx.appcompat.widget.Toolbar} of the specified {@link AppCompatActivity}
     *
     * @param activity the {@link AppCompatActivity} with a toolbar to be hidden
     */
    static void hideToolbar(AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }
    }

    /**
     * Dims the status bar of the specified {@link AppCompatActivity} for devices running
     * {@link android.os.Build.VERSION_CODES#LOLLIPOP} and above.
     *
     * @param activity the {@link AppCompatActivity} with a status bar to be dimmed
     */
    static void dimStatusBar(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int transparent = ContextCompat.getColor(activity, android.R.color.transparent);
            activity.getWindow().setStatusBarColor(transparent);
        }
    }

}
