package com.example.saferide.utils;

public final class Constants {
    private Constants() {
    }

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CONTACTS = "contacts";
    public static final String COLLECTION_INCIDENTS = "incidents";

    public static final String PREFS_NAME = "saferide_prefs";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_IMPACT_THRESHOLD = "impact_threshold";
    public static final String PREF_MONITORING_ACTIVE = "monitoring_active";

    public static final int DEFAULT_IMPACT_THRESHOLD = 60;
    public static final int REQUEST_LOCATION = 2001;
    public static final int REQUEST_EMERGENCY_PERMISSIONS = 2002;

    public static final String STATUS_SENT = "sent";
    public static final String STATUS_FALSE_ALERT = "false_alert";
    public static final String STATUS_MANUAL_SOS = "manual_sos";

    public static final String EXTRA_ACCELERATION = "extra_acceleration";
    public static final String EXTRA_MANUAL = "extra_manual";

    public static final String NOTIFICATION_CHANNEL_ID = "saferide_emergency_channel";
}
