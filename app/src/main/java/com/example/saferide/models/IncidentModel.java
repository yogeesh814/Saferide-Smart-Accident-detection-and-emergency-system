package com.example.saferide.models;

import com.google.firebase.Timestamp;

public class IncidentModel {
    private String id;
    private String uid;
    private double latitude;
    private double longitude;
    private double accelerationValue;
    private String status;
    private Timestamp createdAt;

    public IncidentModel() {
    }

    public IncidentModel(String uid, double latitude, double longitude, double accelerationValue, String status) {
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accelerationValue = accelerationValue;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccelerationValue() {
        return accelerationValue;
    }

    public void setAccelerationValue(double accelerationValue) {
        this.accelerationValue = accelerationValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
