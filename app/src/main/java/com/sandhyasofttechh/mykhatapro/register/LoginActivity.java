package com.sandhyasofttechh.mykhatapro.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.MainActivity;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt;
    private Button loginBtn;
    private TextView registerTv, forgotPasswordTv;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        initViews();

        loginBtn.setOnClickListener(v -> loginUser());

        registerTv.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));

        forgotPasswordTv.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void initViews() {
        emailEt = findViewById(R.id.email_et);
        passwordEt = findViewById(R.id.password_et);
        loginBtn = findViewById(R.id.login_btn);
        registerTv = findViewById(R.id.register_tv);
        forgotPasswordTv = findViewById(R.id.forgot_password_tv);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loginUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Please enter a valid email");
            emailEt.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordEt.setError("Password must be at least 6 characters");
            passwordEt.requestFocus();
            return;
        }

        toggleLoading(true);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // Replace '.' with ',' for Firebase key
                    String emailKey = email.replace(".", ",");

                    // Reference to profile in database
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("Khatabook")
                            .child(emailKey)
                            .child("profile");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            toggleLoading(false);
                            if (snapshot.exists()) {
                                // ✅ NEW: Check status field
                                Boolean status = snapshot.child("status").getValue(Boolean.class);
                                if (status != null && status) {
                                    String uid = snapshot.child("uid").getValue(String.class);
                                    String userEmail = snapshot.child("email").getValue(String.class);
                                    String password = snapshot.child("password").getValue(String.class);

                                    // Save in PrefManager
                                    PrefManager prefManager = new PrefManager(LoginActivity.this);
                                    prefManager.setLogin(true);
                                    prefManager.setUserEmail(userEmail);

                                    // Navigate to MainActivity
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Your account is not active. Please contact support.", Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();
                                }
                            } else {
                                // User deleted from Firebase or database
                                Toast.makeText(LoginActivity.this, "This account no longer exists.", Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            toggleLoading(false);
                            Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    toggleLoading(false);
                    Toast.makeText(LoginActivity.this, "Please verify your email first", Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                }
            } else {
                toggleLoading(false);
                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginBtn.setEnabled(true);
        }
    }
}
