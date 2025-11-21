package com.sandhyasofttechh.mykhatapro.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.PromptInfo;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.concurrent.Executor;

public class AppLockActivity extends AppCompatActivity {

    private SwitchCompat switchAppLock;
    private TextView tvStatus;
    private PrefManager prefManager;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private PromptInfo promptInfo;

    // To remember what user wanted to do (enable/disable) during authentication
    private boolean requestedEnableState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        // Setup Toolbar with Back Arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        prefManager = new PrefManager(this);
        switchAppLock = findViewById(R.id.switch_applock);
        tvStatus = findViewById(R.id.tv_status_applock);

        // Setup executor & biometric prompt
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(AppLockActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                syncSwitchWithPref(); // Revert switch if auth fails or cancelled
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                onAuthSuccessPendingAction(); // Apply user's requested action
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AppLockActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                syncSwitchWithPref();
            }
        });

        // Build prompt that allows biometric OR device PIN/pattern/password
        promptInfo = new PromptInfo.Builder()
                .setTitle("Authenticate to change App Lock")
                .setSubtitle("Use fingerprint, face or device PIN/pattern")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        syncSwitchWithPref();
        setupSwitchListener(); // Set initial listener
    }

    private void setupSwitchListener() {
        switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Prevent immediate change in UI
            switchAppLock.setOnCheckedChangeListener(null);
            switchAppLock.setChecked(prefManager.isAppLockEnabled()); // Revert visually

            requestedEnableState = isChecked;

            if (!isBiometricAvailable()) {
                Toast.makeText(AppLockActivity.this, "No biometric or device credential available.", Toast.LENGTH_LONG).show();
                syncSwitchWithPref();
                setupSwitchListener(); // Restore listener
                return;
            }

            // Show biometric prompt
            biometricPrompt.authenticate(promptInfo);

            // Restore listener after launching prompt
            setupSwitchListener();
        });
    }

    private boolean isBiometricAvailable() {
        BiometricManager bm = BiometricManager.from(this);
        int canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS;
    }

    // Called only when authentication succeeds
    private void onAuthSuccessPendingAction() {
        prefManager.setAppLockEnabled(requestedEnableState);
        syncSwitchWithPref();
        String msg = requestedEnableState ? "App Lock Enabled" : "App Lock Disabled";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void syncSwitchWithPref() {
        boolean enabled = prefManager.isAppLockEnabled();
        switchAppLock.setChecked(enabled);
        tvStatus.setText("Status: " + (enabled ? "Enabled" : "Disabled"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncSwitchWithPref();
    }

    // Handle back arrow click in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Optional: Handle physical back button same as toolbar back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}