package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Customer;
import java.util.List;

public class TopCustomerAdapter extends RecyclerView.Adapter<TopCustomerAdapter.ViewHolder> {
    private final List<Customer> customers;

    public TopCustomerAdapter(List<Customer> customers) {
        this.customers = customers;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer c = customers.get(position);
        holder.tvName.setText(c.getName());
        holder.tvDue.setText(String.format("₹%.2f", c.getPendingAmount()));
        
    }


    @Override public int getItemCount() { return customers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDue;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_customer_name);
            tvDue = itemView.findViewById(R.id.tv_due_amount);
        }
    }
}