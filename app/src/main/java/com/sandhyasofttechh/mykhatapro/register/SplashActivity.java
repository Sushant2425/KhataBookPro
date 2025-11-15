package com.sandhyasofttechh.mykhatapro.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.MainActivity;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.iv_logo);
        TextView appName = findViewById(R.id.tv_app_name_splash);

        // Load animations
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up_logo);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up_text);

        // Start animations
        logo.startAnimation(logoAnimation);
        appName.startAnimation(textAnimation);

        // Decide where to go after the splash screen
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
        if (email == null) {
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
                    navigateTo(MainActivity.class);
                } else {
                    Toast.makeText(SplashActivity.this, "Your account is inactive. Logging out.", Toast.LENGTH_LONG).show();
                    prefManager.setLogin(false);
                    FirebaseAuth.getInstance().signOut();
                    navigateTo(LoginActivity.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplashActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                navigateTo(LoginActivity.class);
            }
        });
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(SplashActivity.this, destination);
        startActivity(intent);
        finish();
    }
}