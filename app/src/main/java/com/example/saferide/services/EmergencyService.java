package com.example.saferide.services;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

import com.example.saferide.models.ContactModel;
import com.example.saferide.utils.Constants;
import com.example.saferide.utils.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

public class EmergencyService {
    private static final long CONTACT_CALL_DELAY_MS = 25_000;

    public interface EmergencyCallback {
        void onComplete(String message);

        void onError(String message);
    }

    private final Activity activity;
    private final FirestoreService firestoreService;
    private final LocationService locationService;
    private final NotificationService notificationService;

    public EmergencyService(Activity activity) {
        this.activity = activity;
        this.firestoreService = new FirestoreService(activity);
        this.locationService = new LocationService(activity);
        this.notificationService = new NotificationService(activity);
    }

    public void sendEmergencyAlert(double accelerationValue, boolean manualSos, EmergencyCallback callback) {
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocation(Location location) {
                saveIncidentAndSendSms(location.getLatitude(), location.getLongitude(), accelerationValue, manualSos, callback);
            }

            @Override
            public void onError(String message) {
                saveIncidentAndSendSms(0, 0, accelerationValue, manualSos, callback);
            }
        });
    }

    public void saveFalseAlert(double accelerationValue, EmergencyCallback callback) {
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocation(Location location) {
                saveFalseAlertAt(location.getLatitude(), location.getLongitude(), accelerationValue, callback);
            }

            @Override
            public void onError(String message) {
                saveFalseAlertAt(0, 0, accelerationValue, callback);
            }
        });
    }

    private void saveFalseAlertAt(double latitude, double longitude, double accelerationValue, EmergencyCallback callback) {
        firestoreService.addIncident(latitude, longitude, accelerationValue, Constants.STATUS_FALSE_ALERT, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                notificationService.showEmergencyNotification("SafeRide alert cancelled", "False alert saved in history.");
                callback.onComplete("False alert saved.");
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void saveIncidentAndSendSms(double latitude, double longitude, double accelerationValue, boolean manualSos, EmergencyCallback callback) {
        String status = manualSos ? Constants.STATUS_MANUAL_SOS : Constants.STATUS_SENT;
        firestoreService.addIncident(latitude, longitude, accelerationValue, status, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                notificationService.showEmergencyNotification("Emergency alert prepared", "Sending SMS to trusted contacts.");
                loadContactsAndSendSms(latitude, longitude, accelerationValue, manualSos, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void loadContactsAndSendSms(double latitude, double longitude, double accelerationValue, boolean manualSos, EmergencyCallback callback) {
        firestoreService.getContactsOnce(new FirestoreService.ContactsCallback() {
            @Override
            public void onContacts(List<ContactModel> contacts) {
                List<String> phones = new ArrayList<>();
                for (ContactModel contact : contacts) {
                    if (contact.getPhone() != null && !contact.getPhone().trim().isEmpty()) {
                        phones.add(contact.getPhone().trim());
                    }
                }
                if (phones.isEmpty()) {
                    callback.onError("Incident saved, but no emergency contacts were found.");
                    return;
                }

                String locationText = latitude == 0 && longitude == 0
                        ? "Location unavailable"
                        : LocationService.mapsLink(latitude, longitude);
                String hospitalText = latitude == 0 && longitude == 0
                        ? "Nearest hospital search unavailable without location"
                        : nearbyHospitalLink(latitude, longitude);

                String sms = "SafeRide Emergency Alert!\n"
                        + (manualSos ? "Manual SOS was triggered." : "Possible accident detected.")
                        + "\nImpact value: " + String.format("%.1f", accelerationValue)
                        + "\nLocation: " + locationText
                        + "\nNearby hospitals: " + hospitalText
                        + "\nPlease contact me immediately.";

                sendSmsAutomatically(phones, sms, new EmergencyCallback() {
                    @Override
                    public void onComplete(String message) {
                        String callMessage = callEmergencyContacts(phones);
                        callback.onComplete(message + " " + callMessage);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void sendSmsAutomatically(List<String> phones, String sms, EmergencyCallback callback) {
        if (!PermissionHelper.hasSmsPermission(activity)) {
            openSmsIntent(phones, sms, callback);
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(sms);
            for (String phone : phones) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            }
            callback.onComplete("Emergency SMS sent automatically to " + phones.size() + " contact(s).");
        } catch (Exception error) {
            openSmsIntent(phones, sms, callback);
        }
    }

    private void openSmsIntent(List<String> phones, String sms, EmergencyCallback callback) {
        // Fallback only: used if SMS permission is denied or automatic sending fails.
        Uri smsUri = Uri.parse("smsto:" + TextUtils.join(";", phones));
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
        intent.putExtra("sms_body", sms);
        try {
            activity.startActivity(intent);
            callback.onComplete("SMS permission missing, so the SMS app was opened.");
        } catch (ActivityNotFoundException error) {
            callback.onError("No SMS app found on this phone.");
        }
    }

    private String callEmergencyContacts(List<String> phones) {
        if (phones.isEmpty()) {
            return "No emergency contact number found to call.";
        }

        Handler handler = new Handler(Looper.getMainLooper());
        for (int index = 0; index < phones.size(); index++) {
            String phone = phones.get(index);
            long delay = CONTACT_CALL_DELAY_MS * index;
            handler.postDelayed(() -> callContact(phone), delay);
        }

        if (phones.size() == 1) {
            return "Calling emergency contact.";
        }
        return "Calling all emergency contacts one by one.";
    }

    private String callContact(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Contact number is empty.";
        }

        Uri phoneUri = Uri.parse("tel:" + Uri.encode(phone.trim()));
        Intent intent = new Intent(Intent.ACTION_CALL, phoneUri);
        String result = "Calling primary contact.";
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            intent = new Intent(Intent.ACTION_DIAL, phoneUri);
            result = "Phone permission is missing, so the dialer was opened instead.";
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(intent);
            return result;
        } catch (ActivityNotFoundException ignored) {
            return "No phone app was found for calling.";
        }
    }

    private String nearbyHospitalLink(double latitude, double longitude) {
        return "https://www.google.com/maps/search/hospitals/@" + latitude + "," + longitude + ",14z";
    }
}
