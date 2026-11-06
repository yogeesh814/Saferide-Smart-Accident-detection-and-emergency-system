package com.example.saferide.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saferide.R;
import com.example.saferide.services.AuthService;

public class AuthActivity extends AppCompatActivity {
    private TextView signInTab;
    private TextView registerTab;
    private TextView nameLabel;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView authMessage;
    private TextView authButton;
    private ProgressBar authLoading;
    private boolean registerMode = false;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getWindow().setStatusBarColor(getColor(R.color.bg_mid));
        getWindow().setNavigationBarColor(getColor(R.color.bg_mid));

        authService = new AuthService(this);
        if (authService.isLoggedIn()) {
            openMain();
            return;
        }

        bindViews();
        findViewById(android.R.id.content).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        signInTab.setOnClickListener(v -> setMode(false));
        registerTab.setOnClickListener(v -> setMode(true));
        authButton.setOnClickListener(v -> submit());
        setMode(false);
    }

    private void bindViews() {
        signInTab = findViewById(R.id.signInTab);
        registerTab = findViewById(R.id.registerTab);
        nameLabel = findViewById(R.id.nameLabel);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        authMessage = findViewById(R.id.authMessage);
        authButton = findViewById(R.id.authButton);
        authLoading = findViewById(R.id.authLoading);
    }

    private void setMode(boolean register) {
        registerMode = register;
        signInTab.setBackgroundResource(register ? R.drawable.bg_tab_unselected : R.drawable.bg_tab_selected);
        registerTab.setBackgroundResource(register ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        signInTab.setTextColor(getColor(register ? R.color.muted : R.color.bg_mid));
        registerTab.setTextColor(getColor(register ? R.color.bg_mid : R.color.muted));
        nameLabel.setVisibility(register ? View.VISIBLE : View.GONE);
        nameInput.setVisibility(register ? View.VISIBLE : View.GONE);
        authButton.setText(register ? "Create Account" : "Sign In");
        clearMessage();
    }

    private void submit() {
        clearMessage();
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (registerMode && name.length() < 2) {
            showMessage("Please enter your full name.");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters.");
            return;
        }

        setLoading(true);
        AuthService.SimpleCallback callback = new AuthService.SimpleCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(AuthActivity.this, registerMode ? "Account created" : "Welcome back", Toast.LENGTH_SHORT).show();
                openMain();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                showMessage(message);
            }
        };

        if (registerMode) {
            authService.register(name, email, password, callback);
        } else {
            authService.signIn(email, password, callback);
        }
    }

    private void setLoading(boolean loading) {
        authLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        authButton.setEnabled(!loading);
        signInTab.setEnabled(!loading);
        registerTab.setEnabled(!loading);
    }

    private void showMessage(String message) {
        authMessage.setText(message);
        authMessage.setVisibility(View.VISIBLE);
    }

    private void clearMessage() {
        authMessage.setText("");
        authMessage.setVisibility(View.GONE);
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
