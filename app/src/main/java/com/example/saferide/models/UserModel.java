package com.example.saferide.models;

import com.google.firebase.Timestamp;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private Timestamp createdAt;

    public UserModel() {
        // Firestore needs an empty constructor.
    }

    public UserModel(String uid, String name, String email, Timestamp createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
