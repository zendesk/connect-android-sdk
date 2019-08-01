package com.zendesk.connect;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import javax.inject.Inject;

@ActivityScope
class IpmModel implements IpmMvp.Model {

    private IpmCoordinator coordinator;

    @Inject
    IpmModel(IpmCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Nullable
    @Override
    public IpmPayload getIpm() {
        return coordinator.getIpm();
    }

    @Nullable
    @Override
    public Bitmap getAvatar() {
        return coordinator.getAvatarImage();
    }

    @Override
    public void onAction() {
        coordinator.handleIpmAction();
    }

    @Override
    public void onDismiss(IpmDismissType ipmDismissType) {
        coordinator.handleIpmDismiss(ipmDismissType);
    }
}
