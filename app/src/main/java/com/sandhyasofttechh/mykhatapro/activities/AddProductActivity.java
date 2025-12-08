//package com.sandhyasofttechh.mykhatapro.activities;
//
//import android.Manifest;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.*;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import com.google.android.material.card.MaterialCardView;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class AddProductActivity extends AppCompatActivity {
//
//    private static final int PICK_IMAGE_REQUEST = 101;
//    private static final int PICK_CAMERA_REQUEST = 102;
//    private static final int PERMISSION_REQUEST_CODE = 103;
//
//    AutoCompleteTextView etUnit;
//    EditText etItemName, etSalePrice, etPurchasePrice, etOpeningStock, etLowStock, etHSN, etGST;
//    ImageView imgProduct;
//    Button btnSaveProduct;
//    MaterialCardView cardImageUpload;
//
//    DatabaseReference productRef;
//    PrefManager pref;
//    ProgressDialog progressDialog;
//
//    private Uri selectedImageUri = null;
//    private String uploadedImageUrl = null;
//
//    // Comprehensive unit list
//    List<String> unitList = Arrays.asList(
//            // Weight Units
//            "Kilogram (kg)",
//            "Gram (g)",
//            "Milligram (mg)",
//            "Quintal (q)",
//            "Ton (t)",
//            "Pound (lb)",
//            "Ounce (oz)",
//
//            // Volume Units
//            "Liter (L)",
//            "Milliliter (mL)",
//            "Gallon (gal)",
//            "Pint (pt)",
//            "Fluid Ounce (fl oz)",
//
//            // Length Units
//            "Meter (m)",
//            "Centimeter (cm)",
//            "Millimeter (mm)",
//            "Kilometer (km)",
//            "Inch (in)",
//            "Foot (ft)",
//            "Yard (yd)",
//
//            // Quantity Units
//            "Piece (pcs)",
//            "Dozen (dzn)",
//            "Pair",
//            "Set",
//            "Unit",
//
//            // Packaging Units
//            "Box",
//            "Packet",
//            "Bag",
//            "Carton",
//            "Bundle",
//            "Roll",
//            "Can",
//            "Bottle",
//            "Jar",
//            "Pouch",
//            "Container",
//
//            // Area Units
//            "Square Meter (sq m)",
//            "Square Foot (sq ft)",
//            "Acre",
//            "Hectare (ha)",
//
//            // Other
//            "Plate",
//            "Sheet",
//            "Strip",
//            "Vial",
//            "Ampule"
//    );
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_product);
//
//        pref = new PrefManager(this);
//
//        initViews();
//        initFirebase();
//        setupUnitDropdown();
//
//        findViewById(R.id.imgBack).setOnClickListener(v -> finish());
//        cardImageUpload.setOnClickListener(v -> showImagePickerDialog());
//        imgProduct.setOnClickListener(v -> showImagePickerDialog());
//        btnSaveProduct.setOnClickListener(v -> validateAndSaveProduct());
//    }
//
//    private void initViews() {
//        etItemName = findViewById(R.id.etItemName);
//        etUnit = findViewById(R.id.etUnit);
//        etSalePrice = findViewById(R.id.etSalePrice);
//        etPurchasePrice = findViewById(R.id.etPurchasePrice);
//        etOpeningStock = findViewById(R.id.etOpeningStock);
//        etLowStock = findViewById(R.id.etLowStock);
//        etHSN = findViewById(R.id.etHSN);
//        etGST = findViewById(R.id.etGST);
//        imgProduct = findViewById(R.id.imgProduct);
//        btnSaveProduct = findViewById(R.id.btnSaveProduct);
//        cardImageUpload = findViewById(R.id.cardImageUpload); // Add this ID to your layout
//    }
//
//    private void initFirebase() {
//        String email = pref.getUserEmail();
//        if (email == null) {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        String userNode = email.replace(".", ",");
//        String shopId = pref.getCurrentShopId();
//
//        if (shopId == null || shopId.trim().isEmpty()) {
//            // No shop selected - use root path
//            productRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(userNode)
//                    .child("products");
//        } else {
//            // Shop selected - use shop path
//            productRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(userNode)
//                    .child("shops")
//                    .child(shopId)
//                    .child("products");
//        }
//    }
//
//    private void setupUnitDropdown() {
//        // Make unit field non-editable and clickable
//        etUnit.setFocusable(false);
//        etUnit.setClickable(true);
//        etUnit.setOnClickListener(v -> openUnitBottomSheet());
//    }
//
//    private void openUnitBottomSheet() {
//        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
//                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
//
//        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_units, null);
//        bottomSheet.setContentView(sheetView);
//
//        EditText etSearchUnit = sheetView.findViewById(R.id.etSearchUnit);
//        ListView listUnits = sheetView.findViewById(R.id.listUnits);
//        TextView txtUnitCount = sheetView.findViewById(R.id.txtUnitCount);
//
//        // Create adapter
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_list_item_1,
//                new ArrayList<>(unitList)
//        );
//        listUnits.setAdapter(adapter);
//
//        // Update count
//        txtUnitCount.setText(unitList.size() + " units available");
//
//        // Search functionality
//        etSearchUnit.addTextChangedListener(new android.text.TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                adapter.getFilter().filter(s);
//            }
//
//            @Override
//            public void afterTextChanged(android.text.Editable s) {}
//        });
//
//        // Item selection
//        listUnits.setOnItemClickListener((parent, view, position, id) -> {
//            String selectedUnit = adapter.getItem(position);
//            etUnit.setText(selectedUnit);
//            bottomSheet.dismiss();
//        });
//
//        bottomSheet.show();
//    }
//
//    private void showImagePickerDialog() {
//        String[] options = {"Camera", "Gallery"};
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Image Source")
//                .setItems(options, (dialog, which) -> {
//                    if (which == 0) {
//                        checkPermissionAndOpenCamera();
//                    } else {
//                        checkPermissionAndOpenGallery();
//                    }
//                })
//                .show();
//    }
//
//    private void checkPermissionAndOpenCamera() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA},
//                    PERMISSION_REQUEST_CODE);
//        } else {
//            openCamera();
//        }
//    }
//
//    private void checkPermissionAndOpenGallery() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    PERMISSION_REQUEST_CODE);
//        } else {
//            openGallery();
//        }
//    }
//
//    private void openCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, PICK_CAMERA_REQUEST);
//        }
//    }
//
//    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && data != null) {
//            if (requestCode == PICK_CAMERA_REQUEST) {
//                handleCameraResult(data);
//            } else if (requestCode == PICK_IMAGE_REQUEST) {
//                handleGalleryResult(data);
//            }
//        }
//    }
//
//    private void handleCameraResult(Intent data) {
//        try {
//            android.graphics.Bitmap photo = (android.graphics.Bitmap) data.getExtras().get("data");
//            if (photo != null) {
//                imgProduct.setImageBitmap(photo);
//
//                // Save bitmap to temporary file
//                File tempFile = saveBitmapToFile(photo);
//                if (tempFile != null) {
//                    selectedImageUri = Uri.fromFile(tempFile);
//                    uploadImageToFirebase(selectedImageUri);
//                }
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
//            Log.e("CameraResult", "Error", e);
//        }
//    }
//
//    private void handleGalleryResult(Intent data) {
//        selectedImageUri = data.getData();
//        if (selectedImageUri != null) {
//            imgProduct.setImageURI(selectedImageUri);
//            uploadImageToFirebase(selectedImageUri);
//        }
//    }
//
//    private File saveBitmapToFile(android.graphics.Bitmap bitmap) {
//        try {
//            File folder = new File(getCacheDir(), "temp_images");
//            if (!folder.exists()) folder.mkdirs();
//
//            File file = new File(folder, "product_" + System.currentTimeMillis() + ".jpg");
//            FileOutputStream fos = new FileOutputStream(file);
//            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, fos);
//            fos.flush();
//            fos.close();
//            return file;
//        } catch (Exception e) {
//            Log.e("SaveBitmap", "Error", e);
//            return null;
//        }
//    }
//
//    private void uploadImageToFirebase(Uri imageUri) {
//        if (imageUri == null) {
//            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Uploading image...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        String fileName = "product_" + timestamp + ".jpg";
//
//        String userEmail = pref.getUserEmail();
//        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
//        String shopId = pref.getCurrentShopId();
//
//        StorageReference storageRef;
//        if (shopId == null || shopId.trim().isEmpty()) {
//            storageRef = FirebaseStorage.getInstance()
//                    .getReference()
//                    .child("ProductImages")
//                    .child(userNode)
//                    .child(fileName);
//        } else {
//            storageRef = FirebaseStorage.getInstance()
//                    .getReference()
//                    .child("ProductImages")
//                    .child(userNode)
//                    .child(shopId)
//                    .child(fileName);
//        }
//
//        storageRef.putFile(imageUri)
//                .addOnProgressListener(taskSnapshot -> {
//                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
//                            taskSnapshot.getTotalByteCount();
//                    progressDialog.setMessage("Uploading: " + (int) progress + "%");
//                })
//                .addOnSuccessListener(taskSnapshot -> {
//                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        uploadedImageUrl = uri.toString();
//
//                        if (progressDialog != null && progressDialog.isShowing()) {
//                            progressDialog.dismiss();
//                        }
//
//                        Toast.makeText(AddProductActivity.this,
//                                "Image uploaded successfully", Toast.LENGTH_SHORT).show();
//
//                        Log.d("ImageUpload", "Download URL: " + uploadedImageUrl);
//
//                    }).addOnFailureListener(e -> {
//                        if (progressDialog != null && progressDialog.isShowing()) {
//                            progressDialog.dismiss();
//                        }
//                        Toast.makeText(AddProductActivity.this,
//                                "Failed to get download URL: " + e.getMessage(),
//                                Toast.LENGTH_LONG).show();
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                    Toast.makeText(AddProductActivity.this,
//                            "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    Log.e("ImageUpload", "Upload Error", e);
//                });
//    }
//
//    private void validateAndSaveProduct() {
//        String name = etItemName.getText().toString().trim();
//        String unit = etUnit.getText().toString().trim();
//        String salePrice = etSalePrice.getText().toString().trim();
//        String purchasePrice = etPurchasePrice.getText().toString().trim();
//        String openingStock = etOpeningStock.getText().toString().trim();
//        String lowStock = etLowStock.getText().toString().trim();
//        String hsn = etHSN.getText().toString().trim();
//        String gst = etGST.getText().toString().trim();
//
//        // Validation
//        if (TextUtils.isEmpty(name)) {
//            etItemName.setError("Enter item name");
//            etItemName.requestFocus();
//            return;
//        }
//
//        if (TextUtils.isEmpty(unit)) {
//            etUnit.setError("Select unit");
//            etUnit.requestFocus();
//            return;
//        }
//
//        if (TextUtils.isEmpty(salePrice)) {
//            etSalePrice.setError("Enter sale price");
//            etSalePrice.requestFocus();
//            return;
//        }
//
//        // Save product
//        saveProduct(name, unit, salePrice, purchasePrice, openingStock, lowStock, hsn, gst);
//    }
//
//    private void saveProduct(String name, String unit, String salePrice, String purchasePrice,
//                             String openingStock, String lowStock, String hsn, String gst) {
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Saving product...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        String id = productRef.push().getKey();
//        if (id == null) {
//            id = String.valueOf(System.currentTimeMillis());
//        }
//
//        HashMap<String, Object> productMap = new HashMap<>();
//        productMap.put("productId", id);
//        productMap.put("name", name);
//        productMap.put("unit", unit);
//        productMap.put("salePrice", salePrice);
//        productMap.put("purchasePrice", purchasePrice.isEmpty() ? "0" : purchasePrice);
//        productMap.put("openingStock", openingStock.isEmpty() ? "0" : openingStock);
//        productMap.put("currentStock", openingStock.isEmpty() ? "0" : openingStock);
//        productMap.put("lowStockAlert", lowStock.isEmpty() ? "0" : lowStock);
//        productMap.put("hsn", hsn);
//        productMap.put("gst", gst.isEmpty() ? "0" : gst);
//
//        // Add image URL
//        productMap.put("imageUrl", uploadedImageUrl != null ? uploadedImageUrl : "");
//
//        // Add timestamp and date
//        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
//        productMap.put("dateAdded", date);
//        productMap.put("timestamp", System.currentTimeMillis());
//
//        productRef.child(id).setValue(productMap)
//                .addOnSuccessListener(unused -> {
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//
//                    Toast.makeText(this, "Product saved successfully!", Toast.LENGTH_SHORT).show();
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//
//                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    Log.e("SaveProduct", "Error", e);
//                });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//    }
//}


package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
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

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int PICK_CAMERA_REQUEST = 102;
    private static final int PERMISSION_REQUEST_CODE = 103;

    AutoCompleteTextView etUnit;
    EditText etItemName, etSalePrice, etPurchasePrice, etOpeningStock, etLowStock, etHSN, etGST;
    ImageView imgProduct;
    Button btnSaveProduct;
    MaterialCardView cardImageUpload;

    DatabaseReference productRef;
    PrefManager pref;
    ProgressDialog progressDialog;

    private Uri selectedImageUri = null;
    private String uploadedImageUrl = null;

    // Comprehensive unit list
    List<String> unitList = Arrays.asList(
            // Weight Units
            "Kilogram (kg)",
            "Gram (g)",
            "Milligram (mg)",
            "Quintal (q)",
            "Ton (t)",
            "Pound (lb)",
            "Ounce (oz)",

            // Volume Units
            "Liter (L)",
            "Milliliter (mL)",
            "Gallon (gal)",
            "Pint (pt)",
            "Fluid Ounce (fl oz)",

            // Length Units
            "Meter (m)",
            "Centimeter (cm)",
            "Millimeter (mm)",
            "Kilometer (km)",
            "Inch (in)",
            "Foot (ft)",
            "Yard (yd)",

            // Quantity Units
            "Piece (pcs)",
            "Dozen (dzn)",
            "Pair",
            "Set",
            "Unit",

            // Packaging Units
            "Box",
            "Packet",
            "Bag",
            "Carton",
            "Bundle",
            "Roll",
            "Can",
            "Bottle",
            "Jar",
            "Pouch",
            "Container",

            // Area Units
            "Square Meter (sq m)",
            "Square Foot (sq ft)",
            "Acre",
            "Hectare (ha)",

            // Other
            "Plate",
            "Sheet",
            "Strip",
            "Vial",
            "Ampule"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        pref = new PrefManager(this);

        initViews();
        initFirebase();
        setupUnitDropdown();

        findViewById(R.id.imgBack).setOnClickListener(v -> finish());
        cardImageUpload.setOnClickListener(v -> showImagePickerDialog());
        imgProduct.setOnClickListener(v -> showImagePickerDialog());
        btnSaveProduct.setOnClickListener(v -> validateAndSaveProduct());
    }

    private void initViews() {
        etItemName = findViewById(R.id.etItemName);
        etUnit = findViewById(R.id.etUnit);
        etSalePrice = findViewById(R.id.etSalePrice);
        etPurchasePrice = findViewById(R.id.etPurchasePrice);
        etOpeningStock = findViewById(R.id.etOpeningStock);
        etLowStock = findViewById(R.id.etLowStock);
        etHSN = findViewById(R.id.etHSN);
        etGST = findViewById(R.id.etGST);
        imgProduct = findViewById(R.id.imgProduct);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        cardImageUpload = findViewById(R.id.cardImageUpload); // Add this ID to your layout
    }

    private void initFirebase() {
        String email = pref.getUserEmail();
        if (email == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userNode = email.replace(".", ",");
        String shopId = pref.getCurrentShopId();

        if (shopId == null || shopId.trim().isEmpty()) {
            // No shop selected - use root path
            productRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("products");
        } else {
            // Shop selected - use shop path
            productRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("shops")
                    .child(shopId)
                    .child("products");
        }
    }

    private void setupUnitDropdown() {
        // Make unit field non-editable and clickable
        etUnit.setFocusable(false);
        etUnit.setClickable(true);
        etUnit.setOnClickListener(v -> openUnitBottomSheet());
    }

    private void openUnitBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_units, null);
        bottomSheet.setContentView(sheetView);

        EditText etSearchUnit = sheetView.findViewById(R.id.etSearchUnit);
        ListView listUnits = sheetView.findViewById(R.id.listUnits);
        TextView txtUnitCount = sheetView.findViewById(R.id.txtUnitCount);

        // Create custom adapter
        UnitAdapter adapter = new UnitAdapter(this, new ArrayList<>(unitList));
        listUnits.setAdapter(adapter);

        // Update count
        txtUnitCount.setText(unitList.size() + " units available");

        // Search functionality
        etSearchUnit.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Item selection
        listUnits.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUnit = adapter.getItem(position);
            etUnit.setText(selectedUnit);
            bottomSheet.dismiss();
            Toast.makeText(this, "Selected: " + selectedUnit, Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show();
    }

    // Custom Adapter for Unit List
    private class UnitAdapter extends ArrayAdapter<String> {
        private final android.content.Context context;
        private List<String> unitListData;
        private List<String> unitListDataFiltered;

        public UnitAdapter(android.content.Context context, List<String> units) {
            super(context, R.layout.item_unit_list, units);
            this.context = context;
            this.unitListData = units;
            this.unitListDataFiltered = new ArrayList<>(units);
        }

        @Override
        public int getCount() {
            return unitListDataFiltered.size();
        }

        @Override
        public String getItem(int position) {
            return unitListDataFiltered.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = android.view.LayoutInflater.from(context)
                        .inflate(R.layout.item_unit_list, parent, false);

                holder = new ViewHolder();
                holder.txtUnitName = convertView.findViewById(R.id.txtUnitName);
                holder.txtUnitCategory = convertView.findViewById(R.id.txtUnitCategory);
                holder.imgUnitIcon = convertView.findViewById(R.id.imgUnitIcon);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String unit = unitListDataFiltered.get(position);
            holder.txtUnitName.setText(unit);

            // Set category based on unit
            String category = getUnitCategory(unit);
            holder.txtUnitCategory.setText(category);

            // Set icon color based on category
            int iconColor = getCategoryColor(category);
            holder.imgUnitIcon.setColorFilter(iconColor);

            return convertView;
        }

        @NonNull
        @Override
        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<String> filteredList = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        filteredList.addAll(unitListData);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (String unit : unitListData) {
                            if (unit.toLowerCase().contains(filterPattern)) {
                                filteredList.add(unit);
                            }
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    unitListDataFiltered.clear();
                    unitListDataFiltered.addAll((List<String>) results.values);
                    notifyDataSetChanged();
                }
            };
        }

        private String getUnitCategory(String unit) {
            String unitLower = unit.toLowerCase();

            if (unitLower.contains("kg") || unitLower.contains("gram") ||
                    unitLower.contains("mg") || unitLower.contains("ton") ||
                    unitLower.contains("quintal") || unitLower.contains("pound") ||
                    unitLower.contains("ounce")) {
                return "Weight";
            } else if (unitLower.contains("liter") || unitLower.contains("ml") ||
                    unitLower.contains("gallon") || unitLower.contains("pint") ||
                    unitLower.contains("fluid")) {
                return "Volume";
            } else if (unitLower.contains("meter") || unitLower.contains("cm") ||
                    unitLower.contains("mm") || unitLower.contains("km") ||
                    unitLower.contains("inch") || unitLower.contains("foot") ||
                    unitLower.contains("yard")) {
                return "Length";
            } else if (unitLower.contains("piece") || unitLower.contains("dozen") ||
                    unitLower.contains("pair") || unitLower.contains("set") ||
                    unitLower.contains("unit")) {
                return "Quantity";
            } else if (unitLower.contains("box") || unitLower.contains("packet") ||
                    unitLower.contains("bag") || unitLower.contains("carton") ||
                    unitLower.contains("bundle") || unitLower.contains("roll") ||
                    unitLower.contains("can") || unitLower.contains("bottle") ||
                    unitLower.contains("jar") || unitLower.contains("pouch") ||
                    unitLower.contains("container")) {
                return "Packaging";
            } else if (unitLower.contains("square") || unitLower.contains("acre") ||
                    unitLower.contains("hectare")) {
                return "Area";
            } else {
                return "Other";
            }
        }

        private int getCategoryColor(String category) {
            switch (category) {
                case "Weight":
                    return 0xFF4CAF50; // Green
                case "Volume":
                    return 0xFF2196F3; // Blue
                case "Length":
                    return 0xFFFF9800; // Orange
                case "Quantity":
                    return 0xFF9C27B0; // Purple
                case "Packaging":
                    return 0xFFE91E63; // Pink
                case "Area":
                    return 0xFF00BCD4; // Cyan
                default:
                    return 0xFF757575; // Grey
            }
        }

        private class ViewHolder {
            TextView txtUnitName;
            TextView txtUnitCategory;
            ImageView imgUnitIcon;
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkPermissionAndOpenCamera();
                    } else {
                        checkPermissionAndOpenGallery();
                    }
                })
                .show();
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_CAMERA_REQUEST);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_CAMERA_REQUEST) {
                handleCameraResult(data);
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                handleGalleryResult(data);
            }
        }
    }

    private void handleCameraResult(Intent data) {
        try {
            android.graphics.Bitmap photo = (android.graphics.Bitmap) data.getExtras().get("data");
            if (photo != null) {
                imgProduct.setImageBitmap(photo);

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
            imgProduct.setImageURI(selectedImageUri);
            uploadImageToFirebase(selectedImageUri);
        }
    }

    private File saveBitmapToFile(android.graphics.Bitmap bitmap) {
        try {
            File folder = new File(getCacheDir(), "temp_images");
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, "product_" + System.currentTimeMillis() + ".jpg");
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
        String fileName = "product_" + timestamp + ".jpg";

        String userEmail = pref.getUserEmail();
        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
        String shopId = pref.getCurrentShopId();

        StorageReference storageRef;
        if (shopId == null || shopId.trim().isEmpty()) {
            storageRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("ProductImages")
                    .child(userNode)
                    .child(fileName);
        } else {
            storageRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("ProductImages")
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

                        Toast.makeText(AddProductActivity.this,
                                "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                        Log.d("ImageUpload", "Download URL: " + uploadedImageUrl);

                    }).addOnFailureListener(e -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(AddProductActivity.this,
                                "Failed to get download URL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AddProductActivity.this,
                            "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ImageUpload", "Upload Error", e);
                });
    }

    private void validateAndSaveProduct() {
        String name = etItemName.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String salePrice = etSalePrice.getText().toString().trim();
        String purchasePrice = etPurchasePrice.getText().toString().trim();
        String openingStock = etOpeningStock.getText().toString().trim();
        String lowStock = etLowStock.getText().toString().trim();
        String hsn = etHSN.getText().toString().trim();
        String gst = etGST.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etItemName.setError("Enter item name");
            etItemName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(unit)) {
            etUnit.setError("Select unit");
            etUnit.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(salePrice)) {
            etSalePrice.setError("Enter sale price");
            etSalePrice.requestFocus();
            return;
        }

        // Save product
        saveProduct(name, unit, salePrice, purchasePrice, openingStock, lowStock, hsn, gst);
    }

    private void saveProduct(String name, String unit, String salePrice, String purchasePrice,
                             String openingStock, String lowStock, String hsn, String gst) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving product...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PrefManager pref = new PrefManager(this);

        String emailNode = pref.getUserEmail().replace(".", ",");
        String shopId = pref.getCurrentShopId();

        // Base path always → shops → shopId
        DatabaseReference baseRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailNode)
                .child("shops");

        // Default shop handling
        if (shopId == null || shopId.trim().isEmpty()) {
            shopId = "defaultShop";
        }

        DatabaseReference productRef = baseRef.child(shopId).child("products");

        String id = productRef.push().getKey();
        if (id == null) id = "" + System.currentTimeMillis();

        String dateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("productId", id);
        productMap.put("name", name);
        productMap.put("unit", unit);
        productMap.put("salePrice", salePrice.isEmpty() ? "0" : salePrice);
        productMap.put("purchasePrice", purchasePrice.isEmpty() ? "0" : purchasePrice);
        productMap.put("openingStock", openingStock.isEmpty() ? "0" : openingStock);
        productMap.put("currentStock", openingStock.isEmpty() ? "0" : openingStock);
        productMap.put("lowStockAlert", lowStock.isEmpty() ? "0" : lowStock);
        productMap.put("hsn", hsn);
        productMap.put("gst", gst.isEmpty() ? "0" : gst);
        productMap.put("timeAdded", timeStr);
        productMap.put("dateAdded", dateStr);
        productMap.put("timestamp", timestamp);

        productMap.put("imageUrl", uploadedImageUrl == null ? "" : uploadedImageUrl);

        String finalId = id;
        String finalShopId = shopId;

        productRef.child(id).setValue(productMap)
                .addOnSuccessListener(unused -> {

                    saveOpeningStockHistory(finalId, name, openingStock, unit, finalShopId);

                    progressDialog.dismiss();
                    Toast.makeText(this, "Product saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveOpeningStockHistory(String productId, String productName,
                                         String openingStock, String unit, String shopId) {

        if (openingStock == null || openingStock.isEmpty() || openingStock.equals("0"))
            return;

        PrefManager pref = new PrefManager(this);
        String emailKey = pref.getUserEmail().replace(".", ",");

        // ALWAYS ensure correct shopId assigned
        if (shopId == null || shopId.trim().isEmpty()) {
            shopId = "defaultShop";
        }

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("shops")        // ✔ correct
                .child(shopId)         // ✔ correct
                .child("history")      // ✔ correct
                .child(productId);     // ✔ correct

        String historyId = historyRef.push().getKey();

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        HashMap<String, Object> map = new HashMap<>();
        map.put("historyId", historyId);
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("type", "IN");
        map.put("quantity", openingStock);
        map.put("price", "0");
        map.put("oldStock", "0");
        map.put("newStock", openingStock);
        map.put("unit", unit);
        map.put("note", "Opening Stock Added");
        map.put("date", date);
        map.put("time", time);
        map.put("timestamp", System.currentTimeMillis());

        historyRef.child(historyId).setValue(map);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}