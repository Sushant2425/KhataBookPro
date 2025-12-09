package com.sandhyasofttechh.mykhatapro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.ServiceModel;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServiceDetailActivity extends AppCompatActivity {

    // Detail views
    private ImageView imgDetailLogo;
    private TextView txtDetailName, txtDetailPrice, txtDetailUnit, txtDetailGst, txtDetailSac, txtDetailDate;
    private MaterialButton btnEditService;

    // Data
    private ServiceModel serviceObj;
    private DatabaseReference serviceRef;
    private PrefManager pref;
    private String emailKey = "", shopId = "";

    // Edit dialog
    private BottomSheetDialog editDialog;
    private Uri newImageUri = null;
    private ImageView imgEditService; // Inner ImageView
    private TextInputEditText edtEditName, edtEditPrice, edtEditUnit, edtEditGst, edtEditSac;
    private MaterialButton btnUpdateService, btnDeleteService;

    private static final int REQUEST_PICK_IMAGE = 5001;
    private static final String DATE_FORMAT = "dd MMM yyyy, HH:mm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        setupToolbar();
        initData();
        initViews();
        loadServiceData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarServiceDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Service Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initData() {
        pref = new PrefManager(this);
        emailKey = pref.getUserEmail().replace(".", ",");
        shopId = pref.getCurrentShopId();
        initFirebase();
    }

    private void initFirebase() {
        if (shopId == null || shopId.trim().isEmpty()) {
            serviceRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook").child(emailKey).child("services");
        } else {
            serviceRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook").child(emailKey).child("shops")
                    .child(shopId).child("services");
        }
    }

    private void initViews() {
        imgDetailLogo = findViewById(R.id.imgDetailLogo);
        txtDetailName = findViewById(R.id.txtDetailName);
        txtDetailPrice = findViewById(R.id.txtDetailPrice);
        txtDetailUnit = findViewById(R.id.txtDetailUnit);
        txtDetailGst = findViewById(R.id.txtDetailGst);
        txtDetailSac = findViewById(R.id.txtDetailSac);
        txtDetailDate = findViewById(R.id.txtDetailDate);
        btnEditService = findViewById(R.id.btnEditService);

        btnEditService.setOnClickListener(v -> showEditBottomSheet());
    }

    private void loadServiceData() {
        serviceObj = (ServiceModel) getIntent().getSerializableExtra("serviceData");
        if (serviceObj == null || TextUtils.isEmpty(serviceObj.serviceId)) {
            Toast.makeText(this, "No service data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        serviceRef.child(serviceObj.serviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                serviceObj = snapshot.getValue(ServiceModel.class);
                if (serviceObj != null) {
                    bindServiceData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServiceDetailActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindServiceData() {
        txtDetailName.setText(serviceObj.serviceName);
        txtDetailPrice.setText("₹ " + serviceObj.price);
        txtDetailUnit.setText(serviceObj.unit);
        txtDetailGst.setText(serviceObj.gst + "%");
        txtDetailSac.setText(serviceObj.sacCode);
        txtDetailDate.setText(formatDate(serviceObj.dateAdded));

        if (!TextUtils.isEmpty(serviceObj.imageUrl)) {
            Glide.with(this).load(serviceObj.imageUrl).placeholder(R.drawable.ic_camera).into(imgDetailLogo);
        } else {
            imgDetailLogo.setImageResource(R.drawable.ic_camera);
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(dateString);
            return sdf.format(date != null ? date : new Date());
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showEditBottomSheet() {
        editDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.bottomsheet_edit_service, null);
        editDialog.setContentView(dialogView);

        initEditDialogViews(dialogView);
        populateEditFields();
        setupEditDialogListeners();

        editDialog.show();
    }

    private void initEditDialogViews(View dialogView) {
        // ✅ FIXED: Handle MaterialCardView structure from professional XML
        MaterialCardView imgEditServiceCard = dialogView.findViewById(R.id.imgEditService);
        imgEditService = imgEditServiceCard.findViewById(R.id.image_service_icon); // Inner camera icon

        edtEditName = dialogView.findViewById(R.id.edtEditName);
        edtEditPrice = dialogView.findViewById(R.id.edtEditPrice);
        edtEditUnit = dialogView.findViewById(R.id.edtEditUnit);
        edtEditGst = dialogView.findViewById(R.id.edtEditGst);
        edtEditSac = dialogView.findViewById(R.id.edtEditSac);
        btnUpdateService = dialogView.findViewById(R.id.btnUpdateService);
        btnDeleteService = dialogView.findViewById(R.id.btnDeleteService);
    }

    private void populateEditFields() {
        edtEditName.setText(serviceObj.serviceName);
        edtEditPrice.setText(serviceObj.price);
        edtEditUnit.setText(serviceObj.unit);
        edtEditGst.setText(serviceObj.gst);
        edtEditSac.setText(serviceObj.sacCode);

        if (!TextUtils.isEmpty(serviceObj.imageUrl)) {
            Glide.with(this).load(serviceObj.imageUrl).into(imgEditService);
        }
    }

    private void setupEditDialogListeners() {
        // Click on card to pick image
        MaterialCardView imgEditServiceCard = editDialog.findViewById(R.id.imgEditService);
        imgEditServiceCard.setOnClickListener(v -> pickImage());

        btnUpdateService.setOnClickListener(v -> {
            if (validateInputs()) {
                btnUpdateService.setEnabled(false);
                uploadImageAndUpdate();
            }
        });
        btnDeleteService.setOnClickListener(v -> confirmDeleteService());
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(edtEditName.getText())) {
            edtEditName.setError("Service name required");
            edtEditName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(edtEditPrice.getText())) {
            edtEditPrice.setError("Price required");
            edtEditPrice.requestFocus();
            return false;
        }
        return true;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            newImageUri = data.getData();
            Glide.with(this).load(newImageUri).into(imgEditService);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndUpdate() {
        if (newImageUri == null) {
            performUpdate();
            return;
        }

        String fileName = "service_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("ServiceImages").child(emailKey).child(fileName);

        storageRef.putFile(newImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            serviceObj.imageUrl = uri.toString();
                            performUpdate();
                        })
                ).addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    btnUpdateService.setEnabled(true);
                });
    }

    private void performUpdate() {
        // Update all fields at once
        serviceObj.serviceName = edtEditName.getText().toString().trim();
        serviceObj.price = edtEditPrice.getText().toString().trim();
        serviceObj.unit = edtEditUnit.getText().toString().trim();
        serviceObj.gst = edtEditGst.getText().toString().trim();
        serviceObj.sacCode = edtEditSac.getText().toString().trim();

        serviceRef.child(serviceObj.serviceId).setValue(serviceObj)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Service updated successfully", Toast.LENGTH_SHORT).show();
                    if (editDialog != null) editDialog.dismiss();
                    loadServiceData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    btnUpdateService.setEnabled(true);
                });
    }

    private void confirmDeleteService() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage("This action cannot be undone. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> deleteService())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteService() {
        serviceRef.child(serviceObj.serviceId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                    if (editDialog != null) editDialog.dismiss();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editDialog != null && editDialog.isShowing()) {
            editDialog.dismiss();
        }
    }
}
