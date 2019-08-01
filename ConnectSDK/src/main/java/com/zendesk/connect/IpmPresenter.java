package com.zendesk.connect;

import android.content.Intent;
import android.graphics.Bitmap;

import com.zendesk.logger.Logger;

import javax.inject.Inject;

@ActivityScope
class IpmPresenter implements IpmMvp.Presenter {

    private static final String LOG_TAG = "IpmPresenter";

    private final IpmMvp.View view;
    private final IpmMvp.Model model;
    private final ConnectActionProcessor connectActionProcessor;

    @Inject
    IpmPresenter(IpmMvp.View view,
                 IpmMvp.Model model,
                 ConnectActionProcessor connectActionProcessor) {

        this.view = view;
        this.model = model;
        this.connectActionProcessor = connectActionProcessor;
    }

    @Override
    public void onIpmReceived() {
        IpmPayload ipmPayload = model.getIpm();
        Bitmap avatar = model.getAvatar();

        if (avatar != null) {
            view.displayAvatar(avatar);
        } else {
            Logger.w(LOG_TAG, "Avatar doesn't exist, hiding the UI element");
            view.hideAvatar();
        }

        if (ipmPayload != null) {
            view.displayIpm(ipmPayload);
        } else {
            Logger.w(LOG_TAG, "Couldn't retrieve the IPM, dismissing the UI");
            view.dismissIpm();
        }
    }

    @Override
    public void onAction(String action) {
        model.onAction();

        Intent deepLinkIntent = connectActionProcessor.resolveDeepLinkIntent(action);
        if (deepLinkIntent != null) {
            view.launchActionDeepLink(deepLinkIntent);
        } else {
            view.dismissIpm();
        }
    }

    @Override
    public void onDismiss(IpmDismissType ipmDismissType) {
        model.onDismiss(ipmDismissType);
        view.dismissIpm();
    }
}
