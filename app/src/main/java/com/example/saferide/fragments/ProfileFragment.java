package com.example.saferide.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.saferide.R;
import com.example.saferide.activities.AuthActivity;
import com.example.saferide.models.UserModel;
import com.example.saferide.services.AuthService;
import com.example.saferide.utils.Constants;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {
    private static final int MIN_THRESHOLD = 20;
    private static final int MAX_THRESHOLD = 300;

    private TextView profileName;
    private TextView profileEmail;
    private TextView sensitivityValue;
    private SwitchMaterial darkModeSwitch;
    private SeekBar sensitivitySeekBar;
    private AuthService authService;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        authService = new AuthService(requireContext());
        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        sensitivityValue = view.findViewById(R.id.sensitivityValue);
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        sensitivitySeekBar = view.findViewById(R.id.sensitivitySeekBar);

        setupProfile();
        setupSettings();
        view.findViewById(R.id.logoutButton).setOnClickListener(v -> logout());
        return view;
    }

    private void setupProfile() {
        profileName.setText(prefs.getString(Constants.PREF_USER_NAME, "Rider"));
        profileEmail.setText(authService.getCurrentEmail());
        authService.loadCurrentUser(new AuthService.UserCallback() {
            @Override
            public void onUser(UserModel user) {
                if (!isAdded() || user == null) {
                    return;
                }
                profileName.setText(user.getName());
                profileEmail.setText(user.getEmail());
            }

            @Override
            public void onError(String message) {
                // Existing Firebase email fallback remains visible.
            }
        });
    }

    private void setupSettings() {
        darkModeSwitch.setChecked(prefs.getBoolean(Constants.PREF_DARK_MODE, true));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(Constants.PREF_DARK_MODE, isChecked).apply());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sensitivitySeekBar.setMin(MIN_THRESHOLD);
        }
        sensitivitySeekBar.setMax(MAX_THRESHOLD);
        int threshold = clampThreshold(prefs.getInt(Constants.PREF_IMPACT_THRESHOLD, Constants.DEFAULT_IMPACT_THRESHOLD));
        prefs.edit().putInt(Constants.PREF_IMPACT_THRESHOLD, threshold).apply();
        sensitivitySeekBar.setProgress(threshold);
        sensitivityValue.setText(String.valueOf(threshold));
        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = Math.max(MIN_THRESHOLD, progress);
                sensitivityValue.setText(String.valueOf(value));
                prefs.edit().putInt(Constants.PREF_IMPACT_THRESHOLD, value).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void logout() {
        authService.logout();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private int clampThreshold(int value) {
        return Math.max(MIN_THRESHOLD, Math.min(MAX_THRESHOLD, value));
    }
}
