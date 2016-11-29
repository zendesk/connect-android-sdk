package io.outbound.sdk.view;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

/**
 * Created by jophde on 6/22/15 for Outbound.
 */
public interface ProgressController {
    void setText(@StringRes int textRes);

    void showIndicator(boolean showIndicator);

    void showText(boolean showText);

    void setTextColor(@ColorRes int color);

    void show(boolean show);
}
