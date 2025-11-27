package com.sandhyasofttechh.mykhatapro.register;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final List<SwitchShopActivity.ShopItem> shopList;
    private final OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(SwitchShopActivity.ShopItem shop);
    }

    public ShopAdapter(List<SwitchShopActivity.ShopItem> list, OnShopClickListener listener) {
        this.shopList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        SwitchShopActivity.ShopItem shop = shopList.get(position);

        holder.tvShopName.setText(shop.name);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onShopClick(shop);
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    // -------------------- ViewHolder ---------------------

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
        }
    }
}
