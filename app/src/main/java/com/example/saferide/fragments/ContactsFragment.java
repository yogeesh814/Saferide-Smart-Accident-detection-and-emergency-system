package com.example.saferide.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saferide.R;
import com.example.saferide.adapters.ContactAdapter;
import com.example.saferide.models.ContactModel;
import com.example.saferide.services.FirestoreService;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ContactsFragment extends Fragment {
    private FirestoreService firestoreService;
    private ContactAdapter adapter;
    private View emptyState;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        firestoreService = new FirestoreService(requireContext());
        emptyState = view.findViewById(R.id.contactsEmptyState);

        RecyclerView recyclerView = view.findViewById(R.id.contactsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactAdapter(new ContactAdapter.ContactActionListener() {
            @Override
            public void onEdit(ContactModel contact) {
                showContactDialog(contact);
            }

            @Override
            public void onDelete(ContactModel contact) {
                confirmDelete(contact);
            }
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.addContactButton).setOnClickListener(v -> showContactDialog(null));
        listenContacts();
        return view;
    }

    private void listenContacts() {
        registration = firestoreService.listenContacts(new FirestoreService.ContactsCallback() {
            @Override
            public void onContacts(List<ContactModel> contacts) {
                adapter.submitList(contacts);
                emptyState.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showContactDialog(@Nullable ContactModel existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_contact, null, false);
        EditText nameInput = dialogView.findViewById(R.id.dialogNameInput);
        EditText phoneInput = dialogView.findViewById(R.id.dialogPhoneInput);
        EditText relationInput = dialogView.findViewById(R.id.dialogRelationInput);

        if (existing != null) {
            nameInput.setText(existing.getName());
            phoneInput.setText(existing.getPhone());
            relationInput.setText(existing.getRelation());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add contact" : "Edit contact")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(existing == null ? "Add" : "Save", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(R.color.cyan));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(R.color.muted));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                String relation = relationInput.getText().toString().trim();
                if (name.isEmpty() || phone.isEmpty() || relation.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveContact(existing, name, phone, relation, dialog);
            });
        });
        dialog.show();
    }

    private void saveContact(@Nullable ContactModel existing, String name, String phone, String relation, AlertDialog dialog) {
        FirestoreService.SimpleCallback callback = new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                dialog.dismiss();
                Toast.makeText(requireContext(), "Contact saved", Toast.LENGTH_SHORT).show();
                reloadContacts();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        };

        if (existing == null) {
            firestoreService.addContact(name, phone, relation, callback);
        } else {
            existing.setName(name);
            existing.setPhone(phone);
            existing.setRelation(relation);
            firestoreService.updateContact(existing, callback);
        }
    }

    private void confirmDelete(ContactModel contact) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete contact?")
                .setMessage("This person will no longer receive SafeRide emergency messages.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> firestoreService.deleteContact(contact.getId(), new FirestoreService.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Contact deleted", Toast.LENGTH_SHORT).show();
                        reloadContacts();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                }))
                .show();
    }

    private void reloadContacts() {
        firestoreService.getContactsOnce(new FirestoreService.ContactsCallback() {
            @Override
            public void onContacts(List<ContactModel> contacts) {
                adapter.submitList(contacts);
                emptyState.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (registration != null) {
            registration.remove();
        }
        super.onDestroyView();
    }
}
