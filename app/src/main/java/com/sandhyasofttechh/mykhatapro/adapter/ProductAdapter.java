package com.sandhyasofttechh.mykhatapro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.ProductDetailsActivity;
import com.sandhyasofttechh.mykhatapro.model.Product;

import java.io.Serializable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import android.widget.TextView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductVH> {

    private final Context context;
    private final List<Product> productList;

    public interface StockActionListener {
        void onStockIn(Product product);
        void onStockOut(Product product);
    }

    private StockActionListener actionListener;

    public void setStockActionListener(StockActionListener listener) {
        this.actionListener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductVH(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProductVH h, int pos) {

        Product p = productList.get(pos);

        // Set values
        h.tvName.setText(p.getName());
        h.tvUnit.setText("Unit : " + p.getUnit());
        h.tvStock.setText("Stock : " + p.getOpeningStock());
        h.tvPrices.setText("Sale ₹" + p.getSalePrice() + " | Purchase ₹" + p.getPurchasePrice());

        // Initial
        if (p.getName() != null && p.getName().length() > 0) {
            String initial = p.getName().substring(0, 1).toUpperCase();
            h.tvInitial.setText(initial);
        }

        // Image Logic
        if (p.getImageUrl() != null && !p.getImageUrl().trim().isEmpty()) {
            h.tvInitial.setVisibility(View.GONE);
            h.imgProduct.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(p.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(h.imgProduct);
        } else {
            h.imgProduct.setVisibility(View.GONE);
            h.tvInitial.setVisibility(View.VISIBLE);
        }

        // Item Click → Open Details Screen
        h.cardView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProductDetailsActivity.class);
            i.putExtra("PRODUCT_DATA", (Serializable) p);
            context.startActivity(i);
        });

        // Stock Actions
        h.btnIn.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onStockIn(p);
            }
        });

        h.btnOut.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onStockOut(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductVH extends RecyclerView.ViewHolder {

        TextView tvInitial, tvName, tvStock, tvUnit, tvPrices;
        MaterialButton btnIn, btnOut;
        CircleImageView imgProduct;
        MaterialCardView cardView;

        public ProductVH(@NonNull View v) {
            super(v);
            tvInitial = v.findViewById(R.id.tvInitial);
            imgProduct = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvStock = v.findViewById(R.id.tvStock);
            tvUnit = v.findViewById(R.id.tvUnit);
            tvPrices = v.findViewById(R.id.tvPrices);
            btnIn = v.findViewById(R.id.btnStockIn);
            btnOut = v.findViewById(R.id.btnStockOut);
            cardView = v.findViewById(R.id.cardItemRoot);
        }
    }
}
