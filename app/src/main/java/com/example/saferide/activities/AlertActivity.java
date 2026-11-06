package com.example.saferide.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saferide.R;
import com.example.saferide.services.AlertSignalService;
import com.example.saferide.services.EmergencyService;
import com.example.saferide.utils.Constants;
import com.example.saferide.utils.PermissionHelper;
import com.google.android.material.button.MaterialButton;

public class AlertActivity extends AppCompatActivity {
    private static final long ALERT_COUNTDOWN_MS = 15_000;

    private TextView countdownText;
    private TextView subtitle;
    private MaterialButton cancelButton;
    private ProgressBar loading;
    private CountDownTimer timer;
    private EmergencyService emergencyService;
    private AlertSignalService alertSignalService;
    private double accelerationValue;
    private boolean manualSos;
    private boolean completed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        getWindow().setStatusBarColor(getColor(R.color.bg_mid));
        getWindow().setNavigationBarColor(getColor(R.color.bg_mid));

        accelerationValue = getIntent().getDoubleExtra(Constants.EXTRA_ACCELERATION, 0.0);
        manualSos = getIntent().getBooleanExtra(Constants.EXTRA_MANUAL, false);
        emergencyService = new EmergencyService(this);
        alertSignalService = new AlertSignalService(this);
        PermissionHelper.requestEmergencyPermissions(this);

        countdownText = findViewById(R.id.countdownText);
        subtitle = findViewById(R.id.alertSubtitle);
        cancelButton = findViewById(R.id.cancelAlertButton);
        loading = findViewById(R.id.alertLoading);

        findViewById(R.id.emergencyPulse).startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
        if (manualSos) {
            subtitle.setText("Manual SOS will send your emergency SMS in 15 seconds.");
        }

        cancelButton.setOnClickListener(v -> cancelAlert());
        alertSignalService.start();
        startCountdown();
    }

    private void startCountdown() {
        timer = new CountDownTimer(ALERT_COUNTDOWN_MS, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = Math.max(1, millisUntilFinished / 1000);
                countdownText.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                countdownText.setText("0");
                sendAlert();
            }
        };
        timer.start();
    }

    private void cancelAlert() {
        if (completed) {
            return;
        }
        completed = true;
        alertSignalService.stop();
        if (timer != null) {
            timer.cancel();
        }
        setBusy(true, "Saving false alert...");
        emergencyService.saveFalseAlert(accelerationValue, new EmergencyService.EmergencyCallback() {
            @Override
            public void onComplete(String message) {
                Toast.makeText(AlertActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AlertActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void sendAlert() {
        if (completed) {
            return;
        }
        completed = true;
        setBusy(true, "Preparing emergency message...");
        emergencyService.sendEmergencyAlert(accelerationValue, manualSos, new EmergencyService.EmergencyCallback() {
            @Override
            public void onComplete(String message) {
                alertSignalService.stop();
                Toast.makeText(AlertActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                alertSignalService.stop();
                Toast.makeText(AlertActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setBusy(boolean busy, String message) {
        loading.setVisibility(busy ? View.VISIBLE : View.GONE);
        cancelButton.setEnabled(!busy);
        subtitle.setText(message);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        if (alertSignalService != null) {
            alertSignalService.stop();
        }
        super.onDestroy();
    }
}
