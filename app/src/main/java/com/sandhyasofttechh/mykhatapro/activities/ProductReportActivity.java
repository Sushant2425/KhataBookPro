package com.sandhyasofttechh.mykhatapro.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.*;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Product;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class ProductReportActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView tvTitle, tvTotalProducts, tvTotalQty, tvStockValue, tvSaleValue,
            tvLowStockCount, tvOutOfStock, tvReorderCost;

    DatabaseReference productsRef;
    List<Product> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_report);

        initViews();
        initFirebase();
        loadReportData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        tvStockValue = findViewById(R.id.tvStockValue);
        tvSaleValue = findViewById(R.id.tvSaleValue);
        tvLowStockCount = findViewById(R.id.tvLowStockCount);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvReorderCost = findViewById(R.id.tvReorderCost);

        tvTitle.setText("Stock Summary Report");

        btnBack.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        PrefManager pref = new PrefManager(this);
        String email = pref.getUserEmail();
        String shopId = pref.getCurrentShopId();

        if (email == null) {
            Toast.makeText(this, "No user found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String emailNode = email.replace(".", ",");

        if (shopId == null || shopId.trim().isEmpty()) {
            shopId = "defaultShop";
        }

        productsRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailNode)
                .child("shops")
                .child(shopId)
                .child("products");
    }

    private void loadReportData() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                productList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product p = ds.getValue(Product.class);
                    if (p != null) {
                        productList.add(p);
                    }
                }

                computeReports(productList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductReportActivity.this,
                        "Failed to load report: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void computeReports(List<Product> list) {

        int totalProducts = list.size();
        double totalQty = 0, totalStockValue = 0, totalSaleValue = 0;
        int lowStock = 0, outOfStock = 0;
        double reorderCost = 0;

        for (Product p : list) {
            double qty = p.getCurrentStockDouble();
            double purchasePrice = p.getPurchasePriceDouble();
            double salePrice = p.getSalePriceDouble();
            double alertQty = p.getLowStockAlertDouble();

            totalQty += qty;
            totalStockValue += qty * purchasePrice;
            totalSaleValue += qty * salePrice;

            if (qty == 0) outOfStock++;

            if (qty <= alertQty) {
                lowStock++;
                reorderCost += (alertQty - qty) * purchasePrice;
            }
        }

        tvTotalProducts.setText(String.valueOf(totalProducts));
        tvTotalQty.setText(String.valueOf(totalQty));
        tvStockValue.setText("₹" + String.format("%.2f", totalStockValue));
        tvSaleValue.setText("₹" + String.format("%.2f", totalSaleValue));
        tvLowStockCount.setText(String.valueOf(lowStock));
        tvOutOfStock.setText(String.valueOf(outOfStock));
        tvReorderCost.setText("₹" + String.format("%.2f", reorderCost));
    }
}
