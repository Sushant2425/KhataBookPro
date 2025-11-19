package com.sandhyasofttechh.mykhatapro.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

public class ChangePasswordActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout layoutCurrent, layoutNew, layoutConfirm;
    private TextInputEditText etCurrent, etNew, etConfirm;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        initViews();

        toolbar.setNavigationOnClickListener(v -> finish());

        Button btnChange = findViewById(R.id.btn_change_password);
        btnChange.setOnClickListener(v -> validateInputs());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_change_password);

        layoutCurrent = findViewById(R.id.layout_current_password);
        layoutNew = findViewById(R.id.layout_new_password);
        layoutConfirm = findViewById(R.id.layout_confirm_password);

        etCurrent = findViewById(R.id.et_current_password);
        etNew = findViewById(R.id.et_new_password);
        etConfirm = findViewById(R.id.et_confirm_password);
    }

    private void validateInputs() {

        String current = etCurrent.getText().toString().trim();
        String newPass = etNew.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(current)) {
            layoutCurrent.setError("Enter current password");
            return;
        }
        layoutCurrent.setError(null);

        if (newPass.length() < 6) {
            layoutNew.setError("Password must be at least 6 characters");
            return;
        }
        layoutNew.setError(null);

        if (!newPass.equals(confirm)) {
            layoutConfirm.setError("Passwords do not match");
            return;
        }
        layoutConfirm.setError(null);

        reauthenticateUser(current, newPass);
    }

    private void reauthenticateUser(String currentPassword, String newPassword) {

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updatePassword(newPassword);
            } else {
                Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(String newPassword) {

        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                updatePasswordInDatabase(newPassword);

                new AlertDialog.Builder(this)
                        .setTitle("Password Updated")
                        .setMessage("Your password has been changed successfully.")
                        .setPositiveButton("OK", (d, w) -> finish())
                        .show();

            } else {
                Toast.makeText(this,
                        "Failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePasswordInDatabase(String newPassword) {

        PrefManager pref = new PrefManager(this);
        String emailKey = pref.getUserEmail().replace(".", ",");

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("profile")
                .child("password");

        dbRef.setValue(newPassword);
    }
}
