package com.sandhyasofttechh.mykhatapro.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.PromptInfo;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.MainActivity;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.concurrent.Executor;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500;

    private Executor splashExecutor;
    private BiometricPrompt splashBiometricPrompt;
    private PromptInfo splashPromptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::decideNextActivity, SPLASH_DELAY);
    }

    private void decideNextActivity() {
        PrefManager prefManager = new PrefManager(this);

        if (prefManager.isLoggedIn()) {
            checkUserStatusFromFirebase(prefManager);
        } else {
            navigateTo(LoginActivity.class);
        }
    }

    private void checkUserStatusFromFirebase(PrefManager prefManager) {

        String email = prefManager.getUserEmail();

        if (email == null || email.isEmpty()) {
            navigateTo(LoginActivity.class);
            return;
        }

        String emailKey = email.replace(".", ",");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("profile");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Boolean status = snapshot.child("status").getValue(Boolean.class);

                if (status != null && status) {

                    PrefManager pm = new PrefManager(SplashActivity.this);

                    if (pm.isAppLockEnabled()) {
                        showBiometricBeforeMain();
                    } else {
                        navigateTo(MainActivity.class);
                    }

                } else {
                    Toast.makeText(SplashActivity.this, "Your account is inactive.", Toast.LENGTH_LONG).show();
                    prefManager.setLogin(false);
                    FirebaseAuth.getInstance().signOut();
                    navigateTo(LoginActivity.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplashActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                navigateTo(LoginActivity.class);
            }
        });
    }

    private void showBiometricBeforeMain() {

        splashExecutor = ContextCompat.getMainExecutor(SplashActivity.this);

        splashBiometricPrompt = new BiometricPrompt(
                SplashActivity.this,
                splashExecutor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(SplashActivity.this, errString, Toast.LENGTH_LONG).show();
                        navigateTo(LoginActivity.class);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        navigateTo(MainActivity.class);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(SplashActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });

        splashPromptInfo = new PromptInfo.Builder()
                .setTitle("Unlock App")
                .setSubtitle("Authenticate to open the app")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();

        // check device capability
        BiometricManager manager = BiometricManager.from(SplashActivity.this);
        int canAuth = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            splashBiometricPrompt.authenticate(splashPromptInfo);
        } else {
            navigateTo(MainActivity.class);
        }
    }

    private void navigateTo(Class<?> activity) {
        startActivity(new Intent(SplashActivity.this, activity));
        finish();
    }
}
