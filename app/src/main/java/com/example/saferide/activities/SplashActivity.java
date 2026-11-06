package com.example.saferide.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saferide.R;
import com.example.saferide.services.AuthService;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        makeFullscreenDark();

        View logo = findViewById(R.id.logoContainer);
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in));

        AuthService authService = new AuthService(this);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> destination = authService.isLoggedIn() ? MainActivity.class : AuthActivity.class;
            startActivity(new Intent(this, destination));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1800);
    }

    private void makeFullscreenDark() {
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.bg_mid));
        window.setNavigationBarColor(getColor(R.color.bg_mid));
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
}
