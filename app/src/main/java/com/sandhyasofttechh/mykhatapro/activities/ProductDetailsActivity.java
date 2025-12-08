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
            tvStockValue, tvLowStock, tvHSN, tvGST, tvLossAmount, tvStockAdded, tvEditProduct;

    ImageView btnBack;
    RecyclerView rvHistory;

    List<StockHistory> list = new ArrayList<>();
    StockHistoryAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        product = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");

        initViews();
        setDetails();
        fetchHistory(); // MAIN CALL
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

        btnBack.setOnClickListener(v -> finish());

        tvEditProduct.setOnClickListener(v -> openEditProduct());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StockHistoryAdapter(this, list);
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

        tvHSN.setText(product.getHsn());
        tvGST.setText(product.getGst() + "%");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            CircleImageView img = findViewById(R.id.imgProduct);
            Glide.with(this).load(product.getImageUrl()).into(img);
        }
    }


    private void fetchHistory() {

        PrefManager pref = new PrefManager(this);

        String emailNode = pref.getUserEmail().replace(".", ",");
        String shopId = pref.getCurrentShopId();

        if (shopId == null || shopId.trim().isEmpty()) {
            shopId = "defaultShop";
        }

        historyRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailNode)
                .child("shops")
                .child(shopId)
                .child("history")
                .child(product.getProductId()); // IMPORTANT

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    StockHistory sh = ds.getValue(StockHistory.class);
                    if (sh != null) list.add(0, sh);
                }

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
