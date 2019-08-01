package com.zendesk.connect;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

/**
 * Describes the MVP contract of the UI for displaying a {@link IpmPayload}.
 */
interface IpmMvp {

    interface View {

        /**
         * <p>
         *     Displays the {@link IpmPayload} on the UI.
         * </p>
         *
         * @param ipmPayload the {@link IpmPayload} to be displayed
         */
        void displayIpm(IpmPayload ipmPayload);

        /**
         * <p>
         *     Displays the Bitmap loaded from {@link IpmPayload#getLogo()} on the UI.
         * </p>
         *
         * @param avatar the Bitmap loaded from {@link IpmPayload#getLogo()}
         */
        void displayAvatar(Bitmap avatar);

        /**
         * <p>
         *     Hides the avatar container on the UI.
         * </p>
         */
        void hideAvatar();

        /**
         * <p>
         *     Dismisses the IPM UI currently displayed to the user.
         * </p>
         */
        void dismissIpm();

        /**
         * <p>
         *     Dismisses the IPM UI currently displayed to the user while starting an activity
         *     with the given intent
         * </p>
         *
         * @param intent the {@link Intent} to start an activity with
         */
        void launchActionDeepLink(Intent intent);
    }

    interface Presenter {

        /**
         * <p>
         *     Invoked when an IPM is received.
         * </p>
         */
        void onIpmReceived();

        /**
         * <p>
         *     Invoked when the action button on the UI is pressed. If the action is empty then the
         *     IPM UI is simply dismissed. If the action is a valid {@link java.net.URI} then it will
         *     be resolved to an {@link Intent}
         * </p>
         *
         * @param action the value of {@link IpmPayload#getAction()}
         */
        void onAction(String action);

        /**
         * <p>
         *     Invoked when the user is attempting to dismiss the IPM UI.
         * </p>
         *
         * @param ipmDismissType the {@link IpmDismissType} that identifies how the UI is being
         * dismissed.
         */
        void onDismiss(IpmDismissType ipmDismissType);
    }

    interface Model {

        /**
         * <p>
         *     Retrieves the current {@link IpmPayload} if available, null otherwise.
         * </p>
         *
         * @return the current {@link IpmPayload} to be displayed if available, null otherwise
         */
        @Nullable
        IpmPayload getIpm();

        /**
         * <p>
         *     Retrieves the current {@link IpmPayload} avatar image if available, null
         *     otherwise.
         * </p>
         *
         * @return the Bitmap for the current {@link IpmPayload} if available, null otherwise
         */
        @Nullable
        Bitmap getAvatar();

        /**
         * <p>
         *     Invoked when the action button on the UI is pressed.
         * </p>
         */
        void onAction();

        /**
         * <p>
         *     Invoked when the user is attempting to dismiss the IPM UI. This also clears any
         *     references to the current {@link IpmPayload} and avatar Bitmap.
         * </p>
         *
         * @param ipmDismissType the {@link IpmDismissType} that identifies how the UI is being
         * dismissed.
         */
        void onDismiss(IpmDismissType ipmDismissType);
    }
}
