<<<<<<< HEAD
# SafeRide: Smart Accident Detection and Emergency Response System

Native Android final-year project demo built with Java, XML layouts, Firebase Authentication, Firestore, Google Maps, Fused Location Provider, SensorManager, local notifications, SharedPreferences, and SMS intent.

## What is included

- Splash screen with animated SafeRide logo
- Firebase login and registration
- Dark futuristic glassmorphism dashboard
- Emergency contacts CRUD in Firestore
- Live monitoring with accelerometer and gyroscope impact detection
- 15-second accident alert countdown
- Manual SOS emergency flow
- GPS location sharing and nearby hospital search
- Accident history saved in Firestore
- Profile/settings with impact sensitivity
- Local notification when an emergency alert is prepared or cancelled

## Project structure

```text
app/src/main/java/com/example/saferide/
  activities/
    SplashActivity.java
    AuthActivity.java
    MainActivity.java
    AlertActivity.java
  fragments/
    HomeFragment.java
    MonitorFragment.java
    ContactsFragment.java
    MapFragment.java
    HistoryFragment.java
    ProfileFragment.java
  adapters/
    ContactAdapter.java
    IncidentAdapter.java
  models/
    UserModel.java
    ContactModel.java
    IncidentModel.java
  services/
    AuthService.java
    FirestoreService.java
    LocationService.java
    SensorService.java
    EmergencyService.java
    NotificationService.java
  utils/
    Constants.java
    PermissionHelper.java
```

## Android permissions

The manifest already includes:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

SafeRide uses `Intent.ACTION_SENDTO` with an `smsto:` URI for SMS. This opens the SMS app with the emergency message filled in. Direct background SMS sending can be added later with native code and stricter permission handling.

## Firebase setup

1. Open [Firebase Console](https://console.firebase.google.com/).
2. Create a project named `SafeRide`.
3. Add an Android app with package name:

```text
com.example.saferide
```

4. Download `google-services.json`.
5. Place it here:

```text
app/google-services.json
```

The Gradle file applies the Google Services plugin automatically when this file exists, so the project can still open cleanly before Firebase is configured.

6. In Firebase Authentication, enable **Email/Password** sign-in.
7. In Firestore Database, create a database in test mode for demo use, then tighten rules before production.

## Firestore collections

```text
users
  uid: string
  name: string
  email: string
  createdAt: timestamp

contacts
  uid: string
  name: string
  phone: string
  relation: string
  createdAt: timestamp

incidents
  uid: string
  latitude: number
  longitude: number
  accelerationValue: number
  status: string
  createdAt: timestamp
```

Demo Firestore rules:

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /contacts/{contactId} {
      allow read, write: if request.auth != null
        && request.resource.data.uid == request.auth.uid;
      allow read, delete: if request.auth != null
        && resource.data.uid == request.auth.uid;
    }

    match /incidents/{incidentId} {
      allow read, write: if request.auth != null
        && request.resource.data.uid == request.auth.uid;
      allow read, delete: if request.auth != null
        && resource.data.uid == request.auth.uid;
    }
  }
}
```

For a quick college demo, you can use Firestore test mode temporarily, but do not ship with open rules.

## Google Maps API setup

1. Open [Google Cloud Console](https://console.cloud.google.com/).
2. Select the Firebase project or create/select a Google Cloud project.
3. Enable **Maps SDK for Android**.
4. Create an Android API key.
5. Restrict the key to package name `com.example.saferide` and your SHA-1 certificate.
6. Replace this placeholder:

```xml
<!-- app/src/main/res/values/strings.xml -->
<string name="google_maps_key">TODO: Add your Google Maps API key here</string>
```

Nearby hospitals are opened through Google Maps search using the current GPS position. A full in-app hospital picker can be added later with the Places SDK.

## How to run

1. Install Android Studio.
2. Open this folder as an Android project:

```text
C:\Users\kiran\OneDrive\Documents\New project 2
```

3. Add `app/google-services.json` from Firebase.
4. Add your Google Maps API key in `app/src/main/res/values/strings.xml`.
5. Sync Gradle.
6. Connect an Android phone or start an emulator with Google Play services.
7. Run the `app` configuration.
8. Register a user, add at least one emergency contact, then start live monitoring.

## Demo notes

- For real device demos, grant location, SMS, and notification permissions when prompted.
- The impact threshold defaults to `28`. Lower it in Profile settings for easier tabletop demo triggering.
- SMS opens the messaging app with recipients and message filled in; the user confirms sending.
=======
# Saferide-Smart-Accident-detection-and-emergency-system
>>>>>>> 9ffeab767b533a52ade280378d629e615c96f3bc
