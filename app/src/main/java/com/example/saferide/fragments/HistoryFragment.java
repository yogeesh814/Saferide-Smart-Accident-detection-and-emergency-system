package com.example.saferide.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saferide.R;
import com.example.saferide.adapters.IncidentAdapter;
import com.example.saferide.models.IncidentModel;
import com.example.saferide.services.FirestoreService;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class HistoryFragment extends Fragment {
    private FirestoreService firestoreService;
    private IncidentAdapter adapter;
    private View emptyState;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        firestoreService = new FirestoreService(requireContext());
        emptyState = view.findViewById(R.id.historyEmptyState);

        RecyclerView recyclerView = view.findViewById(R.id.incidentsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new IncidentAdapter();
        recyclerView.setAdapter(adapter);
        listenIncidents();
        return view;
    }

    private void listenIncidents() {
        registration = firestoreService.listenIncidents(new FirestoreService.IncidentsCallback() {
            @Override
            public void onIncidents(List<IncidentModel> incidents) {
                adapter.submitList(incidents);
                emptyState.setVisibility(incidents.isEmpty() ? View.VISIBLE : View.GONE);
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
