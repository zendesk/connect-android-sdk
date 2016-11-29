package io.outbound.sdk.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.outbound.sdk.R;

/**
 * Created by jophde on 6/22/15 for Outbound.
 */
public class ProgressRelativeLayout extends RelativeLayout implements ProgressController {
    private ProgressBar indicator;
    private TextView text;

    public ProgressRelativeLayout(Context context) {
        super(context);
    }

    public ProgressRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ProgressRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        indicator = (ProgressBar) findViewById(R.id.indicator);
        text = (TextView) findViewById(R.id.text);
    }

    @Override public void setText(@StringRes int textRes) {
        text.setText(textRes);
    }

    @Override public void showIndicator(boolean showIndicator) {
        if (showIndicator) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }
    }

    @Override public void showText(boolean showText) {
        if (showText) {
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }
    }

    @Override public void setTextColor(@ColorRes int color) {
        text.setTextColor(getResources().getColor(color));
    }

    @Override public void show(boolean show) {
        showIndicator(show);
        showText(show);
    }
}
