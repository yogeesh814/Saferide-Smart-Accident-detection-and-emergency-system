package com.example.saferide.services;

import com.example.saferide.models.ContactModel;
import com.example.saferide.models.IncidentModel;
import com.example.saferide.models.UserModel;
import com.example.saferide.utils.Constants;
import com.example.saferide.utils.FirebaseConfig;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {
    public interface SimpleCallback {
        void onSuccess();

        void onError(String message);
    }

    public interface ContactsCallback {
        void onContacts(List<ContactModel> contacts);

        void onError(String message);
    }

    public interface IncidentsCallback {
        void onIncidents(List<IncidentModel> incidents);

        void onError(String message);
    }

    public interface CountCallback {
        void onCount(int count);
    }

    public interface UserCallback {
        void onUser(UserModel user);
    }

    private final android.content.Context context;
    private final boolean firebaseReady;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final LocalDemoStore demoStore;

    public FirestoreService(android.content.Context context) {
        this.context = context.getApplicationContext();
        this.firebaseReady = FirebaseConfig.isConfigured(this.context);
        this.db = firebaseReady ? FirebaseFirestore.getInstance() : null;
        this.auth = firebaseReady ? FirebaseAuth.getInstance() : null;
        this.demoStore = new LocalDemoStore(this.context);
    }

    private String uid() {
        if (!firebaseReady) {
            return demoStore.getUser().getUid();
        }
        return auth.getCurrentUser() == null ? null : auth.getCurrentUser().getUid();
    }

    public void getUser(UserCallback callback) {
        if (!firebaseReady) {
            callback.onUser(demoStore.getUser());
            return;
        }
        String uid = uid();
        if (uid == null) {
            return;
        }
        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> callback.onUser(snapshot.toObject(UserModel.class)));
    }

    public ListenerRegistration listenContacts(ContactsCallback callback) {
        if (!firebaseReady) {
            callback.onContacts(demoStore.getContacts());
            return null;
        }
        String uid = uid();
        if (uid == null) {
            callback.onContacts(new ArrayList<>());
            return null;
        }

        return db.collection(Constants.COLLECTION_CONTACTS)
                .whereEqualTo("uid", uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    List<ContactModel> contacts = new ArrayList<>();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            ContactModel contact = doc.toObject(ContactModel.class);
                            contact.setId(doc.getId());
                            contacts.add(contact);
                        }
                    }
                    Collections.sort(contacts, new TimestampComparator<ContactModel>() {
                        @Override
                        Timestamp timestamp(ContactModel item) {
                            return item.getCreatedAt();
                        }
                    });
                    callback.onContacts(contacts);
                });
    }

    public void getContactsOnce(ContactsCallback callback) {
        if (!firebaseReady) {
            callback.onContacts(demoStore.getContacts());
            return;
        }
        String uid = uid();
        if (uid == null) {
            callback.onContacts(new ArrayList<>());
            return;
        }

        db.collection(Constants.COLLECTION_CONTACTS)
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ContactModel> contacts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        ContactModel contact = doc.toObject(ContactModel.class);
                        contact.setId(doc.getId());
                        contacts.add(contact);
                    }
                    callback.onContacts(contacts);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addContact(String name, String phone, String relation, SimpleCallback callback) {
        if (!firebaseReady) {
            ContactModel contact = new ContactModel(uid(), name, phone, relation);
            contact.setCreatedAt(Timestamp.now());
            demoStore.saveContact(contact);
            callback.onSuccess();
            return;
        }
        String uid = uid();
        if (uid == null) {
            callback.onError("Please sign in again.");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("name", name);
        data.put("phone", phone);
        data.put("relation", relation);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection(Constants.COLLECTION_CONTACTS)
                .add(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateContact(ContactModel contact, SimpleCallback callback) {
        if (!firebaseReady) {
            demoStore.saveContact(contact);
            callback.onSuccess();
            return;
        }
        if (contact.getId() == null) {
            callback.onError("Missing contact id.");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", contact.getName());
        data.put("phone", contact.getPhone());
        data.put("relation", contact.getRelation());

        db.collection(Constants.COLLECTION_CONTACTS)
                .document(contact.getId())
                .update(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteContact(String contactId, SimpleCallback callback) {
        if (!firebaseReady) {
            demoStore.deleteContact(contactId);
            callback.onSuccess();
            return;
        }
        db.collection(Constants.COLLECTION_CONTACTS)
                .document(contactId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public ListenerRegistration listenIncidents(IncidentsCallback callback) {
        if (!firebaseReady) {
            callback.onIncidents(demoStore.getIncidents());
            return null;
        }
        String uid = uid();
        if (uid == null) {
            callback.onIncidents(new ArrayList<>());
            return null;
        }

        return db.collection(Constants.COLLECTION_INCIDENTS)
                .whereEqualTo("uid", uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    List<IncidentModel> incidents = new ArrayList<>();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            IncidentModel incident = doc.toObject(IncidentModel.class);
                            incident.setId(doc.getId());
                            incidents.add(incident);
                        }
                    }
                    Collections.sort(incidents, new TimestampComparator<IncidentModel>() {
                        @Override
                        Timestamp timestamp(IncidentModel item) {
                            return item.getCreatedAt();
                        }
                    });
                    callback.onIncidents(incidents);
                });
    }

    public void addIncident(double latitude, double longitude, double accelerationValue, String status, SimpleCallback callback) {
        if (!firebaseReady) {
            demoStore.addIncident(latitude, longitude, accelerationValue, status);
            callback.onSuccess();
            return;
        }
        String uid = uid();
        if (uid == null) {
            callback.onError("Please sign in again.");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("accelerationValue", accelerationValue);
        data.put("status", status);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection(Constants.COLLECTION_INCIDENTS)
                .add(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public ListenerRegistration listenContactCount(CountCallback callback) {
        return listenContacts(new ContactsCallback() {
            @Override
            public void onContacts(List<ContactModel> contacts) {
                callback.onCount(contacts.size());
            }

            @Override
            public void onError(String message) {
                callback.onCount(0);
            }
        });
    }

    public ListenerRegistration listenIncidentCount(CountCallback callback) {
        return listenIncidents(new IncidentsCallback() {
            @Override
            public void onIncidents(List<IncidentModel> incidents) {
                callback.onCount(incidents.size());
            }

            @Override
            public void onError(String message) {
                callback.onCount(0);
            }
        });
    }

    private abstract static class TimestampComparator<T> implements Comparator<T> {
        abstract Timestamp timestamp(T item);

        @Override
        public int compare(T left, T right) {
            Timestamp a = timestamp(left);
            Timestamp b = timestamp(right);
            if (a == null && b == null) {
                return 0;
            }
            if (a == null) {
                return 1;
            }
            if (b == null) {
                return -1;
            }
            return b.compareTo(a);
        }
    }
}
