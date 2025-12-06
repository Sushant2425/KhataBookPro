package com.sandhyasofttechh.mykhatapro.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Product;

public class ProductDetailsActivity extends AppCompatActivity {

    TextView tvName, tvUnit, tvStock, tvSalePrice, tvPurchasePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        Product product = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");

        tvName = findViewById(R.id.tvName);
        tvUnit = findViewById(R.id.tvUnit);
        tvStock = findViewById(R.id.tvStock);
        tvSalePrice = findViewById(R.id.tvSalePrice);
        tvPurchasePrice = findViewById(R.id.tvPurchasePrice);

        if (product != null) {
            tvName.setText(product.getName());
            tvUnit.setText("Unit: " + product.getUnit());
            tvStock.setText("Stock: " + product.getOpeningStock());
            tvSalePrice.setText("Sale Price: ₹" + product.getSalePrice());
            tvPurchasePrice.setText("Purchase Price: ₹" + product.getPurchasePrice());
        }
    }
}
