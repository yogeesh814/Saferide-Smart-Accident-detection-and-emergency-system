package com.example.saferide.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;

public final class FirebaseConfig {
    private static Boolean configured;

    private FirebaseConfig() {
    }

    public static boolean isConfigured(Context context) {
        if (configured != null) {
            return configured;
        }

        int appId = context.getResources().getIdentifier("google_app_id", "string", context.getPackageName());
        if (appId == 0) {
            configured = false;
            return false;
        }

        String value = context.getString(appId);
        configured = value != null && !value.trim().isEmpty();
        if (configured && FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        return configured;
    }
}
