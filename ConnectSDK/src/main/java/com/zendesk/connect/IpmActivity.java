package com.zendesk.connect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zendesk.logger.Logger;
import com.zendesk.util.ColorUtils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class IpmActivity extends AppCompatActivity implements IpmMvp.View {

    private static final String LOG_TAG = "IpmActivity";

    private View bottomSheet;
    private View dismissArea;
    private View ipmBackground;
    private List<View> ipmArea;
    private Button actionButton;
    private TextView headingText;
    private TextView messageText;
    private ImageView avatarImage;

    @Inject
    IpmMvp.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.zcn_ipm_slide_in, R.anim.zcn_ipm_no_change);
        setContentView(R.layout.zcn_ipm_basic_activity);

        if (!inject()) {
            Logger.e(LOG_TAG, "Couldn't retrieve Connect component, unable to display IPM");
            finish();
            return;
        }

        bindViews();
        initBottomSheet();

        presenter.onIpmReceived();
    }

    @Override
    public void onBackPressed() {
        presenter.onDismiss(IpmDismissType.NAVIGATE_BACK);
    }

    private boolean inject() {
        final ConnectComponent component = Connect.INSTANCE.getComponent();

        if (component == null) {
            return false;
        }

        component.ipmComponentBuilder()
                .view(this)
                .build()
                .inject(this);

        return true;
    }

    private void bindViews() {
        bottomSheet = findViewById(R.id.zcn_ipm_bottom_sheet);
        dismissArea = findViewById(R.id.zcn_ipm_dismiss_area);
        ipmBackground = findViewById(R.id.zcn_ipm_body_view);
        actionButton = findViewById(R.id.zcn_ipm_action_button);
        headingText = findViewById(R.id.zcn_ipm_heading_text);
        messageText = findViewById(R.id.zcn_ipm_message_text);
        avatarImage = findViewById(R.id.zcn_ipm_avatar_image);
        ipmArea = Arrays.asList(
                ipmBackground,
                avatarImage
        );
    }

    private void initBottomSheet() {
        UiUtils.hideToolbar(this);
        UiUtils.dimStatusBar(this);

        BottomSheetBehavior<View> behaviour = BottomSheetBehavior.from(bottomSheet);
        behaviour.setSkipCollapsed(true);
        behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);

        behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        presenter.onDismiss(IpmDismissType.SLIDE_DOWN);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                // nada
            }
        });

        dismissArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onDismiss(IpmDismissType.TAP_OUTSIDE);
            }
        });

        for (View view : ipmArea) {
            view.setClickable(true);
        }
    }

    private void initActionButton(final String action) {
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAction(action);
            }
        });
    }

    // region IpmMvp.View
    @Override
    public void displayIpm(IpmPayload ipmPayload) {
        headingText.setText(ipmPayload.getHeading());
        Integer headingFontColor = ColorUtils.apiColorToAndroidColor(ipmPayload.getHeadingFontColor());
        if (headingFontColor != null) {
            headingText.setTextColor(headingFontColor);
        }

        messageText.setText(ipmPayload.getMessage());
        Integer messageFontColor = ColorUtils.apiColorToAndroidColor(ipmPayload.getMessageFontColor());
        if (messageFontColor != null) {
            messageText.setTextColor(messageFontColor);
        }

        actionButton.setText(ipmPayload.getButtonText());
        Integer buttonTextColor = ColorUtils.apiColorToAndroidColor(ipmPayload.getButtonTextColor());
        if (buttonTextColor != null) {
            actionButton.setTextColor(buttonTextColor);
        }

        Integer buttonBackgroundColor = ColorUtils.apiColorToAndroidColor(ipmPayload.getButtonBackgroundColor());
        if (buttonBackgroundColor != null) {
            actionButton.getBackground().setColorFilter(buttonBackgroundColor, PorterDuff.Mode.SRC);
        }

        initActionButton(ipmPayload.getAction());

        Integer backgroundColor = ColorUtils.apiColorToAndroidColor(ipmPayload.getBackgroundColor());
        if (backgroundColor != null) {
            Drawable drawable = getResources().getDrawable(R.drawable.zcn_ipm_body_shape);
            drawable.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
            ipmBackground.setBackground(drawable);
        }
    }

    @Override
    public void displayAvatar(Bitmap avatar) {
        avatarImage.setImageBitmap(avatar);
        avatarImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAvatar() {
        avatarImage.setVisibility(View.GONE);
    }

    @Override
    public void dismissIpm() {
        finish();
        overridePendingTransition(R.anim.zcn_ipm_no_change, R.anim.zcn_ipm_slide_out);
    }

    @Override
    public void launchActionDeepLink(Intent intent) {
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.zcn_ipm_slide_in, R.anim.zcn_ipm_no_change);
    }
    // endregion
}
