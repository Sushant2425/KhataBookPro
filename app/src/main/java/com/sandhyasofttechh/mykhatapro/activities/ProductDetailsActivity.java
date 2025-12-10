//package com.sandhyasofttechh.mykhatapro.activities;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Toast;
//import android.widget.TextView;
//import android.widget.ImageView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.google.firebase.database.*;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.adapter.StockHistoryAdapter;
//import com.sandhyasofttechh.mykhatapro.model.Product;
//import com.sandhyasofttechh.mykhatapro.model.StockHistory;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class ProductDetailsActivity extends AppCompatActivity {
//
//    Product product;
//    DatabaseReference historyRef;
//
//    TextView tvName, tvSalePrice, tvPurchasePrice, tvStock, tvUnit,
//            tvStockValue, tvLowStock, tvHSN, tvGST, tvLossAmount, tvStockAdded, tvEditProduct;
//
//    ImageView btnBack;
//    RecyclerView rvHistory;
//
//    List<StockHistory> list = new ArrayList<>();
//    StockHistoryAdapter adapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_product_details);
//
//        product = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");
//
//        initViews();
//        setDetails();
//        fetchHistory(); // MAIN CALL
//    }
//
//    private void initViews() {
//        btnBack = findViewById(R.id.btnBack);
//        rvHistory = findViewById(R.id.rvHistory);
//
//        tvName = findViewById(R.id.tvName);
//        tvSalePrice = findViewById(R.id.tvSalePrice);
//        tvPurchasePrice = findViewById(R.id.tvPurchasePrice);
//        tvStock = findViewById(R.id.tvStock);
//        tvUnit = findViewById(R.id.tvUnit);
//        tvStockValue = findViewById(R.id.tvStockValue);
//        tvLowStock = findViewById(R.id.tvLowStock);
//        tvHSN = findViewById(R.id.tvHSN);
//        tvGST = findViewById(R.id.tvGST);
//        tvLossAmount = findViewById(R.id.tvLossAmount);
//        tvStockAdded = findViewById(R.id.tvStockAdded);
//        tvEditProduct = findViewById(R.id.tvEditProduct);
//
//        btnBack.setOnClickListener(v -> finish());
//
//        tvEditProduct.setOnClickListener(v -> openEditProduct());
//
//        rvHistory.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new StockHistoryAdapter(this, list, product); // ✅ PASS PRODUCT FOR HSN/GST FALLBACK
//        rvHistory.setAdapter(adapter);
//    }
//
//    private void openEditProduct() {
//        Intent intent = new Intent(ProductDetailsActivity.this, AddProductActivity.class);
//        intent.putExtra("IS_EDIT_MODE", "YES");
//        intent.putExtra("PRODUCT_ID", product.getProductId());
//        intent.putExtra("PRODUCT_DATA_FULL", product);
//        startActivity(intent);
//    }
//
//    private void setDetails() {
//        tvName.setText(product.getName());
//        tvSalePrice.setText("₹" + product.getSalePrice());
//        tvPurchasePrice.setText("₹" + product.getPurchasePrice());
//
//        tvStock.setText(product.getCurrentStock());
//        tvUnit.setText(product.getUnit());
//        tvLowStock.setText(product.getLowStockAlert());
//
//        double stockValue = product.getCurrentStockDouble() * product.getPurchasePriceDouble();
//        tvStockValue.setText("₹" + String.format("%.2f", stockValue));
//
//        // ✅ PROPER HSN/GST DISPLAY
//        tvHSN.setText(product.getHsn() != null ? product.getHsn() : "N/A");
//        tvGST.setText((product.getGst() != null ? product.getGst() : "0") + "%");
//
//        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
//            CircleImageView img = findViewById(R.id.imgProduct);
//            Glide.with(this).load(product.getImageUrl()).into(img);
//        }
//    }
//
//    private void fetchHistory() {
//        PrefManager pref = new PrefManager(this);
//
//        String emailNode = pref.getUserEmail().replace(".", ",");
//        String shopId = pref.getCurrentShopId();
//
//        DatabaseReference baseRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(emailNode);
//
//        // ✔ If shop exists → get under shop
//        if (shopId != null && !shopId.trim().isEmpty()) {
//            historyRef = baseRef.child("shops")
//                    .child(shopId)
//                    .child("history")
//                    .child(product.getProductId());
//        }
//        // ✔ If no shop exists → get direct history
//        else {
//            historyRef = baseRef.child("history")
//                    .child(product.getProductId());
//        }
//
//        historyRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                list.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    StockHistory sh = ds.getValue(StockHistory.class);
//                    if (sh != null) {
//                        // ✅ CRITICAL FIX: Populate HSN/GST from Product if missing in history
//                        if (sh.getHsn() == null || sh.getHsn().trim().isEmpty()) {
//                            sh.setHsn(product.getHsn());
//                        }
//                        if (sh.getGst() == null || sh.getGst().trim().isEmpty()) {
//                            sh.setGst(product.getGst());
//                        }
//                        list.add(0, sh);
//                    }
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Toast.makeText(ProductDetailsActivity.this,
//                        error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}





package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.StockHistoryAdapter;
import com.sandhyasofttechh.mykhatapro.model.Product;
import com.sandhyasofttechh.mykhatapro.model.StockHistory;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductDetailsActivity extends AppCompatActivity {

    Product product;
    DatabaseReference historyRef;

    TextView tvName, tvSalePrice, tvPurchasePrice, tvStock, tvUnit,
            tvStockValue, tvLowStock, tvHSN, tvGST, tvLossAmount, tvStockAdded, tvEditProduct,
            tvTotalAdded, tvTotalReduced, tvNetStockChange; // ✅ NEW SUMMARY TEXTVIEWS

    ImageView btnBack;
    RecyclerView rvHistory;

    List<StockHistory> list = new ArrayList<>();
    StockHistoryAdapter adapter;

    double totalAdded = 0, totalReduced = 0; // ✅ SUMMARY CALCULATIONS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        product = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");

        initViews();
        setDetails();
        fetchHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvHistory = findViewById(R.id.rvHistory);

        tvName = findViewById(R.id.tvName);
        tvSalePrice = findViewById(R.id.tvSalePrice);
        tvPurchasePrice = findViewById(R.id.tvPurchasePrice);
        tvStock = findViewById(R.id.tvStock);
        tvUnit = findViewById(R.id.tvUnit);
        tvStockValue = findViewById(R.id.tvStockValue);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvHSN = findViewById(R.id.tvHSN);
        tvGST = findViewById(R.id.tvGST);
        tvLossAmount = findViewById(R.id.tvLossAmount);
        tvStockAdded = findViewById(R.id.tvStockAdded);
        tvEditProduct = findViewById(R.id.tvEditProduct);

        // ✅ NEW SUMMARY VIEWS
        tvTotalAdded = findViewById(R.id.tvTotalAdded);
        tvTotalReduced = findViewById(R.id.tvTotalReduced);
        tvNetStockChange = findViewById(R.id.tvNetStockChange);

        btnBack.setOnClickListener(v -> finish());
        tvEditProduct.setOnClickListener(v -> openEditProduct());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StockHistoryAdapter(this, list, product);
        rvHistory.setAdapter(adapter);
    }

    private void openEditProduct() {
        Intent intent = new Intent(ProductDetailsActivity.this, AddProductActivity.class);
        intent.putExtra("IS_EDIT_MODE", "YES");
        intent.putExtra("PRODUCT_ID", product.getProductId());
        intent.putExtra("PRODUCT_DATA_FULL", product);
        startActivity(intent);
    }

    private void setDetails() {
        tvName.setText(product.getName());
        tvSalePrice.setText("₹" + product.getSalePrice());
        tvPurchasePrice.setText("₹" + product.getPurchasePrice());

        tvStock.setText(product.getCurrentStock());
        tvUnit.setText(product.getUnit());
        tvLowStock.setText(product.getLowStockAlert());

        double stockValue = product.getCurrentStockDouble() * product.getPurchasePriceDouble();
        tvStockValue.setText("₹" + String.format("%.2f", stockValue));

        tvHSN.setText(product.getHsn() != null ? product.getHsn() : "N/A");
        tvGST.setText((product.getGst() != null ? product.getGst() : "0") + "%");

        // ✅ INITIAL SUMMARY (will update with data)
        updateSummaryDisplay();

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            CircleImageView img = findViewById(R.id.imgProduct);
            Glide.with(this).load(product.getImageUrl()).into(img);
        }
    }

    // ✅ NEW: Calculate Totals Method
    private void calculateStockSummary() {
        totalAdded = 0;
        totalReduced = 0;

        for (StockHistory sh : list) {
            double qty = sh.getQuantityDouble();
            String type = sh.getType() != null ? sh.getType().toLowerCase() : "";

            boolean isStockOut = type.contains("out") || type.contains("sale") ||
                    type.contains("damage") || type.contains("loss");

            if (isStockOut) {
                totalReduced += qty;
            } else {
                totalAdded += qty;
            }
        }

        updateSummaryDisplay();
    }

    // ✅ NEW: Update Summary UI
    private void updateSummaryDisplay() {
        tvTotalAdded.setText("+" + String.format("%.2f", totalAdded) + " " + product.getUnit());
        tvTotalReduced.setText("-" + String.format("%.2f", totalReduced) + " " + product.getUnit());

        double netChange = totalAdded - totalReduced;
        String netText = netChange >= 0 ?
                "+" + String.format("%.2f", netChange) :
                String.format("%.2f", netChange);
        tvNetStockChange.setText("Net: " + netText + " " + product.getUnit());

        // ✅ COLOR CODING
        tvTotalAdded.setTextColor(getColor(android.R.color.holo_green_dark));
        tvTotalReduced.setTextColor(getColor(android.R.color.holo_red_dark));
        tvNetStockChange.setTextColor(netChange >= 0 ?
                getColor(android.R.color.holo_green_dark) :
                getColor(android.R.color.holo_red_dark));
    }

    private void fetchHistory() {
        PrefManager pref = new PrefManager(this);

        String emailNode = pref.getUserEmail().replace(".", ",");
        String shopId = pref.getCurrentShopId();

        DatabaseReference baseRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailNode);

        if (shopId != null && !shopId.trim().isEmpty()) {
            historyRef = baseRef.child("shops")
                    .child(shopId)
                    .child("history")
                    .child(product.getProductId());
        } else {
            historyRef = baseRef.child("history")
                    .child(product.getProductId());
        }

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    StockHistory sh = ds.getValue(StockHistory.class);
                    if (sh != null) {
                        if (sh.getHsn() == null || sh.getHsn().trim().isEmpty()) {
                            sh.setHsn(product.getHsn());
                        }
                        if (sh.getGst() == null || sh.getGst().trim().isEmpty()) {
                            sh.setGst(product.getGst());
                        }
                        list.add(0, sh);
                    }
                }

                // ✅ CRITICAL: Recalculate summary after data load
                calculateStockSummary();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductDetailsActivity.this,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
