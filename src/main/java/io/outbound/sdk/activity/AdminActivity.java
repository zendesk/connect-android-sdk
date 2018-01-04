package io.outbound.sdk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import java.util.concurrent.TimeUnit;

import io.outbound.sdk.Outbound;
import io.outbound.sdk.R;
import io.outbound.sdk.view.PinController;
import io.outbound.sdk.view.ProgressController;

public class AdminActivity extends Activity implements AdminController {

    public static final long SUCCESS_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(1);
    public static final long ERROR_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(5);
    public static final long VIBRATE_MILLIS = 500;
    private Vibrator vibrator;

    private PinController pinController;
    private ProgressController progressController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        pinController = findViewById(R.id.pinController);
        progressController = findViewById(R.id.progressViewController);

        progressController.showIndicator(false);
        progressController.setText(R.string.outbound_admin_pairing_prompt);
    }

    @Override public void onPin(final String pin) {
        new AsyncTask<String, Void, Boolean>() {
            @Override protected Boolean doInBackground(String... params) {
                return Outbound.pairDevice(pin);
            }

            @SuppressLint("ResourceAsColor")
            @Override protected void onPreExecute() {
                super.onPreExecute();

                progressController.setText(R.string.outbound_admin_pairing_status);
                progressController.setTextColor(R.color.outbound_admin_pin_box_bg);
                progressController.show(true);
            }

            @SuppressLint("ResourceAsColor")
            @Override protected void onPostExecute(Boolean success) {
                progressController.showIndicator(false);
                progressController.showText(true);

                if (success) {
                    progressController.setTextColor(R.color.outbound_admin_pairing_success_text);
                    progressController.setText(R.string.outbound_admin_pairing_success);
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            finish(); // Finishes activity going back to host app
                        }
                    }, SUCCESS_DELAY_MILLIS);

                } else {
                    pinController.reset();
                    progressController.setTextColor(R.color.outbound_admin_paring_failure_text);
                    progressController.setText(R.string.outbound_admin_pairing_fail);

                    vibrator.vibrate(VIBRATE_MILLIS);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressController.show(false);
                        }
                    }, ERROR_DELAY_MILLIS);
                }
            }
        }.execute(pin);
    }
}
