package com.example.saferide.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saferide.R;
import com.example.saferide.activities.AlertActivity;
import com.example.saferide.activities.MainActivity;
import com.example.saferide.models.UserModel;
import com.example.saferide.services.FirestoreService;
import com.example.saferide.utils.Constants;
import com.google.firebase.firestore.ListenerRegistration;

public class HomeFragment extends Fragment {
    private TextView welcomeName;
    private TextView contactCount;
    private TextView incidentCount;
    private FirestoreService firestoreService;
    private ListenerRegistration contactCountRegistration;
    private ListenerRegistration incidentCountRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firestoreService = new FirestoreService(requireContext());

        welcomeName = view.findViewById(R.id.welcomeName);
        contactCount = view.findViewById(R.id.contactCountText);
        incidentCount = view.findViewById(R.id.incidentCountText);

        view.findViewById(R.id.startMonitoringButton).setOnClickListener(v -> navigate(R.id.nav_monitor));
        view.findViewById(R.id.liveMapCard).setOnClickListener(v -> navigate(R.id.nav_map));
        view.findViewById(R.id.historyCard).setOnClickListener(v -> openHistory());
        view.findViewById(R.id.sosButton).setOnClickListener(v -> openSos());

        loadUserName();
        listenCounts();
        return view;
    }

    private void loadUserName() {
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        welcomeName.setText(prefs.getString(Constants.PREF_USER_NAME, "Rider"));

        firestoreService.getUser(new FirestoreService.UserCallback() {
            @Override
            public void onUser(UserModel user) {
                if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
                    welcomeName.setText(user.getName());
                    prefs.edit().putString(Constants.PREF_USER_NAME, user.getName()).apply();
                }
            }
        });
    }

    private void listenCounts() {
        contactCountRegistration = firestoreService.listenContactCount(count -> contactCount.setText(String.valueOf(count)));
        incidentCountRegistration = firestoreService.listenIncidentCount(count -> incidentCount.setText(String.valueOf(count)));
    }

    private void navigate(int itemId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateTo(itemId);
        }
    }

    private void openHistory() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, new HistoryFragment())
                .commit();
    }

    private void openSos() {
        Intent intent = new Intent(requireContext(), AlertActivity.class);
        intent.putExtra(Constants.EXTRA_MANUAL, true);
        intent.putExtra(Constants.EXTRA_ACCELERATION, 0.0);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        if (contactCountRegistration != null) {
            contactCountRegistration.remove();
        }
        if (incidentCountRegistration != null) {
            incidentCountRegistration.remove();
        }
        super.onDestroyView();
    }
}
