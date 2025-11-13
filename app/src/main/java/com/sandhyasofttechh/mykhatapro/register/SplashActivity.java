package com.sandhyasofttechh.mykhatapro.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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

    private static final long SPLASH_TIMEOUT = 100; // 1.8 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PrefManager prefManager = new PrefManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (prefManager.isLoggedIn()) {
                // User logged in -> check status from Firebase
                String email = prefManager.getUserEmail();
                if (email != null) {
                    String emailKey = email.replace(".", ",");
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("Khatabook")
                            .child(emailKey)
                            .child("profile");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Boolean status = snapshot.child("status").getValue(Boolean.class);
                            if (status != null && status) {
                                // Account active → go to MainActivity
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            } else {
                                // Account inactive → auto logout + delete node
                                Toast.makeText(SplashActivity.this,
                                        "Your account is inactive. Logging out.", Toast.LENGTH_LONG).show();

                                // Delete the node from Firebase
                                userRef.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Reset local login flag
                                        prefManager.setLogin(false);
                                        FirebaseAuth.getInstance().signOut();
                                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                    } else {
                                        // Could not delete node, still logout locally
                                        prefManager.setLogin(false);
                                        FirebaseAuth.getInstance().signOut();
                                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                    }
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(SplashActivity.this,
                                    "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    });

                } else {
                    // Email not found in PrefManager → go to login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            } else {
                // Not logged in → go to login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}
