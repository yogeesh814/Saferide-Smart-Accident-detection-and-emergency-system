package com.example.saferide.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.saferide.models.ContactModel;
import com.example.saferide.models.IncidentModel;
import com.example.saferide.models.UserModel;
import com.example.saferide.utils.Constants;
import com.google.firebase.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalDemoStore {
    private static final String KEY_DEMO_LOGGED_IN = "demo_logged_in";
    private static final String KEY_DEMO_EMAIL = "demo_email";
    private static final String KEY_DEMO_NAME_PREFIX = "demo_name_";
    private static final String KEY_CONTACTS = "demo_contacts";
    private static final String KEY_INCIDENTS = "demo_incidents";

    private final SharedPreferences prefs;

    public LocalDemoStore(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_DEMO_LOGGED_IN, false);
    }

    public void signIn(String email, String name) {
        String cleanEmail = safe(email);
        String cleanName = safe(name);
        String existingName = prefs.getString(nameKey(cleanEmail), "");
        String finalName = existingName == null || existingName.trim().isEmpty() || "Kiran".equalsIgnoreCase(existingName.trim())
                ? cleanName
                : existingName.trim();
        prefs.edit()
                .putBoolean(KEY_DEMO_LOGGED_IN, true)
                .putString(KEY_DEMO_EMAIL, cleanEmail)
                .putString(nameKey(cleanEmail), finalName)
                .putString(Constants.PREF_USER_NAME, finalName)
                .apply();
    }

    public void signOut() {
        prefs.edit().putBoolean(KEY_DEMO_LOGGED_IN, false).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_DEMO_EMAIL, "demo@saferide.app");
    }

    public UserModel getUser() {
        UserModel user = new UserModel();
        user.setUid(currentUid());
        user.setName(prefs.getString(nameKey(getEmail()), displayNameFromEmail(getEmail())));
        user.setEmail(getEmail());
        user.setCreatedAt(Timestamp.now());
        return user;
    }

    public List<ContactModel> getContacts() {
        List<ContactModel> contacts = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(scopedKey(KEY_CONTACTS), "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                ContactModel contact = new ContactModel();
                contact.setId(object.optString("id"));
                contact.setUid(currentUid());
                contact.setName(object.optString("name"));
                contact.setPhone(object.optString("phone"));
                contact.setRelation(object.optString("relation"));
                contact.setCreatedAt(Timestamp.now());
                contacts.add(contact);
            }
        } catch (Exception ignored) {
        }
        return contacts;
    }

    public void saveContact(ContactModel contact) {
        List<ContactModel> contacts = getContacts();
        if (contact.getId() == null || contact.getId().trim().isEmpty()) {
            contact.setId(UUID.randomUUID().toString());
            contacts.add(contact);
        } else {
            boolean updated = false;
            for (int i = 0; i < contacts.size(); i++) {
                if (contact.getId().equals(contacts.get(i).getId())) {
                    contacts.set(i, contact);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                contacts.add(contact);
            }
        }
        writeContacts(contacts);
    }

    public void deleteContact(String id) {
        List<ContactModel> contacts = getContacts();
        List<ContactModel> remaining = new ArrayList<>();
        for (ContactModel contact : contacts) {
            if (!id.equals(contact.getId())) {
                remaining.add(contact);
            }
        }
        writeContacts(remaining);
    }

    public List<IncidentModel> getIncidents() {
        List<IncidentModel> incidents = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(scopedKey(KEY_INCIDENTS), "[]"));
            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject object = array.getJSONObject(i);
                IncidentModel incident = new IncidentModel();
                incident.setId(object.optString("id"));
                incident.setUid(currentUid());
                incident.setLatitude(object.optDouble("latitude"));
                incident.setLongitude(object.optDouble("longitude"));
                incident.setAccelerationValue(object.optDouble("accelerationValue"));
                incident.setStatus(object.optString("status"));
                incident.setCreatedAt(Timestamp.now());
                incidents.add(incident);
            }
        } catch (Exception ignored) {
        }
        return incidents;
    }

    public void addIncident(double latitude, double longitude, double accelerationValue, String status) {
        try {
            JSONArray array = new JSONArray(prefs.getString(scopedKey(KEY_INCIDENTS), "[]"));
            JSONObject object = new JSONObject();
            object.put("id", UUID.randomUUID().toString());
            object.put("latitude", latitude);
            object.put("longitude", longitude);
            object.put("accelerationValue", accelerationValue);
            object.put("status", status);
            array.put(object);
            prefs.edit().putString(scopedKey(KEY_INCIDENTS), array.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void writeContacts(List<ContactModel> contacts) {
        try {
            JSONArray array = new JSONArray();
            for (ContactModel contact : contacts) {
                JSONObject object = new JSONObject();
                object.put("id", contact.getId());
                object.put("name", contact.getName());
                object.put("phone", contact.getPhone());
                object.put("relation", contact.getRelation());
                array.put(object);
            }
            prefs.edit().putString(scopedKey(KEY_CONTACTS), array.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private String scopedKey(String baseKey) {
        return baseKey + "_" + currentUid();
    }

    private String currentUid() {
        String email = getEmail().trim().toLowerCase();
        return "demo-user-" + email.replaceAll("[^a-z0-9]+", "_");
    }

    private String nameKey(String email) {
        String cleanEmail = safe(email).trim().toLowerCase();
        return KEY_DEMO_NAME_PREFIX + cleanEmail.replaceAll("[^a-z0-9]+", "_");
    }

    private String displayNameFromEmail(String email) {
        String cleanEmail = safe(email).trim();
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return "Rider";
        }
        String localPart = cleanEmail.substring(0, cleanEmail.indexOf('@')).trim();
        if (localPart.isEmpty()) {
            return "Rider";
        }
        String readable = localPart.replaceAll("[._-]+", " ").trim();
        StringBuilder result = new StringBuilder();
        for (String part : readable.split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1).toLowerCase());
            }
            result.append(' ');
        }
        String name = result.toString().trim();
        return name.isEmpty() ? "Rider" : name;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
