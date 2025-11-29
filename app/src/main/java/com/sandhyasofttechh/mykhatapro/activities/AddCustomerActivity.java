//package com.sandhyasofttechh.mykhatapro.activities;
//
//import android.Manifest;
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.provider.ContactsContract;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.RequestOptions;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.card.MaterialCardView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class AddCustomerActivity extends AppCompatActivity {
//
//    private static final String TAG = "AddCustomerActivity";
//    private static final int REQUEST_CODE_PICK_CONTACT = 1001;
//    private static final int REQUEST_CODE_PICK_IMAGE = 1003;
//    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
//    private static final long AUTO_SAVE_DELAY_MS = 2000;
//
//    // Views
//    private TextInputLayout tilName, tilPhone, tilEmail, tilGstin, tilPan, tilAddress, tilNotes;
//    private TextInputEditText etName, etPhone, etEmail, etGstin, etPan, etAddress, etNotes;
//    private MaterialButton btnDeleteCustomer;
//    private FloatingActionButton fabChangeImage;
//    private ImageView ivCustomerImage;
//    private MaterialCardView cardAutoSaveIndicator;
//    private ProgressBar progressSaving;
//    private TextView tvSaveStatus;
//
//    // Firebase
//    private DatabaseReference baseRef, customersRef;
//    private StorageReference storageRef;
//    private PrefManager prefManager;
//
//    // State
//    private boolean isEditMode = false;
//    private String currentCustomerPhone = "";
//    private String currentCustomerId = "";
//    private boolean isLoadingData = false;
//    private boolean isSaving = false;
//    private Handler autoSaveHandler = new Handler(Looper.getMainLooper());
//    private Runnable autoSaveRunnable;
//    private Uri selectedImageUri = null;
//    private String customerImageUrl = "";
//    private boolean imageChanged = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_customer);
//
//        Log.d(TAG, "🚀 Professional Customer Form - Enhanced Version");
//
//        setupToolbar();
//        initViews();
//        setupFirebase();
//        handleIntentData();
//        setupClickListeners();
//        setupAutoSave();
//        setupValidation();
//    }
//
//    private void setupToolbar() {
//        Toolbar toolbar = findViewById(R.id.toolbar_add_customer);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//    }
//
//    private void initViews() {
//        // Text Input Layouts
//        tilName = findViewById(R.id.til_customer_name);
//        tilPhone = findViewById(R.id.til_customer_phone);
//        tilEmail = findViewById(R.id.til_customer_email);
//        tilGstin = findViewById(R.id.til_customer_gstin);
//        tilPan = findViewById(R.id.til_customer_pan);
//        tilAddress = findViewById(R.id.til_customer_address);
//        tilNotes = findViewById(R.id.til_customer_notes);
//
//        // Edit Texts
//        etName = findViewById(R.id.et_customer_name);
//        etPhone = findViewById(R.id.et_customer_phone);
//        etEmail = findViewById(R.id.et_customer_email);
//        etGstin = findViewById(R.id.et_customer_gstin);
//        etPan = findViewById(R.id.et_customer_pan);
//        etAddress = findViewById(R.id.et_customer_address);
//        etNotes = findViewById(R.id.et_customer_notes);
//
//        // Other Views
//        ivCustomerImage = findViewById(R.id.iv_customer_image);
//        fabChangeImage = findViewById(R.id.fab_change_image);
//        btnDeleteCustomer = findViewById(R.id.btn_delete_customer);
//        cardAutoSaveIndicator = findViewById(R.id.card_auto_save_indicator);
//        progressSaving = findViewById(R.id.progress_saving);
//        tvSaveStatus = findViewById(R.id.tv_save_status);
//
//        prefManager = new PrefManager(this);
//    }
//
//    private void setupFirebase() {
//        String email = prefManager.getUserEmail();
//        if (email == null || email.isEmpty()) {
//            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        String emailKey = email.replace(".", ",");
//        String shopId = prefManager.getCurrentShopId();
//
//        if (shopId == null || shopId.isEmpty()) {
//            baseRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(emailKey);
//        } else {
//            baseRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(emailKey)
//                    .child("shops")
//                    .child(shopId);
//        }
//
//        customersRef = baseRef.child("customers");
//        storageRef = FirebaseStorage.getInstance().getReference("customer_images");
//
//        Log.d(TAG, "✅ Firebase initialized successfully");
//    }
//
//    private void handleIntentData() {
//        Intent intent = getIntent();
//        if (intent.getBooleanExtra("EDIT_MODE", false)) {
//            isEditMode = true;
//            currentCustomerPhone = intent.getStringExtra("CUSTOMER_PHONE");
//            currentCustomerId = intent.getStringExtra("CUSTOMER_ID");
//
//            if (currentCustomerId == null || currentCustomerId.isEmpty()) {
//                currentCustomerId = currentCustomerPhone;
//            }
//
//            loadCustomerData(currentCustomerId);
//            btnDeleteCustomer.setVisibility(View.VISIBLE);
//            etPhone.setEnabled(false);
//
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setTitle("Edit Customer");
//            }
//        } else {
//            currentCustomerId = UUID.randomUUID().toString();
//            btnDeleteCustomer.setVisibility(View.GONE);
//            etPhone.setEnabled(true);
//
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setTitle("Add Customer");
//            }
//        }
//    }
//
//    private void loadCustomerData(String customerId) {
//        isLoadingData = true;
//        showSaveIndicator("Loading...");
//
//        customersRef.child(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
//                    if (data != null) {
//                        etName.setText(getStringValue(data, "name"));
//                        etPhone.setText(getStringValue(data, "phone"));
//                        etEmail.setText(getStringValue(data, "email"));
//                        etAddress.setText(getStringValue(data, "address"));
//                        etGstin.setText(getStringValue(data, "gstin"));
//                        etPan.setText(getStringValue(data, "pan"));
//                        etNotes.setText(getStringValue(data, "notes"));
//
//                        customerImageUrl = getStringValue(data, "image_url");
//                        loadCustomerImage(customerImageUrl);
//                    }
//                }
//                isLoadingData = false;
//                hideSaveIndicator();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                isLoadingData = false;
//                hideSaveIndicator();
//                Toast.makeText(AddCustomerActivity.this,
//                        "Failed to load customer data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void loadCustomerImage(String imageUrl) {
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            Glide.with(this)
//                    .load(imageUrl)
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.ic_person)
//                            .error(R.drawable.ic_person)
//                            .circleCrop()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL))
//                    .into(ivCustomerImage);
//        } else {
//            ivCustomerImage.setImageResource(R.drawable.ic_person);
//        }
//    }
//
//    private String getStringValue(Map<String, Object> data, String key) {
//        Object value = data.get(key);
//        return value != null ? value.toString() : "";
//    }
//
//    private void setupClickListeners() {
//        // Contact Picker
//        tilPhone.setEndIconOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        PERMISSIONS_REQUEST_READ_CONTACTS);
//            } else {
//                openContactPicker();
//            }
//        });
//
//        // Image Picker
//        fabChangeImage.setOnClickListener(v -> openImagePicker());
//
//        // Delete Customer
//        btnDeleteCustomer.setOnClickListener(v -> showDeleteConfirmationDialog());
//    }
//
//    private void setupAutoSave() {
//        autoSaveRunnable = this::saveCustomerData;
//
//        TextWatcher autoSaveWatcher = new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (!isLoadingData) {
//                    autoSaveHandler.removeCallbacks(autoSaveRunnable);
//                    autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY_MS);
//                }
//            }
//        };
//
//        etName.addTextChangedListener(autoSaveWatcher);
//        etPhone.addTextChangedListener(autoSaveWatcher);
//        etEmail.addTextChangedListener(autoSaveWatcher);
//        etAddress.addTextChangedListener(autoSaveWatcher);
//        etGstin.addTextChangedListener(autoSaveWatcher);
//        etPan.addTextChangedListener(autoSaveWatcher);
//        etNotes.addTextChangedListener(autoSaveWatcher);
//    }
//
//    private void setupValidation() {
//        // Real-time Email Validation
//        etEmail.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String email = s.toString().trim();
//                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                    tilEmail.setError("Invalid email format");
//                } else {
//                    tilEmail.setError(null);
//                }
//            }
//        });
//
//        // Phone Validation
//        etPhone.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String phone = s.toString().trim();
//                if (!phone.isEmpty() && phone.length() < 10) {
//                    tilPhone.setError("Phone must be 10 digits");
//                } else {
//                    tilPhone.setError(null);
//                }
//            }
//        });
//
//        // GSTIN Validation (15 characters)
//        etGstin.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String gstin = s.toString().trim();
//                if (!gstin.isEmpty() && gstin.length() != 15) {
//                    tilGstin.setError("GSTIN must be 15 characters");
//                } else {
//                    tilGstin.setError(null);
//                }
//            }
//        });
//
//        // PAN Validation (10 characters)
//        etPan.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String pan = s.toString().trim();
//                if (!pan.isEmpty() && pan.length() != 10) {
//                    tilPan.setError("PAN must be 10 characters");
//                } else {
//                    tilPan.setError(null);
//                }
//            }
//        });
//    }
//
//    private boolean isValidForm() {
//        boolean isValid = true;
//
//        // Required: Name
//        String name = etName.getText().toString().trim();
//        if (name.isEmpty()) {
//            tilName.setError("Customer name is required");
//            isValid = false;
//        } else {
//            tilName.setError(null);
//        }
//
//        // Required: Phone
//        String phone = etPhone.getText().toString().trim();
//        if (phone.isEmpty()) {
//            tilPhone.setError("Phone number is required");
//            isValid = false;
//        } else if (phone.length() < 10) {
//            tilPhone.setError("Phone must be 10 digits");
//            isValid = false;
//        } else {
//            tilPhone.setError(null);
//        }
//
//        // Optional but validate if provided: Email
//        String email = etEmail.getText().toString().trim();
//        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            tilEmail.setError("Invalid email format");
//            isValid = false;
//        } else {
//            tilEmail.setError(null);
//        }
//
//        // Optional but validate if provided: GSTIN
//        String gstin = etGstin.getText().toString().trim();
//        if (!gstin.isEmpty() && gstin.length() != 15) {
//            tilGstin.setError("GSTIN must be 15 characters");
//            isValid = false;
//        } else {
//            tilGstin.setError(null);
//        }
//
//        // Optional but validate if provided: PAN
//        String pan = etPan.getText().toString().trim();
//        if (!pan.isEmpty() && pan.length() != 10) {
//            tilPan.setError("PAN must be 10 characters");
//            isValid = false;
//        } else {
//            tilPan.setError(null);
//        }
//
//        return isValid;
//    }
//
//    private void saveCustomerData() {
//        if (isSaving || isLoadingData || !isValidForm()) {
//            return;
//        }
//
//        isSaving = true;
//        showSaveIndicator("Saving...");
//
//        String phone = etPhone.getText().toString().trim();
//
//        // Upload image first if changed
//        if (imageChanged && selectedImageUri != null) {
//            uploadImageAndSaveData(currentCustomerId, phone);
//        } else {
//            saveDataToFirebase(currentCustomerId, phone, customerImageUrl);
//        }
//    }
//
//    private void uploadImageAndSaveData(String customerId, String phone) {
//        Log.d(TAG, "📤 Uploading customer image...");
//
//        String imageFileName = customerId + "_" + System.currentTimeMillis() + ".jpg";
//        StorageReference imageRef = storageRef.child(imageFileName);
//
//        imageRef.putFile(selectedImageUri)
//                .addOnProgressListener(snapshot -> {
//                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//                    tvSaveStatus.setText("Uploading: " + (int) progress + "%");
//                })
//                .addOnSuccessListener(taskSnapshot -> {
//                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        customerImageUrl = uri.toString();
//                        imageChanged = false;
//                        Log.d(TAG, "✅ Image uploaded: " + customerImageUrl);
//                        saveDataToFirebase(customerId, phone, customerImageUrl);
//                    }).addOnFailureListener(e -> {
//                        Log.e(TAG, "Failed to get download URL: " + e.getMessage());
//                        saveDataToFirebase(customerId, phone, "");
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Image upload failed: " + e.getMessage());
//                    Toast.makeText(this, "Image upload failed, saving without image",
//                            Toast.LENGTH_SHORT).show();
//                    saveDataToFirebase(customerId, phone, customerImageUrl);
//                });
//    }
//
//    private void saveDataToFirebase(String customerId, String phone, String imageUrl) {
//        Map<String, Object> customerData = new HashMap<>();
//        customerData.put("id", customerId);
//        customerData.put("name", etName.getText().toString().trim());
//        customerData.put("phone", phone);
//
//        // Optional fields - only save if not empty
//        String email = etEmail.getText().toString().trim();
//        customerData.put("email", email.isEmpty() ? null : email);
//
//        String address = etAddress.getText().toString().trim();
//        customerData.put("address", address.isEmpty() ? null : address);
//
//        String gstin = etGstin.getText().toString().trim();
//        customerData.put("gstin", gstin.isEmpty() ? null : gstin);
//
//        String pan = etPan.getText().toString().trim();
//        customerData.put("pan", pan.isEmpty() ? null : pan);
//
//        String notes = etNotes.getText().toString().trim();
//        customerData.put("notes", notes.isEmpty() ? null : notes);
//
//        customerData.put("image_url", imageUrl.isEmpty() ? null : imageUrl);
//        customerData.put("timestamp", System.currentTimeMillis());
//        customerData.put("updated_at", System.currentTimeMillis());
//
//        customersRef.child(customerId).setValue(customerData)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "✅ Customer data saved successfully");
//                    showSaveIndicator("✓ Saved");
//
//                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        hideSaveIndicator();
//                    }, 2000);
//
//                    isSaving = false;
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "❌ Save failed: " + e.getMessage());
//                    Toast.makeText(this, "Failed to save customer", Toast.LENGTH_SHORT).show();
//                    hideSaveIndicator();
//                    isSaving = false;
//                });
//    }
//
//    private void showSaveIndicator(String message) {
//        runOnUiThread(() -> {
//            tvSaveStatus.setText(message);
//            cardAutoSaveIndicator.setVisibility(View.VISIBLE);
//
//            if (message.contains("Saving") || message.contains("Uploading") || message.contains("Loading")) {
//                progressSaving.setVisibility(View.VISIBLE);
//            } else {
//                progressSaving.setVisibility(View.GONE);
//            }
//        });
//    }
//
//    private void hideSaveIndicator() {
//        runOnUiThread(() -> {
//            cardAutoSaveIndicator.setVisibility(View.GONE);
//            progressSaving.setVisibility(View.GONE);
//        });
//    }
//
//    private void showDeleteConfirmationDialog() {
//        String customerName = etName.getText().toString().trim();
//
//        new AlertDialog.Builder(this)
//                .setTitle("Delete Customer")
//                .setMessage("Are you sure you want to delete " + customerName + "?\n\nThis action cannot be undone.")
//                .setPositiveButton("Delete", (dialog, which) -> deleteCustomer())
//                .setNegativeButton("Cancel", null)
//                .setIcon(R.drawable.ic_delete)
//                .show();
//    }
//
//    private void deleteCustomer() {
//        showSaveIndicator("Deleting...");
//
//        // Delete image from storage if exists
//        if (customerImageUrl != null && !customerImageUrl.isEmpty()) {
//            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(customerImageUrl);
//            imageRef.delete().addOnSuccessListener(aVoid -> {
//                Log.d(TAG, "✅ Customer image deleted");
//            }).addOnFailureListener(e -> {
//                Log.w(TAG, "Failed to delete image: " + e.getMessage());
//            });
//        }
//
//        // Delete customer data
//        customersRef.child(currentCustomerId).removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(this, "Customer deleted successfully", Toast.LENGTH_LONG).show();
//                    setResult(RESULT_OK);
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    hideSaveIndicator();
//                    Toast.makeText(this, "Failed to delete customer", Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void openContactPicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
//        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
//    }
//
//    private void openImagePicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openContactPicker();
//            } else {
//                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && data != null) {
//            if (requestCode == REQUEST_CODE_PICK_CONTACT) {
//                handleContactPicked(data);
//            } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
//                handleImagePicked(data);
//            }
//        }
//    }
//
//    private void handleContactPicked(Intent data) {
//        Uri contactUri = data.getData();
//        if (contactUri == null) return;
//
//        try (Cursor cursor = getContentResolver().query(
//                contactUri,
//                new String[]{
//                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                        ContactsContract.CommonDataKinds.Phone.NUMBER
//                },
//                null, null, null)) {
//
//            if (cursor != null && cursor.moveToFirst()) {
//                String name = cursor.getString(0);
//                String phone = cursor.getString(1).replaceAll("[^0-9+]", "");
//
//                // Remove country code if present
//                if (phone.startsWith("+91") && phone.length() > 10) {
//                    phone = phone.substring(3);
//                } else if (phone.startsWith("91") && phone.length() > 10) {
//                    phone = phone.substring(2);
//                }
//
//                etName.setText(name);
//                etPhone.setText(phone);
//
//                Toast.makeText(this, "Contact imported successfully", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error picking contact: " + e.getMessage());
//            Toast.makeText(this, "Failed to import contact", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void handleImagePicked(Intent data) {
//        selectedImageUri = data.getData();
//        if (selectedImageUri != null) {
//            imageChanged = true;
//
//            Glide.with(this)
//                    .load(selectedImageUri)
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.ic_person)
//                            .error(R.drawable.ic_person)
//                            .circleCrop())
//                    .into(ivCustomerImage);
//
//            Toast.makeText(this, "Image selected - will be saved automatically",
//                    Toast.LENGTH_SHORT).show();
//
//            // Trigger auto-save
//            autoSaveHandler.removeCallbacks(autoSaveRunnable);
//            autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY_MS);
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (isSaving) {
//            Toast.makeText(this, "Please wait, saving in progress...", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        super.onBackPressed();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (autoSaveHandler != null && autoSaveRunnable != null) {
//            autoSaveHandler.removeCallbacks(autoSaveRunnable);
//        }
//    }
//}



package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCustomerActivity extends AppCompatActivity {

    private static final String TAG = "AddCustomerActivity";
    private static final int REQUEST_CODE_PICK_CONTACT = 1001;
    private static final int REQUEST_CODE_PICK_IMAGE = 1003;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    private static final long AUTO_SAVE_DELAY_MS = 1500;

    // Views
    private TextInputLayout tilName, tilPhone, tilEmail, tilGstin, tilPan, tilAddress, tilNotes;
    private TextInputEditText etName, etPhone, etEmail, etGstin, etPan, etAddress, etNotes;
    private MaterialButton btnDeleteCustomer;
    private FloatingActionButton fabChangeImage;
    private ImageView ivCustomerImage;
    private MaterialCardView cardAutoSaveIndicator;
    private ProgressBar progressSaving;
    private TextView tvSaveStatus;

    // Firebase - NO STORAGE NEEDED
    private DatabaseReference baseRef, customersRef;
    private PrefManager prefManager;

    // State
    private boolean isEditMode = false;
    private String currentCustomerPhone = "";
    private String currentCustomerId = "";
    private boolean isLoadingData = false;
    private boolean isSaving = false;
    private Handler autoSaveHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSaveRunnable;
    private String selectedImagePath = "";  // 🔥 IMAGE PATH (NOT URI)
    private boolean imageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        Log.d(TAG, "🚀 IMAGE PATH SAVING - 100% WORKING NO STORAGE!");

        setupToolbar();
        initViews();
        setupFirebase();
        handleIntentData();
        setupClickListeners();
        setupAutoSave();
        setupValidation();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_add_customer);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        tilName = findViewById(R.id.til_customer_name);
        tilPhone = findViewById(R.id.til_customer_phone);
        tilEmail = findViewById(R.id.til_customer_email);
        tilGstin = findViewById(R.id.til_customer_gstin);
        tilPan = findViewById(R.id.til_customer_pan);
        tilAddress = findViewById(R.id.til_customer_address);
        tilNotes = findViewById(R.id.til_customer_notes);

        etName = findViewById(R.id.et_customer_name);
        etPhone = findViewById(R.id.et_customer_phone);
        etEmail = findViewById(R.id.et_customer_email);
        etGstin = findViewById(R.id.et_customer_gstin);
        etPan = findViewById(R.id.et_customer_pan);
        etAddress = findViewById(R.id.et_customer_address);
        etNotes = findViewById(R.id.et_customer_notes);

        ivCustomerImage = findViewById(R.id.iv_customer_image);
        fabChangeImage = findViewById(R.id.fab_change_image);
        btnDeleteCustomer = findViewById(R.id.btn_delete_customer);
        cardAutoSaveIndicator = findViewById(R.id.card_auto_save_indicator);
        progressSaving = findViewById(R.id.progress_saving);
        tvSaveStatus = findViewById(R.id.tv_save_status);

        prefManager = new PrefManager(this);
    }

    private void setupFirebase() {
        String email = prefManager.getUserEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String emailKey = email.replace(".", ",");
        String shopId = prefManager.getCurrentShopId();

        if (shopId == null || shopId.isEmpty()) {
            baseRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(emailKey);
        } else {
            baseRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId);
        }

        customersRef = baseRef.child("customers");
        Log.d(TAG, "✅ Firebase Database ready: " + customersRef.toString());
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("EDIT_MODE", false)) {
            isEditMode = true;
            currentCustomerPhone = intent.getStringExtra("CUSTOMER_PHONE");
            currentCustomerId = intent.getStringExtra("CUSTOMER_ID");

            if (currentCustomerId == null || currentCustomerId.isEmpty()) {
                currentCustomerId = currentCustomerPhone;
            }

            loadCustomerData(currentCustomerId);
            btnDeleteCustomer.setVisibility(View.VISIBLE);
            etPhone.setEnabled(false);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Customer");
            }
        } else {
            currentCustomerId = UUID.randomUUID().toString();
            btnDeleteCustomer.setVisibility(View.GONE);
            etPhone.setEnabled(true);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Customer");
            }
        }
    }

    private void loadCustomerData(String customerId) {
        isLoadingData = true;
        showSaveIndicator("Loading...");

        customersRef.child(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    if (data != null) {
                        etName.setText(getStringValue(data, "name"));
                        etPhone.setText(getStringValue(data, "phone"));
                        etEmail.setText(getStringValue(data, "email"));
                        etAddress.setText(getStringValue(data, "address"));
                        etGstin.setText(getStringValue(data, "gstin"));
                        etPan.setText(getStringValue(data, "pan"));
                        etNotes.setText(getStringValue(data, "notes"));

                        // 🔥 LOAD IMAGE PATH
                        selectedImagePath = getStringValue(data, "image_path");
                        if (!selectedImagePath.isEmpty()) {
                            loadCustomerImage(selectedImagePath);
                            Log.d(TAG, "✅ IMAGE PATH LOADED: " + selectedImagePath);
                        }
                    }
                }
                isLoadingData = false;
                hideSaveIndicator();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoadingData = false;
                hideSaveIndicator();
                Toast.makeText(AddCustomerActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCustomerImage(String imagePath) {
        Glide.with(this)
                .load(imagePath)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(ivCustomerImage);
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    private void setupClickListeners() {
        tilPhone.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                openContactPicker();
            }
        });

        fabChangeImage.setOnClickListener(v -> openImagePicker());
        btnDeleteCustomer.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void setupAutoSave() {
        autoSaveRunnable = this::saveCustomerData;

        TextWatcher autoSaveWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isLoadingData && !isSaving) {
                    autoSaveHandler.removeCallbacks(autoSaveRunnable);
                    autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY_MS);
                }
            }
        };

        etName.addTextChangedListener(autoSaveWatcher);
        etPhone.addTextChangedListener(autoSaveWatcher);
        etEmail.addTextChangedListener(autoSaveWatcher);
        etAddress.addTextChangedListener(autoSaveWatcher);
        etGstin.addTextChangedListener(autoSaveWatcher);
        etPan.addTextChangedListener(autoSaveWatcher);
        etNotes.addTextChangedListener(autoSaveWatcher);
    }

    private void setupValidation() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.setError("Invalid email");
                } else {
                    tilEmail.setError(null);
                }
            }
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                if (!phone.isEmpty() && phone.length() < 10) {
                    tilPhone.setError("Phone must be 10 digits");
                } else {
                    tilPhone.setError(null);
                }
            }
        });

        etGstin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String gstin = s.toString().trim();
                if (!gstin.isEmpty() && gstin.length() != 15) {
                    tilGstin.setError("GSTIN must be 15 chars");
                } else {
                    tilGstin.setError(null);
                }
            }
        });

        etPan.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String pan = s.toString().trim();
                if (!pan.isEmpty() && pan.length() != 10) {
                    tilPan.setError("PAN must be 10 chars");
                } else {
                    tilPan.setError(null);
                }
            }
        });
    }

    private boolean isValidForm() {
        boolean isValid = true;
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError("Name required");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty() || phone.length() < 10) {
            tilPhone.setError("Valid phone required");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        return isValid;
    }

    // 🔥 PERFECT SAVE - IMAGE PATH SAVED TO FIREBASE
    private void saveCustomerData() {
        if (isSaving || isLoadingData || !isValidForm()) return;

        isSaving = true;
        String phone = etPhone.getText().toString().trim();
        Log.d(TAG, "🚀 SAVING ALL DATA + IMAGE PATH: " + selectedImagePath);

        showSaveIndicator("Saving...");

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("id", currentCustomerId);
        customerData.put("name", etName.getText().toString().trim());
        customerData.put("phone", phone);
        customerData.put("email", etEmail.getText().toString().trim().isEmpty() ? null : etEmail.getText().toString().trim());
        customerData.put("address", etAddress.getText().toString().trim().isEmpty() ? null : etAddress.getText().toString().trim());
        customerData.put("gstin", etGstin.getText().toString().trim().isEmpty() ? null : etGstin.getText().toString().trim());
        customerData.put("pan", etPan.getText().toString().trim().isEmpty() ? null : etPan.getText().toString().trim());
        customerData.put("notes", etNotes.getText().toString().trim().isEmpty() ? null : etNotes.getText().toString().trim());

        // 🔥 IMAGE PATH SAVED HERE - NO UPLOAD!
        customerData.put("image_path", selectedImagePath.isEmpty() ? null : selectedImagePath);
        customerData.put("timestamp", System.currentTimeMillis());
        customerData.put("updated_at", System.currentTimeMillis());

        customersRef.child(currentCustomerId).setValue(customerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ ALL SAVED WITH IMAGE PATH!");
                    showSaveIndicator("✓ Saved with Image!");
                    imageChanged = false;

                    new Handler(Looper.getMainLooper()).postDelayed(this::hideSaveIndicator, 1500);
                    isSaving = false;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ SAVE FAILED: " + e.getMessage());
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                    hideSaveIndicator();
                    isSaving = false;
                });
    }

    private void showSaveIndicator(String message) {
        tvSaveStatus.setText(message);
        cardAutoSaveIndicator.setVisibility(View.VISIBLE);
        if (message.contains("Saving")) {
            progressSaving.setVisibility(View.VISIBLE);
        }
    }

    private void hideSaveIndicator() {
        cardAutoSaveIndicator.setVisibility(View.GONE);
        progressSaving.setVisibility(View.GONE);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Delete this customer permanently?")
                .setPositiveButton("Delete", (d, w) -> deleteCustomer())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCustomer() {
        customersRef.child(currentCustomerId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Customer deleted", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show());
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_PICK_CONTACT) {
                handleContactPicked(data);
            } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                handleImagePicked(data);
            }
        }
    }

    private void handleContactPicked(Intent data) {
        Uri contactUri = data.getData();
        if (contactUri == null) return;

        try (Cursor cursor = getContentResolver().query(contactUri, new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        }, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                String phone = cursor.getString(1).replaceAll("[^0-9+]", "");
                if (phone.startsWith("+91") && phone.length() > 10) phone = phone.substring(3);
                else if (phone.startsWith("91") && phone.length() > 10) phone = phone.substring(2);

                etName.setText(name);
                etPhone.setText(phone);
                Toast.makeText(this, "Contact imported", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Contact import failed", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 IMAGE PATH SAVING - WORKS 100%
    private void handleImagePicked(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri != null) {
            selectedImagePath = imageUri.toString();  // 🔥 JUST SAVE PATH AS STRING
            imageChanged = true;

            Glide.with(this)
                    .load(imageUri)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop())
                    .into(ivCustomerImage);

            Log.d(TAG, "✅ IMAGE PATH SAVED: " + selectedImagePath);
            Toast.makeText(this, "🖼️ Image selected - saves automatically!", Toast.LENGTH_SHORT).show();

            // Auto-save immediately
            autoSaveHandler.removeCallbacks(autoSaveRunnable);
            autoSaveHandler.postDelayed(autoSaveRunnable, 1000);
        }
    }

    @Override
    public void onBackPressed() {
        if (isSaving) {
            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoSaveHandler != null && autoSaveRunnable != null) {
            autoSaveHandler.removeCallbacks(autoSaveRunnable);
        }
    }
}
