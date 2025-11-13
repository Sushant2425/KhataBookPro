package com.sandhyasofttechh.mykhatapro.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.MainActivity;
import com.sandhyasofttechh.mykhatapro.R;

import com.sandhyasofttechh.mykhatapro.model.User;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt, confirmPasswordEt;
    private Button registerBtn;
    private TextView loginTv;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Khatabook"); // Parent node

        initViews();

        registerBtn.setOnClickListener(v -> registerUser());

        loginTv.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        emailEt = findViewById(R.id.email_et);
        passwordEt = findViewById(R.id.password_et);
        confirmPasswordEt = findViewById(R.id.confirm_password_et);
        registerBtn = findViewById(R.id.register_btn);
        loginTv = findViewById(R.id.login_tv);
        progressBar = findViewById(R.id.progressBar);
    }

    private void registerUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString();
        String confirmPassword = confirmPasswordEt.getText().toString();

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
        if (!password.equals(confirmPassword)) {
            confirmPasswordEt.setError("Passwords do not match");
            confirmPasswordEt.requestFocus();
            return;
        }

        toggleLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();

                    // Replace '.' with ',' for Firebase key
                    String emailKey = email.replace(".", ",");

                    // Save under Khatabook/email/profile
                    DatabaseReference profileRef = database.child(emailKey).child("profile");
                    User newUser = new User(uid, email, password);

                    profileRef.setValue(newUser).addOnCompleteListener(dbTask -> {
                        toggleLoading(false);
                        if (dbTask.isSuccessful()) {
                            firebaseUser.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                if (verifyTask.isSuccessful()) {
                                    Toast.makeText(RegistrationActivity.this,
                                            "Registration successful. Please verify your email.",
                                            Toast.LENGTH_LONG).show();
                                    PrefManager prefManager = new PrefManager(RegistrationActivity.this);
                                    prefManager.setLogin(false);
                                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegistrationActivity.this,
                                            "Failed to send verification email.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    "Failed to save user data.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                toggleLoading(false);
                Toast.makeText(RegistrationActivity.this,
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }



    private void toggleLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            registerBtn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(true);
        }
    }
}
