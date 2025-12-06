package com.sandhyasofttechh.mykhatapro.activities;

import android.os.Bundle;
import android.widget.*;
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

    TextView tvName,tvSalePrice,tvPurchasePrice,tvStock,tvUnit,tvStockValue,tvLowStock,tvHSN,tvGST,tvLossAmount,tvStockAdded;
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

        btnBack.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StockHistoryAdapter(this, list);
        rvHistory.setAdapter(adapter);
    }

    private void setDetails() {

        tvName.setText(product.getName());
        tvSalePrice.setText("Sale: ₹" + product.getSalePrice());
        tvPurchasePrice.setText("Purchase: ₹" + product.getPurchasePrice());
        tvStock.setText("Qty: " + product.getOpeningStock());
        tvUnit.setText("Unit: " + product.getUnit());
        tvLowStock.setText("Low Stock: " + product.getLowStockAlert());

        double stockValue = product.getOpeningStockDouble() * product.getPurchasePriceDouble();
        tvStockValue.setText("Stock Value: ₹" + stockValue);

        tvHSN.setText("HSN: " + product.getHsnCode());
        tvGST.setText("GST: " + product.getGstRate());

        tvLossAmount.setText("Total Loss: ₹0");
        tvStockAdded.setText("Added: 0");

        if(product.getImageUrl()!=null && !product.getImageUrl().isEmpty()){
            CircleImageView img = findViewById(R.id.imgProduct);

            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(img);
        }
    }

    private void fetchHistory() {

        PrefManager pref = new PrefManager(this);
        String emailKey = pref.getUserEmail().replace(".", ",");

        historyRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("history")
                .child(product.getProductId());

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    StockHistory sh = ds.getValue(StockHistory.class);
                    if(sh!=null) list.add(sh);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

    }

}
