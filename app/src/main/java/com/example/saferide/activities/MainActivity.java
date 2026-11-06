package com.example.saferide.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.saferide.R;
import com.example.saferide.fragments.ContactsFragment;
import com.example.saferide.fragments.HistoryFragment;
import com.example.saferide.fragments.HomeFragment;
import com.example.saferide.fragments.MapFragment;
import com.example.saferide.fragments.MonitorFragment;
import com.example.saferide.fragments.ProfileFragment;
import com.example.saferide.services.NotificationService;
import com.example.saferide.utils.PermissionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(getColor(R.color.bg_mid));
        getWindow().setNavigationBarColor(getColor(R.color.bg_mid));

        new NotificationService(this).createChannel();
        PermissionHelper.requestEmergencyPermissions(this);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            showFragment(fragmentForItem(item.getItemId()));
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    public void navigateTo(int navItemId) {
        bottomNavigationView.setSelectedItemId(navItemId);
    }

    private Fragment fragmentForItem(int itemId) {
        if (itemId == R.id.nav_monitor) {
            return new MonitorFragment();
        }
        if (itemId == R.id.nav_contacts) {
            return new ContactsFragment();
        }
        if (itemId == R.id.nav_map) {
            return new MapFragment();
        }
        if (itemId == R.id.nav_profile) {
            return new ProfileFragment();
        }
        return new HomeFragment();
    }

    private void showFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
