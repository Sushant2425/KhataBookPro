package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.ServiceModel;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    List<ServiceModel> list;
    OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onClick(ServiceModel model);
    }

    public ServiceAdapter(List<ServiceModel> list, OnServiceClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        ServiceModel m = list.get(pos);

        holder.txtName.setText(m.serviceName);
        holder.txtPrice.setText("₹" + m.price);
        holder.txtUnit.setText(m.unit);
        holder.txtGst.setText("GST: " + m.gst + "%");

        if (m.imageUrl != null && !m.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(m.imageUrl).into(holder.img);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(m);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int v) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);

        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPrice, txtUnit, txtGst;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtServiceName);
            txtPrice = itemView.findViewById(R.id.txtServicePrice);
            txtUnit = itemView.findViewById(R.id.txtServiceUnit);
            txtGst = itemView.findViewById(R.id.txtServiceGst);
            img = itemView.findViewById(R.id.imgServiceLogo);
        }
    }
}
