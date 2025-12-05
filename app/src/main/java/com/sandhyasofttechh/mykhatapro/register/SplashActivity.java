package com.sandhyasofttechh.mykhatapro.register;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.PromptInfo;
import androidx.cardview.widget.CardView;
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

    private static final long SPLASH_DELAY = 3000;
    private Executor splashExecutor;
    private BiometricPrompt splashBiometricPrompt;
    private PromptInfo splashPromptInfo;
    // Views for animation
    private CardView logoContainer;
    private LinearLayout nameContainer;
    private View nameUnderline;
    private TextView tagline;
    private ProgressBar progressBar;
    private LinearLayout bottomInfo;
    private View circle1;
    private View circle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initViews();
        startAnimations();
        new Handler(Looper.getMainLooper()).postDelayed(this::decideNextActivity, SPLASH_DELAY);
    }

    private void initViews() {
        logoContainer = findViewById(R.id.logo_container);
        nameContainer = findViewById(R.id.name_container);
        nameUnderline = findViewById(R.id.name_underline);
        tagline = findViewById(R.id.tv_tagline);
        progressBar = findViewById(R.id.progress_bar);
        bottomInfo = findViewById(R.id.bottom_info);
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
    }

    private void startAnimations() {
        // Animate background circles
        animateCircles();

        // Logo animation - Scale and Fade In
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0.3f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0.3f, 1f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logoContainer, "alpha", 0f, 1f);
        ObjectAnimator logoRotation = ObjectAnimator.ofFloat(logoContainer, "rotation", -180f, 0f);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(logoScaleX, logoScaleY, logoAlpha, logoRotation);
        logoSet.setDuration(800);
        logoSet.setInterpolator(new OvershootInterpolator());
        logoSet.setStartDelay(300);
        logoSet.start();

        // App name animation - Slide from bottom with fade
        ObjectAnimator nameTranslateY = ObjectAnimator.ofFloat(nameContainer, "translationY", 100f, 0f);
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(nameContainer, "alpha", 0f, 1f);

        AnimatorSet nameSet = new AnimatorSet();
        nameSet.playTogether(nameTranslateY, nameAlpha);
        nameSet.setDuration(600);
        nameSet.setInterpolator(new DecelerateInterpolator());
        nameSet.setStartDelay(900);
        nameSet.start();

        // Underline animation - Expand width
        nameUnderline.post(() -> {
            ValueAnimator underlineAnim = ValueAnimator.ofFloat(0f, 1f);
            underlineAnim.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                nameUnderline.setScaleX(value);
                nameUnderline.setAlpha(value);
            });
            underlineAnim.setDuration(500);
            underlineAnim.setStartDelay(1300);
            underlineAnim.start();
        });

        // Tagline animation - Fade in
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f);
        taglineAlpha.setDuration(600);
        taglineAlpha.setStartDelay(1500);
        taglineAlpha.start();

        // Progress bar animation - Fade in
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
        progressAlpha.setDuration(400);
        progressAlpha.setStartDelay(1800);
        progressAlpha.start();

        // Bottom info animation - Slide from bottom
        ObjectAnimator bottomTranslateY = ObjectAnimator.ofFloat(bottomInfo, "translationY", 80f, 0f);
        ObjectAnimator bottomAlpha = ObjectAnimator.ofFloat(bottomInfo, "alpha", 0f, 1f);

        AnimatorSet bottomSet = new AnimatorSet();
        bottomSet.playTogether(bottomTranslateY, bottomAlpha);
        bottomSet.setDuration(600);
        bottomSet.setInterpolator(new DecelerateInterpolator());
        bottomSet.setStartDelay(2000);
        bottomSet.start();
    }

    private void animateCircles() {
        // Circle 1 - Rotate continuously
        ObjectAnimator circle1Rotation = ObjectAnimator.ofFloat(circle1, "rotation", 0f, 360f);
        circle1Rotation.setDuration(20000);
        circle1Rotation.setRepeatCount(ValueAnimator.INFINITE);
        circle1Rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        circle1Rotation.start();

        // Circle 2 - Rotate opposite direction
        ObjectAnimator circle2Rotation = ObjectAnimator.ofFloat(circle2, "rotation", 360f, 0f);
        circle2Rotation.setDuration(25000);
        circle2Rotation.setRepeatCount(ValueAnimator.INFINITE);
        circle2Rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        circle2Rotation.start();

        // Pulse animation for circles
        ObjectAnimator circle1Scale = ObjectAnimator.ofFloat(circle1, "scaleX", 1f, 1.1f, 1f);
        circle1Scale.setDuration(3000);
        circle1Scale.setRepeatCount(ValueAnimator.INFINITE);
        circle1Scale.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator circle1ScaleY = ObjectAnimator.ofFloat(circle1, "scaleY", 1f, 1.1f, 1f);
        circle1ScaleY.setDuration(3000);
        circle1ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        circle1ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        circle1Scale.start();
        circle1ScaleY.start();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
        return false;
    }


    private void decideNextActivity() {

        if (!isNetworkAvailable()) {
            startActivity(new Intent(SplashActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

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
        // Fade out animation before navigation
        View rootView = findViewById(android.R.id.content);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, activity));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 300);
    }
}

