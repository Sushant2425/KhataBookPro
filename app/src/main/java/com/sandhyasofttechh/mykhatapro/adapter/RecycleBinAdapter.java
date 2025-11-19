package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.ViewHolder> {

    public interface OnTransactionActionListener {
        void onUndoClicked(Transaction transaction);
        void onDeleteClicked(Transaction transaction);
    }

    private final List<Transaction> transactions;
    private final OnTransactionActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public RecycleBinAdapter(List<Transaction> transactions, OnTransactionActionListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecycleBinAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_bin_transaction, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull RecycleBinAdapter.ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        String dateTimeStr = sdf.format(transaction.getTimestamp());
        String customerName = transaction.getCustomerName() != null ? transaction.getCustomerName() : "Unknown";
        String amountStr = String.format(Locale.getDefault(), "₹%.2f", transaction.getAmount());
        boolean gave = "gave".equalsIgnoreCase(transaction.getType());

        holder.tvCustomerName.setText(customerName);
        holder.tvAmount.setText(amountStr);
        holder.tvDateTime.setText(dateTimeStr);

        if (gave) {
            holder.cashflowType.setText("You Gave");
            holder.cashflowType.setTextColor(holder.itemView.getResources().getColor(R.color.error));
        } else {
            holder.cashflowType.setText("You Got");
            holder.cashflowType.setTextColor(holder.itemView.getResources().getColor(R.color.green));
        }

        holder.btnUndo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUndoClicked(transaction);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvAmount, tvDateTime, cashflowType;
        Button btnUndo, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            cashflowType = itemView.findViewById(R.id.cashflow_type);
            btnUndo = itemView.findViewById(R.id.button_undo);
            btnDelete = itemView.findViewById(R.id.button_delete);
        }
    }

}

