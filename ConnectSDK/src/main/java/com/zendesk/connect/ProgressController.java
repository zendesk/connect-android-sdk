package com.zendesk.connect;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

interface ProgressController {
    void setText(@StringRes int textRes);

    void showIndicator(boolean showIndicator);

    void showText(boolean showText);

    void setTextColor(@ColorRes int color);

    void show(boolean show);
}
