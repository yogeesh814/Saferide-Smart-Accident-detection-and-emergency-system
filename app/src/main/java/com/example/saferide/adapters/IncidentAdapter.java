package com.example.saferide.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saferide.R;
import com.example.saferide.models.IncidentModel;
import com.example.saferide.utils.Constants;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {
    private final List<IncidentModel> incidents = new ArrayList<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public void submitList(List<IncidentModel> newIncidents) {
        incidents.clear();
        incidents.addAll(newIncidents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        IncidentModel incident = incidents.get(position);
        holder.status.setText(statusLabel(incident) + " • " + String.format(Locale.getDefault(), "%.1f m/s²", incident.getAccelerationValue()));
        holder.date.setText(formatDate(incident.getCreatedAt()));
        holder.location.setText(formatLocation(incident));
    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }

    private String statusLabel(IncidentModel incident) {
        String status = incident.getStatus();
        if (Constants.STATUS_FALSE_ALERT.equals(status)) {
            return "False alert";
        }
        if (Constants.STATUS_MANUAL_SOS.equals(status)) {
            return "Manual SOS";
        }
        return "Alert sent";
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "Just now";
        }
        return formatter.format(timestamp.toDate());
    }

    private String formatLocation(IncidentModel incident) {
        if (incident.getLatitude() == 0 && incident.getLongitude() == 0) {
            return "Location unavailable";
        }
        return String.format(Locale.getDefault(), "%.5f, %.5f", incident.getLatitude(), incident.getLongitude());
    }

    static class IncidentViewHolder extends RecyclerView.ViewHolder {
        TextView status;
        TextView date;
        TextView location;

        IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.incidentStatus);
            date = itemView.findViewById(R.id.incidentDate);
            location = itemView.findViewById(R.id.incidentLocation);
        }
    }
}
