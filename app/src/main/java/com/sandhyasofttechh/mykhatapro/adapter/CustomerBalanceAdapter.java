package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.fragments.CustomerListFragment;

import java.util.List;

public class CustomerBalanceAdapter extends RecyclerView.Adapter<CustomerBalanceAdapter.CustomerViewHolder> {

    private final List<CustomerListFragment.CustomerSummary> customers;

    public CustomerBalanceAdapter(List<CustomerListFragment.CustomerSummary> customers) {
        this.customers = customers;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_balance, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        CustomerListFragment.CustomerSummary customer = customers.get(position);
        holder.tvName.setText(customer.name);
        holder.tvPhone.setText(customer.phone);
        holder.tvBalance.setText(String.format("₹%.2f", customer.balance));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone, tvBalance;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}
