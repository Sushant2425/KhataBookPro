package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.CollectionActivity;
import com.sandhyasofttechh.mykhatapro.model.CollectionModel;

import java.util.ArrayList;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    Context context;
    ArrayList<CollectionModel> list;
    CollectionActivity activity; // To call date picker from adapter

    public CollectionAdapter(Context context, ArrayList<CollectionModel> list) {
        this.context = context;
        this.list = list;
        if (context instanceof CollectionActivity)
            this.activity = (CollectionActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_collection_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionModel model = list.get(position);

        holder.txtName.setText(model.getName());
        holder.txtPhone.setText(model.getPhone());
        holder.txtDue.setText(String.format("₹%.2f", model.getPendingAmount()));
        holder.txtDate.setText(model.getFormattedDueDate());

        holder.btnSetDate.setOnClickListener(v -> {
            if (activity != null) {
                activity.showDatePicker(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone, txtDue, txtDate;
        MaterialButton btnSetDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCollectionName);
            txtPhone = itemView.findViewById(R.id.txtCollectionPhone);
            txtDue = itemView.findViewById(R.id.txtCollectionDue);
            txtDate = itemView.findViewById(R.id.txtCollectionDate);
            btnSetDate = itemView.findViewById(R.id.btnSetDate);
        }
    }
}
