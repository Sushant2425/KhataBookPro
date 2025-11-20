package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddCustomerActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_CONTACT = 1001;
    private static final int REQUEST_CODE_PICK_CALL = 1002;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    private static final int PERMISSIONS_REQUEST_READ_CALL_LOG = 102;

    private TextInputLayout tilName, tilPhone;
    private TextInputEditText etName, etPhone, etEmail, etAddress;
    private Button btnSave;
    private DatabaseReference databaseReference;
    private PrefManager prefManager;

    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        setupToolbar();
        initViews();
        setupFirebase();
        handleEditMode();
        setupClickListeners();
    }

    // -------------------------- Toolbar ------------------------------
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_add_customer);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Customer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    // ------------------------ Initialize Views ------------------------
    private void initViews() {
        tilName = findViewById(R.id.til_customer_name);
        tilPhone = findViewById(R.id.til_customer_phone);

        etName = findViewById(R.id.et_customer_name);
        etPhone = findViewById(R.id.et_customer_phone);
        etEmail = findViewById(R.id.et_customer_email);
        etAddress = findViewById(R.id.et_customer_address);

        btnSave = findViewById(R.id.btn_save_customer);

        prefManager = new PrefManager(this);
    }

    // -------------------------- Firebase Setup ------------------------
    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Khatabook");
    }

    // ------------------------ Handle Edit Mode -------------------------
//    private void handleEditMode() {
//        Intent intent = getIntent();
//        if (intent != null && intent.hasExtra("edit_customer_name")) {
//
//            isEditMode = true;
//
//            etName.setText(intent.getStringExtra("edit_customer_name"));
//            etPhone.setText(intent.getStringExtra("edit_customer_phone"));
//            etPhone.setEnabled(false);
//
//            etEmail.setText(intent.getStringExtra("edit_customer_email"));
//            etAddress.setText(intent.getStringExtra("edit_customer_address"));
//
//            btnSave.setText("Update Customer");
//
//        } else {
//            btnSave.setText("Save Customer");
//            etPhone.setEnabled(true);
//        }
//    }
// ------------------------ Handle Edit Mode (UPDATED & FIXED) -------------------------
    private void handleEditMode() {
        Intent intent = getIntent();

        // We are now using "EDIT_MODE" + standard keys from CustomerDetailsActivity
        if (intent.getBooleanExtra("EDIT_MODE", false)) {
            isEditMode = true;

            String name = intent.getStringExtra("CUSTOMER_NAME");
            String phone = intent.getStringExtra("CUSTOMER_PHONE");
            String email = intent.getStringExtra("CUSTOMER_EMAIL");
            String address = intent.getStringExtra("CUSTOMER_ADDRESS");

            // Fill the fields safely
            if (!TextUtils.isEmpty(name)) {
                etName.setText(name);
            }

            if (!TextUtils.isEmpty(phone)) {
                etPhone.setText(phone);
                etPhone.setEnabled(false); // Phone is unique key, cannot change
            }

            if (!TextUtils.isEmpty(email)) {
                etEmail.setText(email);
            }

            if (!TextUtils.isEmpty(address)) {
                etAddress.setText(address);
            }

            // Change button & title
            btnSave.setText("Update Customer");

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Customer");
            }

        } else {
            // Normal Add Mode
            btnSave.setText("Save Customer");
            etPhone.setEnabled(true);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Customer");
            }
        }
    }
    // ------------------------ Click Listeners -------------------------
    private void setupClickListeners() {

        tilName.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);

            } else {
                openContactPicker();
            }
        });

        tilPhone.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALL_LOG},
                        PERMISSIONS_REQUEST_READ_CALL_LOG);

            } else {
                openCallLogPicker();
            }
        });

        btnSave.setOnClickListener(v -> saveCustomer());
    }

    // ------------------------ Contact Picker --------------------------
    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
    }

    // ------------------- Call Log Picker (Custom Dialog) --------------------
    private void openCallLogPicker() {
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    new String[]{
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.CACHED_NAME,
                            CallLog.Calls.DATE
                    },
                    null, null,
                    CallLog.Calls.DATE + " DESC"
            );

            if (cursor == null || cursor.getCount() == 0) {
                Toast.makeText(this, "No call logs found", Toast.LENGTH_SHORT).show();
                return;
            }

            List<CallLogEntry> callLogs = new ArrayList<>();
            Set<String> seenNumbers = new HashSet<>();

            while (cursor.moveToNext()) {
                String number = cursor.getString(
                        cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));

                // Clean number
                number = number.replaceAll("[^0-9+]", "");

                if (number.startsWith("+91") && number.length() > 10) {
                    number = number.substring(3);
                } else if (number.startsWith("0") && number.length() > 10) {
                    number = number.substring(1);
                }

                if (number.length() < 10) continue;

                if (!seenNumbers.contains(number)) {
                    seenNumbers.add(number);
                    callLogs.add(new CallLogEntry(name, number));
                }
            }

            if (callLogs.isEmpty()) {
                Toast.makeText(this, "No valid phone numbers found", Toast.LENGTH_SHORT).show();
            } else {
                showCallLogPickerDialog(callLogs);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error reading call log", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void showCallLogPickerDialog(List<CallLogEntry> callLogs) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick from Call Log");

        CharSequence[] items = new CharSequence[callLogs.size()];
        for (int i = 0; i < callLogs.size(); i++) {
            items[i] = callLogs.get(i).toString();
        }

        builder.setItems(items, (dialog, which) -> {
            CallLogEntry entry = callLogs.get(which);
            etPhone.setText(entry.number);

            if (!TextUtils.isEmpty(entry.name)
                    && TextUtils.isEmpty(etName.getText().toString().trim())) {

                etName.setText(entry.name);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ---------------------- Model Class -------------------------
    private static class CallLogEntry {
        String name;
        String number;

        CallLogEntry(String name, String number) {
            this.name = name;
            this.number = number;
        }

        @Override
        public String toString() {
            if (TextUtils.isEmpty(name) || "null".equals(name)) {
                return number;
            }
            return name + " (" + number + ")";
        }
    }

    // ------------------------ Permissions -------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == PERMISSIONS_REQUEST_READ_CALL_LOG) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCallLogPicker();
            } else {
                Toast.makeText(this, "Call log permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------------ OnActivityResult -------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();


            // Contact
            if (requestCode == REQUEST_CODE_PICK_CONTACT && uri != null) {

                try (Cursor cursor = getContentResolver().query(uri,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                        },
                        null, null, null)) {

                    if (cursor != null && cursor.moveToFirst()) {

                        String name = cursor.getString(0);
                        String number = cursor.getString(1);

                        number = number.replaceAll("[^0-9+]", "");

                        if (number.startsWith("+91") && number.length() > 10) {
                            number = number.substring(3);
                        }

                        etName.setText(name);
                        etPhone.setText(number);
                    }

                } catch (Exception e) {
                    Toast.makeText(this, "Error reading contact", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // ------------------------ Save Customer -------------------------
    private void saveCustomer() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        tilName.setError(null);

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }
        tilPhone.setError(null);

        String userEmail = prefManager.getUserEmail();

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("name", name);
        customerMap.put("phone", phone);
        customerMap.put("email", email);
        customerMap.put("address", address);

        String userNode = userEmail.replace(".", ",");

        databaseReference
                .child(userNode)
                .child("customers")
                .child(phone)
                .setValue(customerMap)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this,
                            isEditMode ? "Customer updated successfully" :
                                    "Customer saved successfully",
                            Toast.LENGTH_SHORT).show();

// 2. In saveCustomer() success block, add this before finish():
                    Intent result = new Intent();
                    result.putExtra("UPDATED_CUSTOMER_NAME", name);
                    setResult(RESULT_OK, result);

                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
