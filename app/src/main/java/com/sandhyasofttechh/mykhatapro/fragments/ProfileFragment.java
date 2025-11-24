package com.sandhyasofttechh.mykhatapro.fragments;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttechh.mykhatapro.R;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private ProgressBar progressBarProfile;

    private CircleImageView ivProfileLogo;
    private TextInputEditText etName, etBusinessName, etMobile, etAddress, etGstin, etEmail;
    private View ivCameraIcon;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference logoRef;

    private Uri logoUri = null;
    private String currentPhotoPath;

    // Activity Result Launchers
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleImageResult);

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && logoUri != null) {
                    handleImageResult(logoUri);
                } else {
                    Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean granted = Boolean.TRUE.equals(permissions.get(Manifest.permission.CAMERA)) &&
                        (Boolean.TRUE.equals(permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE)) ||
                                Boolean.TRUE.equals(permissions.get(Manifest.permission.READ_MEDIA_IMAGES)));

                if (granted) {
                    openImageChooser();
                } else {
                    Toast.makeText(getContext(), "Camera & Storage permission required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Views
        ivProfileLogo = view.findViewById(R.id.ivProfileLogo);
        ivCameraIcon = view.findViewById(R.id.ivCameraIcon);
        etName = view.findViewById(R.id.etName);
        etBusinessName = view.findViewById(R.id.etBusinessName);
        etMobile = view.findViewById(R.id.etMobile);
        etAddress = view.findViewById(R.id.etAddress);
        etGstin = view.findViewById(R.id.etGstin);
        etEmail = view.findViewById(R.id.etEmail);
        progressBarProfile = view.findViewById(R.id.progressBarProfile);

        view.findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());

        // Click on logo or camera icon
        View.OnClickListener photoClick = v -> requestPermissionsAndOpenPicker();
        ivProfileLogo.setOnClickListener(photoClick);
        ivCameraIcon.setOnClickListener(photoClick);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        String encodedEmail = currentUser.getEmail().replace(".", ",");
        userRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(encodedEmail)
                .child("profile");

        logoRef = FirebaseStorage.getInstance()
                .getReference("profile_logos")
                .child(encodedEmail + ".jpg");

        loadProfileData(); // Load name, business, logo etc.

        return view;
    }

    private void requestPermissionsAndOpenPicker() {
        permissionLauncher.launch(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
    }

    private void openImageChooser() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Add Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else if (which == 1) galleryLauncher.launch("image/*");
                    else dialog.dismiss();
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) == null) {
            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            logoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.sandhyasofttechh.mykhatapro.fileprovider",  // 100% MATCHES MANIFEST & PDF
                    photoFile
            );
            cameraLauncher.launch(logoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PROFILE_" + timeStamp;
        File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void handleImageResult(@Nullable Uri uri) {
        if (uri != null && isAdded()) {
            logoUri = uri;
            Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .placeholder(R.drawable.img)
                    .into(ivProfileLogo);
        }
    }

    // SAFELY LOAD PROFILE DATA + LOGO (NO GLIDE CRASH)
    private void loadProfileData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Prevent crash if fragment detached

                etName.setText(getString(snapshot, "name"));
                etBusinessName.setText(getString(snapshot, "businessName"));
                etMobile.setText(getString(snapshot, "mobile"));
                etAddress.setText(getString(snapshot, "address"));
                etGstin.setText(getString(snapshot, "gstin"));
                etEmail.setText(getString(snapshot, "email", mAuth.getCurrentUser().getEmail()));

                String logoUrl = snapshot.child("logoUrl").getValue(String.class);
                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(logoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.img)
                            .error(R.drawable.img)
                            .into(ivProfileLogo);
                } else {
                    ivProfileLogo.setImageResource(R.drawable.img);
                }
            }

            private String getString(DataSnapshot snap, String key) {
                return getString(snap, key, "");
            }

            private String getString(DataSnapshot snap, String key, String fallback) {
                String val = snap.child(key).getValue(String.class);
                return val != null ? val : fallback;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        progressBarProfile.setVisibility(View.VISIBLE);
        if (logoUri != null) {
            uploadLogoAndSave(name);
        } else {
            saveTextOnly(name);
        }
    }

    private void uploadLogoAndSave(String name) {
        logoRef.putFile(logoUri)
                .addOnSuccessListener(taskSnapshot -> logoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveProfileToDatabase(uri.toString(), name);
                }))
                .addOnFailureListener(e -> {
                    progressBarProfile.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveTextOnly(String name) {
        saveProfileToDatabase(null, name);
    }

    private void saveProfileToDatabase(String logoUrl, String name) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("businessName", etBusinessName.getText().toString().trim());
        updates.put("mobile", etMobile.getText().toString().trim());
        updates.put("address", etAddress.getText().toString().trim());
        updates.put("gstin", etGstin.getText().toString().trim());
        updates.put("email", mAuth.getCurrentUser().getEmail());
        updates.put("uid", mAuth.getCurrentUser().getUid());
        updates.put("status", true);
        if (logoUrl != null) {
            updates.put("logoUrl", logoUrl);
        }

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBarProfile.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Profile saved successfully!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    progressBarProfile.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}