package com.example.saferide.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.saferide.models.UserModel;
import com.example.saferide.utils.Constants;
import com.example.saferide.utils.FirebaseConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    public interface SimpleCallback {
        void onSuccess();

        void onError(String message);
    }

    public interface UserCallback {
        void onUser(UserModel user);

        void onError(String message);
    }

    private final Context context;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final LocalDemoStore demoStore;
    private final boolean firebaseReady;

    public AuthService(Context context) {
        this.context = context.getApplicationContext();
        this.firebaseReady = FirebaseConfig.isConfigured(this.context);
        this.auth = firebaseReady ? FirebaseAuth.getInstance() : null;
        this.db = firebaseReady ? FirebaseFirestore.getInstance() : null;
        this.demoStore = new LocalDemoStore(this.context);
    }

    public boolean isLoggedIn() {
        return firebaseReady ? auth.getCurrentUser() != null : demoStore.isLoggedIn();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseReady ? auth.getCurrentUser() : null;
    }

    public String getCurrentUid() {
        if (!firebaseReady) {
            return demoStore.getUser().getUid();
        }
        FirebaseUser user = auth.getCurrentUser();
        return user == null ? null : user.getUid();
    }

    public String getCurrentEmail() {
        if (!firebaseReady) {
            return demoStore.getEmail();
        }
        FirebaseUser user = auth.getCurrentUser();
        return user == null ? "" : safe(user.getEmail());
    }

    public void signIn(String email, String password, SimpleCallback callback) {
        if (!firebaseReady) {
            demoStore.signIn(email, displayNameFromEmail(email));
            callback.onSuccess();
            return;
        }
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> loadCurrentUser(new UserCallback() {
                    @Override
                    public void onUser(UserModel user) {
                        saveName(nameOrEmailFallback(user.getName(), email));
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String message) {
                        callback.onSuccess();
                    }
                }))
                .addOnFailureListener(e -> callback.onError(readableError(e.getMessage())));
    }

    public void register(String name, String email, String password, SimpleCallback callback) {
        if (!firebaseReady) {
            demoStore.signIn(email, name);
            callback.onSuccess();
            return;
        }
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Registration failed. Please try again.");
                        return;
                    }

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", firebaseUser.getUid());
                    userMap.put("name", name);
                    userMap.put("email", email);
                    userMap.put("createdAt", FieldValue.serverTimestamp());

                    db.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.getUid())
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                saveName(name);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> callback.onError("Account created, but profile save failed."));
                })
                .addOnFailureListener(e -> callback.onError(readableError(e.getMessage())));
    }

    public void loadCurrentUser(UserCallback callback) {
        if (!firebaseReady) {
            callback.onUser(demoStore.getUser());
            return;
        }
        String uid = getCurrentUid();
        if (uid == null) {
            callback.onError("Not signed in.");
            return;
        }
        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    UserModel user = snapshot.toObject(UserModel.class);
                    if (user == null) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        user = new UserModel();
                        user.setUid(uid);
                        user.setEmail(firebaseUser == null ? "" : safe(firebaseUser.getEmail()));
                        user.setName(displayNameFromEmail(user.getEmail()));
                    }
                    user.setName(nameOrEmailFallback(user.getName(), user.getEmail()));
                    saveName(user.getName());
                    callback.onUser(user);
                })
                .addOnFailureListener(e -> callback.onError(readableError(e.getMessage())));
    }

    public void logout() {
        if (firebaseReady) {
            auth.signOut();
        } else {
            demoStore.signOut();
        }
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().remove(Constants.PREF_USER_NAME).apply();
    }

    private void saveName(String name) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(Constants.PREF_USER_NAME, safe(name))
                .apply();
    }

    private String readSavedName(String fallback) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                .getString(Constants.PREF_USER_NAME, fallback);
    }

    private String readableError(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "Something went wrong. Please try again.";
        }
        return raw.replace("The supplied auth credential is incorrect, malformed or has expired.", "Invalid email or password.");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String nameOrEmailFallback(String name, String email) {
        String cleanName = safe(name).trim();
        return cleanName.isEmpty() || "Kiran".equalsIgnoreCase(cleanName)
                ? displayNameFromEmail(email)
                : cleanName;
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
}
