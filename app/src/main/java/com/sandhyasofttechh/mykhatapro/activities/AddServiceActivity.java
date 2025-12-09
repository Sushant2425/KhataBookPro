package com.sandhyasofttechh.mykhatapro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddServiceActivity extends AppCompatActivity {

    private static final int PICK_GALLERY = 301;
    private static final int PICK_CAMERA = 302;
    private static final int PERMISSION_CODE = 303;

    EditText edtName, edtPrice, edtSac, edtGst;
    AutoCompleteTextView edtUnit;
    MaterialButton btnSave, btnAddSacGst;
    ImageView imgSelect;
    TextView txtAddPhoto;
    MaterialCardView cardUpload;
    LinearLayout layoutSacGst;

    PrefManager pref;
    ProgressDialog progressDialog;
    String uploadedImageUrl = "";
    Uri selectedImageUri;
    boolean isSacGstVisible = false;

    List<String> unitList = Arrays.asList(
            "Kilogram (kg)", "Gram (g)", "Milligram (mg)", "Quintal (q)", "Ton (t)", "Pound (lb)", "Ounce (oz)",
            "Liter (L)", "Milliliter (mL)", "Gallon (gal)", "Pint (pt)", "Fluid Ounce (fl oz)",
            "Meter (m)", "Centimeter (cm)", "Millimeter (mm)", "Kilometer (km)", "Inch (in)", "Foot (ft)", "Yard (yd)",
            "Piece (pcs)", "Dozen (dzn)", "Pair", "Set", "Unit",
            "Box", "Packet", "Bag", "Carton", "Bundle", "Roll", "Can", "Bottle", "Jar", "Pouch", "Container",
            "Square Meter (sq m)", "Square Foot (sq ft)", "Acre", "Hectare (ha)",
            "Plate", "Sheet", "Strip", "Vial", "Ampule"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        Toolbar toolbar = findViewById(R.id.toolbarAddService);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Service");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        pref = new PrefManager(this);
        initViews();

        // Remove autofocus from service name
        edtName.clearFocus();

        // Image selection
        cardUpload.setOnClickListener(v -> pickImageDialog());
        imgSelect.setOnClickListener(v -> pickImageDialog());

        // Unit selection with smooth animation
        edtUnit.setOnClickListener(v -> openUnitBottomSheet());

        // Toggle SAC & GST visibility
        btnAddSacGst.setOnClickListener(v -> toggleSacGstLayout());

        // Save button
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void initViews() {
        edtName = findViewById(R.id.edtServiceName);
        edtPrice = findViewById(R.id.edtServicePrice);
        edtUnit = findViewById(R.id.edtServiceUnit);
        edtSac = findViewById(R.id.edtSacCode);
        edtGst = findViewById(R.id.edtGst);
        btnSave = findViewById(R.id.btnSaveService);
        btnAddSacGst = findViewById(R.id.btnAddSacGst);
        imgSelect = findViewById(R.id.imgService);
        cardUpload = findViewById(R.id.cardUpload);
        layoutSacGst = findViewById(R.id.layoutSacGst);

        // Initialize txtAddPhoto if it exists
        try {
        } catch (Exception e) {
            txtAddPhoto = null;
        }
    }

    private void toggleSacGstLayout() {
        TransitionManager.beginDelayedTransition((ViewGroup) layoutSacGst.getParent(), new AutoTransition());

        if (isSacGstVisible) {
            layoutSacGst.setVisibility(View.GONE);
            btnAddSacGst.setText("+ Add SAC Code & GST");
            btnAddSacGst.setIconResource(android.R.drawable.ic_input_add);
            isSacGstVisible = false;
        } else {
            layoutSacGst.setVisibility(View.VISIBLE);
            btnAddSacGst.setText("- Remove SAC Code & GST");
            btnAddSacGst.setIconResource(android.R.drawable.ic_delete);
            isSacGstVisible = true;
            edtSac.requestFocus();
        }
    }

    private void openUnitBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottomsheet_units, null);
        bottomSheet.setContentView(sheet);

        EditText etSearch = sheet.findViewById(R.id.etSearchUnit);
        ListView listUnits = sheet.findViewById(R.id.listUnits);
        TextView txtCount = sheet.findViewById(R.id.txtUnitCount);

        txtCount.setText(unitList.size() + " units available");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, unitList);
        listUnits.setAdapter(adapter);

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        listUnits.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUnit = adapter.getItem(position);
            edtUnit.setText(selectedUnit);
            bottomSheet.dismiss();
        });

        bottomSheet.getBehavior().setPeekHeight(800);
        bottomSheet.show();
    }

    private void pickImageDialog() {
        String[] options = {"Camera", "Gallery"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) checkCameraPermission();
            else checkGalleryPermission();
        }).show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
        } else openCamera();
    }

    private void checkGalleryPermission() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_GALLERY);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_CAMERA) {
                handleCameraResult(data);
            } else if (requestCode == PICK_GALLERY) {
                handleGalleryResult(data);
            }
        }
    }

    private void handleCameraResult(Intent data) {
        try {
            android.graphics.Bitmap photo = (android.graphics.Bitmap) data.getExtras().get("data");
            if (photo != null) {
                imgSelect.setImageBitmap(photo);
                imgSelect.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgSelect.setPadding(0, 0, 0, 0);

                if (txtAddPhoto != null) {
                    txtAddPhoto.setVisibility(View.GONE);
                }

                // Save bitmap to temporary file
                File tempFile = saveBitmapToFile(photo);
                if (tempFile != null) {
                    selectedImageUri = Uri.fromFile(tempFile);
                    uploadImageToFirebase(selectedImageUri);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
            Log.e("CameraResult", "Error", e);
        }
    }

    private void handleGalleryResult(Intent data) {
        selectedImageUri = data.getData();
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(imgSelect);

            imgSelect.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgSelect.setPadding(0, 0, 0, 0);

            if (txtAddPhoto != null) {
                txtAddPhoto.setVisibility(View.GONE);
            }

            uploadImageToFirebase(selectedImageUri);
        }
    }

    private File saveBitmapToFile(android.graphics.Bitmap bitmap) {
        try {
            File folder = new File(getCacheDir(), "temp_images");
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, "service_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            Log.e("SaveBitmap", "Error", e);
            return null;
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "service_" + timestamp + ".jpg";

        String userEmail = pref.getUserEmail();
        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
        String shopId = pref.getCurrentShopId();

        StorageReference storageRef;
        if (shopId == null || shopId.trim().isEmpty()) {
            storageRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("ServiceImages")
                    .child(userNode)
                    .child(fileName);
        } else {
            storageRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("ServiceImages")
                    .child(userNode)
                    .child(shopId)
                    .child(fileName);
        }

        storageRef.putFile(imageUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading: " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadedImageUrl = uri.toString();

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        Toast.makeText(AddServiceActivity.this,
                                "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                        Log.d("ImageUpload", "Download URL: " + uploadedImageUrl);

                    }).addOnFailureListener(e -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(AddServiceActivity.this,
                                "Failed to get download URL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AddServiceActivity.this,
                            "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ImageUpload", "Upload Error", e);
                });
    }

    private void validateAndSave() {
        String name = edtName.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String unit = edtUnit.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Enter service name");
            edtName.requestFocus();
            return;
        }

        if (price.isEmpty()) {
            edtPrice.setError("Enter price");
            edtPrice.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show();
            edtUnit.requestFocus();
            return;
        }

        saveServiceToFirebase();
    }

    private void saveServiceToFirebase() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving service...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String emailKey = pref.getUserEmail().replace(".", ",");
        String shopId = pref.getCurrentShopId();

        DatabaseReference ref;

        if (shopId != null && !shopId.trim().isEmpty()) {
            ref = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId)
                    .child("services");
        } else {
            ref = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("services");
        }

        String id = ref.push().getKey();
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        HashMap<String, Object> map = new HashMap<>();
        map.put("serviceId", id);
        map.put("serviceName", edtName.getText().toString().trim());
        map.put("price", edtPrice.getText().toString().trim());
        map.put("unit", edtUnit.getText().toString().trim());
        map.put("sacCode", edtSac.getText().toString().trim());
        map.put("gst", edtGst.getText().toString().trim());
        map.put("imageUrl", uploadedImageUrl == null || uploadedImageUrl.isEmpty() ? "" : uploadedImageUrl);
        map.put("timestamp", System.currentTimeMillis());
        map.put("dateAdded", date);
        map.put("timeAdded", time);

        ref.child(id).setValue(map).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Service saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}