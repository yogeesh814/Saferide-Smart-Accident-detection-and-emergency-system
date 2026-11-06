package com.example.saferide.services;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import com.example.saferide.R;

import java.io.IOException;

public class AlertSignalService {
    private static final long[] VIBRATION_PATTERN = {0, 600, 300, 600, 800};

    private final Context context;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    public AlertSignalService(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        startVibration();
        startAlarm();
    }

    public void stop() {
        stopVibration();
        stopAlarm();
    }

    private void startVibration() {
        vibrator = getVibrator();
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
        } else {
            vibrator.vibrate(VIBRATION_PATTERN, 0);
        }
    }

    private Vibrator getVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return manager == null ? null : manager.getDefaultVibrator();
        }
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void startAlarm() {
        stopAlarm();
        try (AssetFileDescriptor alarmFile = context.getResources().openRawResourceFd(R.raw.alarm)) {
            mediaPlayer = new MediaPlayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            }
            mediaPlayer.setDataSource(alarmFile.getFileDescriptor(), alarmFile.getStartOffset(), alarmFile.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException | RuntimeException error) {
            stopAlarm();
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
