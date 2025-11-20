package com.sandhyasofttechh.mykhatapro.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        prefManager = new PrefManager(this);
        switchAppLock = findViewById(R.id.switch_applock);
        tvStatus = findViewById(R.id.tv_status_applock);

        // Setup executor & biometric prompt
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // if user cancels, restore switch to previous state (no change)
                Toast.makeText(AppLockActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                syncSwitchWithPref();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Success: toggle state already decided by pendingAction
                onAuthSuccessPendingAction();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AppLockActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                syncSwitchWithPref();
            }
        });

        // Build prompt that allows biometric OR device credential
        promptInfo = new PromptInfo.Builder()
                .setTitle("Authenticate to change App Lock")
                .setSubtitle("Use fingerprint/face or device PIN/pattern/password")
                // Allow device credential fallback
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        syncSwitchWithPref();

        switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Immediately revert UI; we'll update when auth succeeds
            switchAppLock.setOnCheckedChangeListener(null);
            switchAppLock.setChecked(prefManager.isAppLockEnabled()); // revert UI while we auth
            switchAppLock.setOnCheckedChangeListener((v, checked) -> {}); // temporary noop to avoid recursion

            // Save requested action in a field and launch prompt
            requestedEnableState = isChecked;
            if (!isBiometricAvailable()) {
                Toast.makeText(AppLockActivity.this, "No biometric or device credential available on this device.", Toast.LENGTH_LONG).show();
                syncSwitchWithPref();
                // restore listener properly
                setupSwitchListener();
                return;
            }
            biometricPrompt.authenticate(promptInfo);
            // reset listener will be done in sync methods
            setupSwitchListener();
        });

        // ensure listener established properly
        setupSwitchListener();
    }

    // small field to remember user's requested state while waiting for auth
    private boolean requestedEnableState = false;

    private void setupSwitchListener() {
        switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // same flow as above but ensures stable listener
            switchAppLock.setOnCheckedChangeListener(null);
            switchAppLock.setChecked(prefManager.isAppLockEnabled());
            requestedEnableState = isChecked;
            if (!isBiometricAvailable()) {
                Toast.makeText(AppLockActivity.this, "No biometric or device credential available on this device.", Toast.LENGTH_LONG).show();
                syncSwitchWithPref();
                setupSwitchListener();
                return;
            }
            biometricPrompt.authenticate(promptInfo);
            setupSwitchListener();
        });
    }

    private boolean isBiometricAvailable() {
        BiometricManager bm = BiometricManager.from(this);
        int canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS;
    }

    // Called when biometric auth succeeds.
    private void onAuthSuccessPendingAction() {
        // Apply the requested setting
        prefManager.setAppLockEnabled(requestedEnableState);
        syncSwitchWithPref();
        String msg = requestedEnableState ? "App Lock enabled" : "App Lock disabled";
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
}
