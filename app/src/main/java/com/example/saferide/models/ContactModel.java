package com.example.saferide.models;

import com.google.firebase.Timestamp;

public class ContactModel {
    private String id;
    private String uid;
    private String name;
    private String phone;
    private String relation;
    private Timestamp createdAt;

    public ContactModel() {
    }

    public ContactModel(String uid, String name, String phone, String relation) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.relation = relation;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
