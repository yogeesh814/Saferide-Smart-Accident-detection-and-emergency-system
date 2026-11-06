package com.example.saferide.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saferide.R;
import com.example.saferide.services.MonitoringForegroundService;
import com.example.saferide.services.SensorService;
import com.example.saferide.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class MonitorFragment extends Fragment {
    private TextView motionValue;
    private TextView motionState;
    private TextView peakValue;
    private TextView thresholdText;
    private MaterialButton toggleButton;
    private SwitchMaterial voiceSwitch;
    private SharedPreferences prefs;
    private SensorService uiSensorService;
    private int threshold;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        motionValue = view.findViewById(R.id.motionValue);
        motionState = view.findViewById(R.id.motionStateText);
        peakValue = view.findViewById(R.id.peakValueText);
        thresholdText = view.findViewById(R.id.thresholdText);
        toggleButton = view.findViewById(R.id.monitorToggleButton);
        voiceSwitch = view.findViewById(R.id.voiceSwitch);

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        threshold = prefs.getInt(Constants.PREF_IMPACT_THRESHOLD, Constants.DEFAULT_IMPACT_THRESHOLD);
        thresholdText.setText(String.valueOf(threshold));

        toggleButton.setOnClickListener(v -> toggleMonitoring());
        voiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> voiceSwitch.setText(isChecked ? "On" : "Off"));
        updateMonitoringUi(isMonitoringActive());
        if (isMonitoringActive()) {
            startUiSensorPreview();
        }
        return view;
    }

    private void toggleMonitoring() {
        if (isMonitoringActive()) {
            stopMonitoring();
        } else {
            SensorService availabilityCheck = new SensorService(requireContext(), threshold, new SensorService.SensorCallback() {
                @Override
                public void onMotionChanged(double currentValue, double peakValue, String state) {
                }

                @Override
                public void onImpactDetected(double impactValue) {
                }
            });
            if (!availabilityCheck.hasRequiredSensors()) {
                Toast.makeText(requireContext(), "Accelerometer sensor not available on this device.", Toast.LENGTH_LONG).show();
                return;
            }
            startMonitoringService();
            startUiSensorPreview();
            updateMonitoringUi(true);
            Toast.makeText(requireContext(), "Live monitoring will continue in the background.", Toast.LENGTH_LONG).show();
        }
    }

    private void stopMonitoring() {
        Intent intent = new Intent(requireContext(), MonitoringForegroundService.class);
        intent.setAction(MonitoringForegroundService.ACTION_STOP);
        requireContext().startService(intent);
        stopUiSensorPreview();
        updateMonitoringUi(false);
    }

    private void updateMonitoringUi(boolean monitoring) {
        prefs.edit().putBoolean(Constants.PREF_MONITORING_ACTIVE, monitoring).apply();
        toggleButton.setText(monitoring ? "Stop monitoring" : "Start monitoring");
        toggleButton.setIconResource(monitoring ? R.drawable.ic_stop : R.drawable.ic_play);
        motionState.setText(monitoring ? "Monitoring" : "Idle");
        if (!monitoring) {
            motionValue.setText("0.0");
            peakValue.setText("0.0");
        }
    }

    private void startMonitoringService() {
        Intent intent = new Intent(requireContext(), MonitoringForegroundService.class);
        intent.setAction(MonitoringForegroundService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs != null) {
            boolean monitoring = isMonitoringActive();
            updateMonitoringUi(monitoring);
            if (monitoring) {
                startUiSensorPreview();
            }
        }
    }

    private boolean isMonitoringActive() {
        return prefs != null && prefs.getBoolean(Constants.PREF_MONITORING_ACTIVE, false);
    }

    private void startUiSensorPreview() {
        if (uiSensorService != null && uiSensorService.isMonitoring()) {
            return;
        }
        if (uiSensorService != null) {
            uiSensorService.stop();
        }
        uiSensorService = new SensorService(requireContext(), threshold, new SensorService.SensorCallback() {
            @Override
            public void onMotionChanged(double currentValue, double peak, String state) {
                if (!isAdded() || !isMonitoringActive()) {
                    return;
                }
                motionValue.setText(String.format(Locale.getDefault(), "%.1f", currentValue));
                peakValue.setText(String.format(Locale.getDefault(), "%.1f", peak));
                motionState.setText(state);
            }

            @Override
            public void onImpactDetected(double impactValue) {
                // The foreground service owns emergency detection while monitoring is active.
            }
        });
        if (uiSensorService.hasRequiredSensors()) {
            uiSensorService.start();
        }
    }

    private void stopUiSensorPreview() {
        if (uiSensorService != null) {
            uiSensorService.stop();
            uiSensorService = null;
        }
    }

    @Override
    public void onDestroyView() {
        stopUiSensorPreview();
        super.onDestroyView();
    }
}
