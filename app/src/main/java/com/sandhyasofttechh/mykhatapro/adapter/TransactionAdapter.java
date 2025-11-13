package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> transactions;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.tvAmount.setText(String.format("₹%.2f", t.getAmount()));
        holder.tvNote.setText(t.getNote());
        holder.tvDate.setText(sdf.format(t.getTimestamp()));

        boolean isIncome = "got".equals(t.getType());
        holder.tvAmount.setTextColor(
                holder.itemView.getContext().getColor(isIncome ? R.color.green : R.color.error)
        );
        holder.tvType.setText(isIncome ? "You Got" : "You Gave");
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvType, tvNote, tvDate;

        ViewHolder(View v) {
            super(v);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvType = v.findViewById(R.id.tv_type);
            tvNote = v.findViewById(R.id.tv_note);
            tvDate = v.findViewById(R.id.tv_date);
        }
    }
}