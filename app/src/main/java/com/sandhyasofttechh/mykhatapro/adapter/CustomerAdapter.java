//package com.sandhyasofttechh.mykhatapro.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Filter;
//import android.widget.Filterable;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.button.MaterialButton;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.model.Customer;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;

//public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> implements Filterable {
//
//    public interface CustomerActionListener {
//        void onEditClicked(Customer customer);
//        void onDeleteClicked(Customer customer);
//    }
//
//    private List<Customer> customers = new ArrayList<>();
//    private final List<Customer> customersFull = new ArrayList<>();
//    private final CustomerActionListener listener;
//
//    public CustomerAdapter(CustomerActionListener listener) {
//        this.listener = listener;
//    }
//
//    public void setCustomers(List<Customer> newList) {
//        customers.clear();
//        customers.addAll(newList);
//        customersFull.clear();
//        customersFull.addAll(newList);
//        notifyDataSetChanged();
//    }
//
//    public void filter(String query) {
//        getFilter().filter(query);
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_customer, parent, false);
//        return new ViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Customer c = customers.get(position);
//        holder.tvName.setText(c.getName());
//        holder.tvPhone.setText(c.getPhone());
//
//        holder.btnEdit.setOnClickListener(v -> listener.onEditClicked(c));
//        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(c));
//    }
//
//    @Override
//    public int getItemCount() {
//        return customers.size();
//    }
//
//    @Override
//    public Filter getFilter() {
//        return customerFilter;
//    }
//
//    private final Filter customerFilter = new Filter() {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            String pattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
//            List<Customer> filtered = new ArrayList<>();
//
//            if (pattern.isEmpty()) {
//                filtered.addAll(customersFull);
//            } else {
//                for (Customer c : customersFull) {
//                    if (c.getName().toLowerCase(Locale.getDefault()).contains(pattern) ||
//                            c.getPhone().toLowerCase(Locale.getDefault()).contains(pattern)) {
//                        filtered.add(c);
//                    }
//                }
//            }
//
//            FilterResults r = new FilterResults();
//            r.values = filtered;
//            return r;
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            customers.clear();
//            //noinspection unchecked
//            customers.addAll((List<Customer>) results.values);
//            notifyDataSetChanged();
//        }
//    };
//
//    // FIXED: Use MaterialButton
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView tvName, tvPhone;
//        MaterialButton btnEdit, btnDelete;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvName = itemView.findViewById(R.id.tv_customer_name);
//            tvPhone = itemView.findViewById(R.id.tv_customer_phone);
//            btnEdit = itemView.findViewById(R.id.btn_edit_customer);
//            btnDelete = itemView.findViewById(R.id.btn_delete_customer);
//        }
//    }
//}



package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> implements Filterable {

    public interface CustomerActionListener {
        void onEditClicked(Customer customer);
        void onDeleteClicked(Customer customer);
        void onWhatsAppClicked(Customer customer);
        void onSmsClicked(Customer customer);

        void onItemClicked(Customer customer);
    }

    private List<Customer> customers = new ArrayList<>();
    private final List<Customer> customersFull = new ArrayList<>();
    private final CustomerActionListener listener;

    public CustomerAdapter(CustomerActionListener listener) {
        this.listener = listener;
    }

    public void setCustomers(List<Customer> newList) {
        customers.clear();
        customers.addAll(newList);
        customersFull.clear();
        customersFull.addAll(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        getFilter().filter(query);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer c = customers.get(position);
        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone());

        double due = c.getPendingAmount();
        if (due > 0) {
            holder.tvPending.setText(String.format("₹%.2f due", due));
            holder.tvPending.setVisibility(View.VISIBLE);
            holder.btnWhatsApp.setVisibility(View.VISIBLE);
            holder.btnSms.setVisibility(View.VISIBLE);
        } else {
            holder.tvPending.setVisibility(View.GONE);
            holder.btnWhatsApp.setVisibility(View.GONE);
            holder.btnSms.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClicked(c));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(c));
        holder.btnWhatsApp.setOnClickListener(v -> listener.onWhatsAppClicked(c));
        holder.btnSms.setOnClickListener(v -> listener.onSmsClicked(c));
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(c));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    @Override
    public Filter getFilter() {
        return customerFilter;
    }

    private final Filter customerFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String pattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
            List<Customer> filtered = new ArrayList<>();
            if (pattern.isEmpty()) {
                filtered.addAll(customersFull);
            } else {
                for (Customer c : customersFull) {
                    if (c.getName().toLowerCase(Locale.getDefault()).contains(pattern) ||
                            c.getPhone().toLowerCase(Locale.getDefault()).contains(pattern)) {
                        filtered.add(c);
                    }
                }
            }
            FilterResults r = new FilterResults();
            r.values = filtered;
            return r;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            customers.clear();
            customers.addAll((List<Customer>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvPending;
        MaterialButton btnEdit, btnDelete, btnWhatsApp, btnSms;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_customer_name);
            tvPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvPending = itemView.findViewById(R.id.tv_pending_amount);
            btnEdit = itemView.findViewById(R.id.btn_edit_customer);
            btnDelete = itemView.findViewById(R.id.btn_delete_customer);
            btnWhatsApp = itemView.findViewById(R.id.btn_whatsapp);
            btnSms = itemView.findViewById(R.id.btn_sms);
        }
    }
}