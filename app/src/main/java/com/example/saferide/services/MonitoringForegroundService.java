package com.example.saferide.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.saferide.R;
import com.example.saferide.activities.AlertActivity;
import com.example.saferide.activities.MainActivity;
import com.example.saferide.utils.Constants;

public class MonitoringForegroundService extends Service {
    public static final String ACTION_START = "com.example.saferide.action.START_MONITORING";
    public static final String ACTION_STOP = "com.example.saferide.action.STOP_MONITORING";
    private static final int NOTIFICATION_ID = 2001;

    private SensorService sensorService;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        new NotificationService(this).createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopMonitoring();
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification("Monitoring your ride"));
        startMonitoring();
        return START_STICKY;
    }

    private void startMonitoring() {
        if (sensorService != null && sensorService.isMonitoring()) {
            return;
        }

        int threshold = prefs.getInt(Constants.PREF_IMPACT_THRESHOLD, Constants.DEFAULT_IMPACT_THRESHOLD);
        sensorService = new SensorService(this, threshold, new SensorService.SensorCallback() {
            @Override
            public void onMotionChanged(double currentValue, double peakValue, String state) {
                // Background monitoring only needs impact detection.
            }

            @Override
            public void onImpactDetected(double impactValue) {
                handleImpact(impactValue);
            }
        });

        if (!sensorService.hasRequiredSensors()) {
            stopMonitoring();
            stopSelf();
            return;
        }

        sensorService.start();
        prefs.edit().putBoolean(Constants.PREF_MONITORING_ACTIVE, true).apply();
    }

    private void handleImpact(double impactValue) {
        stopMonitoring();
        Intent alertIntent = new Intent(this, AlertActivity.class);
        alertIntent.putExtra(Constants.EXTRA_ACCELERATION, impactValue);
        alertIntent.putExtra(Constants.EXTRA_MANUAL, false);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(alertIntent);
        stopSelf();
    }

    private void stopMonitoring() {
        if (sensorService != null) {
            sensorService.stop();
            sensorService = null;
        }
        prefs.edit().putBoolean(Constants.PREF_MONITORING_ACTIVE, false).apply();
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    private Notification buildNotification(String text) {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, MonitoringForegroundService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_monitor)
                .setContentTitle("SafeRide live monitoring")
                .setContentText(text)
                .setContentIntent(openPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        stopMonitoring();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
