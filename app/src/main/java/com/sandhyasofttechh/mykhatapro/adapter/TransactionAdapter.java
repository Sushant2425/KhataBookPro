package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// **IMPORTANT**: This adapter now works with a List<Transaction> again.
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
        Context context = holder.itemView.getContext();

        holder.tvCustomerName.setText(t.getCustomerName());
        holder.tvNote.setText(t.getNote());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", t.getAmount()));
        holder.tvDate.setText(t.getDate());
        holder.tvRelativeTime.setText(getCustomRelativeTime(t.getDate()));

        boolean isGot = "got".equals(t.getType());
        int color = isGot ? R.color.green : R.color.error;

        holder.tvAmount.setTextColor(ContextCompat.getColor(context, color));
        holder.viewTypeIndicator.setBackgroundColor(ContextCompat.getColor(context, color));

        // You can add an OnClickListener here if you want to open details
        // holder.itemView.setOnClickListener(v -> { ... });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String getCustomRelativeTime(String dateString) {
        if (dateString == null) return "";
        try {
            Date date = sdf.parse(dateString);
            if (date == null) return "";
            
            if (DateUtils.isToday(date.getTime())) {
                return "(Today)";
            } else {
                return "(" + DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS) + ")";
            }
        } catch (ParseException e) {
            Log.e("TransactionAdapter", "Date parsing error", e);
            return "";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewTypeIndicator;
        TextView tvCustomerName, tvNote, tvDate, tvRelativeTime, tvAmount;

        ViewHolder(View v) {
            super(v);
            viewTypeIndicator = v.findViewById(R.id.view_type_indicator);
            tvCustomerName = v.findViewById(R.id.tv_customer_name);
            tvNote = v.findViewById(R.id.tv_note);
            tvDate = v.findViewById(R.id.tv_date);
            tvRelativeTime = v.findViewById(R.id.tv_relative_time);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }
}