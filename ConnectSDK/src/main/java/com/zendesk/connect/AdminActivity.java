package com.zendesk.connect;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.zendesk.logger.Logger;
import com.zendesk.util.StringUtils;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends Activity implements AdminController {

    private static final String LOG_TAG = "AdminActivity";

    private PinController pinController;
    private ProgressController progressController;
    private Handler handler;

    private static final int SUCCESS_TIME_DELAY = (int) TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
    private static final int FAILURE_TIME_DELAY = (int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        pinController = findViewById(R.id.pinController);
        progressController = findViewById(R.id.progressViewController);

        progressController.showIndicator(false);
        progressController.setText(R.string.connect_admin_pairing_prompt);

        handler = new Handler();
    }

    @Override
    public void onPin(String pin) {
        ConnectComponent connectComponent = Connect.INSTANCE.getComponent();
        if (connectComponent == null) {
            Logger.e(LOG_TAG, "Connect has not been initialised, can't send request");
            pairingFailed();
            return;
        }

        User user = Connect.INSTANCE.getUser();
        if (user == null) {
            Logger.e(LOG_TAG, "No stored user");
            pairingFailed();
            return;
        }

        if (user.getFcm() == null || StringUtils.isEmpty(user.getFcm().get(0))) {
            Logger.e(LOG_TAG, "User has no FCM token");
            pairingFailed();
            return;
        }

        progressController.setText(R.string.connect_admin_pairing_status);

        PairDevice pairDeviceBody = new PairDevice(Integer.parseInt(pin),
                user.getFcm().get(0),
                Build.MANUFACTURER + " " + Build.MODEL);

        TestSendProvider testSendProvider = connectComponent.testSendProvider();
        testSendProvider.pairDevice(Connect.CLIENT_PLATFORM, pairDeviceBody).enqueue(
                new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            pairingSucceeded();
                            return;
                        }
                        pairingFailed();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        pairingFailed();
                    }
                }
        );
    }

    private void resetController() {
        progressController.setTextColor(R.color.connect_admin_pin_box_bg);
        progressController.setText(R.string.connect_admin_pairing_prompt);
    }

    /**
     * Displays an error message and resets the inputs upon failure to pair the device. The
     * error will disappear after a delay.
     */
    private void pairingFailed() {
        Logger.e(LOG_TAG, "Failed to pair device!");

        pinController.reset();
        progressController.showIndicator(false);
        progressController.setTextColor(R.color.connect_admin_paring_failure_text);
        progressController.setText(R.string.connect_admin_pairing_fail);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetController();
            }
        }, FAILURE_TIME_DELAY);
    }

    /**
     * Displays a success message upon a successful device pairing request. Returns to
     * the host app after a delay.
     */
    private void pairingSucceeded() {
        Logger.d(LOG_TAG, "Successfully paired!");

        progressController.showIndicator(false);
        progressController.setTextColor(R.color.connect_admin_pairing_success_text);
        progressController.setText(R.string.connect_admin_pairing_success);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish(); // Finishes activity going back to host app
            }
        }, SUCCESS_TIME_DELAY);
    }
}
