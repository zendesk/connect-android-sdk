package com.zendesk.connect;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.InputStream;

import javax.inject.Inject;

/**
 * Simple repository for storing {@link IpmPayload} and its fetched avatar using
 * {@link FileStorage}
 */
@ConnectScope
class IpmRepository {

    @VisibleForTesting
    static final String AVATAR_FILE_NAME = "zcn_ipm_avatar";
    @VisibleForTesting
    static final String IPM_FILE_NAME = "zcn_ipm_model";

    @Nullable
    @VisibleForTesting
    IpmPayload ipmPayload = null;
    @Nullable
    @VisibleForTesting
    Bitmap avatarImage = null;

    private final FileStorage fileStorage;
    private final Context context;
    private final BitmapTransformer bitmapTransformer;

    @Inject
    IpmRepository(FileStorage fileStorage, Context context, BitmapTransformer bitmapTransformer) {
        this.fileStorage = fileStorage;
        this.context = context;
        this.bitmapTransformer = bitmapTransformer;
    }

    /**
     * Reads from the {@link FileStorage} to get the stored {@link IpmPayload}. The result is saved
     * in memory for faster future calls.
     *
     * @return the stored {@link IpmPayload}, or null if nothing is stored
     */
    @Nullable
    IpmPayload getIpmPayload() {
        if (ipmPayload == null) {
            ipmPayload = (IpmPayload) fileStorage.getFileAsObject(IPM_FILE_NAME);
        }
        return ipmPayload;
    }

    /**
     * Saves the given {@link IpmPayload} in the repository, or deletes any saved files if
     * ipmPayload is null
     *
     * @param ipmPayload an instance of {@link IpmPayload}
     */
    void setIpmPayload(@Nullable IpmPayload ipmPayload) {
        if (ipmPayload != null) {
            fileStorage.saveToFile(ipmPayload, IPM_FILE_NAME);
        } else {
            fileStorage.deleteFile(IPM_FILE_NAME);
        }
        this.ipmPayload = null;
    }

    /**
     * Reads from the {@link FileStorage} to get the stored {@link InputStream} of the avatar image,
     * then transforms it into a {@link Bitmap}. The result is saved in memory for faster future calls.
     *
     * @return the stored avatar {@link Bitmap}, or null if nothing is stored
     */
    @Nullable
    Bitmap getAvatarImage() {
        if (avatarImage == null) {
            InputStream avatarInputStream = fileStorage.getFileAsInputStream(AVATAR_FILE_NAME);
            if (avatarInputStream == null) {
                return  null;
            }

            avatarImage = bitmapTransformer.toRoundedBitmap(avatarInputStream, context);
        }

        return avatarImage;
    }

    /**
     * Saves the given {@link InputStream} of the avatar image in the repository, or deletes any
     * saved files if avatarImage is null
     *
     * @param avatarImage an instance of {@link InputStream} of the avatar image
     */
    void setAvatarImage(@Nullable InputStream avatarImage) {
        if (avatarImage != null) {
            fileStorage.saveToFile(avatarImage, AVATAR_FILE_NAME);
        } else {
            fileStorage.deleteFile(AVATAR_FILE_NAME);
        }
        this.avatarImage = null;
    }

    /**
     * Calls {@link #getIpmPayload()} and {@link #getAvatarImage()} to save the results in memory
     * for cheaper future calls, enabling synchronous work without the expensive operations being
     * done every time while the values are still cached in memory.
     */
    void warmUp() {
        getIpmPayload();
        getAvatarImage();
    }

    /**
     * Deletes any stored IPM from the repository
     */
    void clear() {
        fileStorage.deleteFile(IPM_FILE_NAME);
        fileStorage.deleteFile(AVATAR_FILE_NAME);
        ipmPayload = null;
        avatarImage = null;
    }

}
