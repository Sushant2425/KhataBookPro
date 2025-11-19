package com.sandhyasofttechh.mykhatapro.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;

import android.os.Bundle;
import android.widget.Toast;

import com.sandhyasofttechh.mykhatapro.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppLockActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        executor = Executors.newSingleThreadExecutor();

        biometricPrompt = new BiometricPrompt(AppLockActivity.this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        runOnUiThread(() ->
                                Toast.makeText(AppLockActivity.this,
                                        "Unlocked Successfully",
                                        Toast.LENGTH_SHORT).show()
                        );

                        finish(); // Continue to app
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        runOnUiThread(() ->
                                Toast.makeText(AppLockActivity.this,
                                        "Authentication Failed",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        runOnUiThread(() ->
                                Toast.makeText(AppLockActivity.this,
                                        "Error: " + errString,
                                        Toast.LENGTH_SHORT).show()
                        );

                        finish(); // Close lock screen activity
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MyKhata Pro")
                .setSubtitle("Use phone lock (PIN/Pattern/Fingerprint)")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                | BiometricManager.Authenticators.BIOMETRIC_WEAK
                                | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
