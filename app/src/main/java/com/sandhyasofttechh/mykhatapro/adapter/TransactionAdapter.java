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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transaction> transactions;
    private final OnItemClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, OnItemClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
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

        // Set Texts
        holder.tvNote.setText(t.getNote());
        holder.tvDate.setText(t.getDate());
        holder.tvRelativeTime.setText(getCustomRelativeTime(t.getDate()));
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", t.getAmount()));

        // Set Type and Colors
        boolean isGave = "gave".equals(t.getType());
        if (isGave) {
            holder.tvType.setText("You Gave");
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else {
            holder.tvType.setText("You Got");
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }
    
    private String getCustomRelativeTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            Date date = sdf.parse(dateString);
            if (date == null) return "";
            if (DateUtils.isToday(date.getTime())) return "(Today)";
            long now = System.currentTimeMillis();
            long diff = now - date.getTime();
            if(diff < 0) return "";
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days < 7) return "(" + days + (days == 1 ? " day ago)" : " days ago)");
            long weeks = days / 7;
            if (weeks < 5) return "(" + weeks + (weeks == 1 ? " week ago)" : " weeks ago)");
            Calendar start = Calendar.getInstance();
            start.setTime(date);
            Calendar end = Calendar.getInstance();
            int monthDiff = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 + (end.get(Calendar.MONTH) - start.get(Calendar.MONTH));
            if (monthDiff < 12) return "(" + monthDiff + (monthDiff == 1 ? " month ago)" : " months ago)");
            int yearDiff = monthDiff / 12;
            return "(" + yearDiff + (yearDiff == 1 ? " year ago)" : " years ago)");
        } catch (ParseException e) {
            Log.e("Adapter", "Date parsing error", e);
            return "";
        }

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvNote, tvDate, tvRelativeTime, tvAmount;

        ViewHolder(View v) {
            super(v);
            tvType = v.findViewById(R.id.tv_transaction_type);
            tvNote = v.findViewById(R.id.tv_note);
            tvDate = v.findViewById(R.id.tv_date);
            tvRelativeTime = v.findViewById(R.id.tv_relative_time);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }
}