package com.example.saferide.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorService implements SensorEventListener {
    private static final double GRAVITY_FILTER_ALPHA = 0.8;

    public interface SensorCallback {
        void onMotionChanged(double currentValue, double peakValue, String state);

        void onImpactDetected(double impactValue);
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor linearAccelerometer;
    private final Sensor gyroscope;
    private final SensorCallback callback;
    private final float[] gravity = new float[3];
    private double threshold;
    private double gyroMagnitude = 0.0;
    private double peakValue = 0.0;
    private boolean monitoring = false;
    private boolean gravityInitialized = false;
    private long lastTriggerTime = 0L;

    public SensorService(Context context, double threshold, SensorCallback callback) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.linearAccelerometer = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.gyroscope = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.threshold = threshold;
        this.callback = callback;
    }

    public boolean hasRequiredSensors() {
        return sensorManager != null && accelerometer != null;
    }

    public void start() {
        if (!hasRequiredSensors()) {
            callback.onMotionChanged(0, peakValue, "Sensor unavailable");
            return;
        }
        monitoring = true;
        peakValue = 0.0;
        gyroMagnitude = 0.0;
        gravityInitialized = false;
        Sensor motionSensor = linearAccelerometer != null ? linearAccelerometer : accelerometer;
        sensorManager.registerListener(this, motionSensor, SensorManager.SENSOR_DELAY_GAME);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        monitoring = false;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!monitoring) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroMagnitude = magnitude(event.values[0], event.values[1], event.values[2]);
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION
                || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double impactValue = event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION
                    ? magnitude(event.values[0], event.values[1], event.values[2])
                    : filteredLinearAcceleration(event.values);
            peakValue = Math.max(peakValue, impactValue);

            String state;
            if (impactValue < 3 && gyroMagnitude < 1.5) {
                state = "Idle";
            } else if (impactValue < threshold * 0.6) {
                state = "Moving";
            } else {
                state = "High motion";
            }
            callback.onMotionChanged(impactValue, peakValue, state);

            long now = System.currentTimeMillis();
            if (impactValue >= threshold && now - lastTriggerTime > 10_000) {
                lastTriggerTime = now;
                callback.onImpactDetected(impactValue);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No special handling needed for the demo.
    }

    private double magnitude(float x, float y, float z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private double filteredLinearAcceleration(float[] values) {
        if (!gravityInitialized) {
            System.arraycopy(values, 0, gravity, 0, gravity.length);
            gravityInitialized = true;
            return 0.0;
        }

        gravity[0] = (float) (GRAVITY_FILTER_ALPHA * gravity[0] + (1 - GRAVITY_FILTER_ALPHA) * values[0]);
        gravity[1] = (float) (GRAVITY_FILTER_ALPHA * gravity[1] + (1 - GRAVITY_FILTER_ALPHA) * values[1]);
        gravity[2] = (float) (GRAVITY_FILTER_ALPHA * gravity[2] + (1 - GRAVITY_FILTER_ALPHA) * values[2]);

        float linearX = values[0] - gravity[0];
        float linearY = values[1] - gravity[1];
        float linearZ = values[2] - gravity[2];
        return magnitude(linearX, linearY, linearZ);
    }
}
