package com.zendesk.connect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.zendesk.logger.Logger;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

/**
 * Utility class to transform {@link Bitmap}s.
 */
class BitmapTransformer {

    private static final String LOG_TAG = "BitmapTransformer";

    @Inject
    BitmapTransformer() {
    }

    /**
     * Transforms the given input stream into a rounded {@link Bitmap}. If the source image does not
     * have a square aspect ratio then a square will be obtained from center of its biggest size.
     *
     * @param in the {@link InputStream} to be transformed
     * @param context a {@link Context} to get resources from
     * @return a rounded {@link Bitmap}, or null if the received InputStream could not be decoded
     */
    @Nullable
    Bitmap toRoundedBitmap(InputStream in, Context context) {
        Bitmap source = BitmapFactory.decodeStream(in);

        try {
            in.close();
        } catch (IOException exception) {
            Logger.d(LOG_TAG, "InputStream failed to close after decoding");
        }

        if (source == null) {
            Logger.w(LOG_TAG, "InputStream failed to decode");
            return null;
        }

        int diameter = Math.min(source.getWidth(), source.getHeight());
        float radius = diameter / 2f;

        Bitmap target = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);

        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(radius, radius, radius, paint);

        int horizontalOffset = 0;
        int verticalOffset = 0;

        if (source.getWidth() > source.getHeight()) {
            horizontalOffset = (source.getWidth() - diameter) / 2;
        } else if (source.getHeight() > source.getWidth()) {
            verticalOffset = (source.getHeight() - diameter) / 2;
        }

        final Rect sourceRect = new Rect(horizontalOffset,
                verticalOffset,
                diameter + horizontalOffset,
                diameter + verticalOffset);
        final Rect destRect = new Rect(0, 0, diameter, diameter);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, sourceRect, destRect, paint);

        RoundedBitmapDrawable output = RoundedBitmapDrawableFactory.create(context.getResources(),
                target);
        output.setCircular(true);

        return output.getBitmap();
    }
}
