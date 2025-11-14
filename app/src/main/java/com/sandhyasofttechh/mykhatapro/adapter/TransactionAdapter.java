package com.sandhyasofttechh.mykhatapro.adapter;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        holder.tvCustomerName.setText(t.getCustomerName());
        holder.tvAmount.setText(String.format("₹%.2f", t.getAmount()));
        holder.tvNote.setText(t.getNote());
        
        holder.tvDate.setText(t.getDate());
        holder.tvRelativeTime.setText(getCustomRelativeTime(t.getDate()));

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

    private String getCustomRelativeTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        long timestamp;
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                timestamp = date.getTime();
            } else {
                return "";
            }
        } catch (ParseException e) {
            Log.e("TransactionAdapter", "Failed to parse date: " + dateString, e);
            return "";
        }
        
        if (DateUtils.isToday(timestamp)) {
            return "(Today)";
        }

        long now = System.currentTimeMillis();
        long diffInMillis = now - timestamp;

        if (diffInMillis < 0) {
            return ""; // Don't show for future dates
        }

        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        if (days < 7) {
            return "(" + days + (days == 1 ? " day ago)" : " days ago)");
        }

        long weeks = days / 7;
        if (weeks < 5) {
            return "(" + weeks + (weeks == 1 ? " week ago)" : " weeks ago)");
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(timestamp);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(now);

        int yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int monthDiff = yearDiff * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

        if (monthDiff < 12) {
            return "(" + monthDiff + (monthDiff == 1 ? " month ago)" : " months ago)");
        }

        int years = monthDiff / 12;
        return "(" + years + (years == 1 ? " year ago)" : " years ago)");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvAmount, tvType, tvNote, tvDate, tvRelativeTime;

        ViewHolder(View v) {
            super(v);
            tvCustomerName = v.findViewById(R.id.tv_customer_name);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvType = v.findViewById(R.id.tv_type);
            tvNote = v.findViewById(R.id.tv_note);
            tvDate = v.findViewById(R.id.tv_date);
            tvRelativeTime = v.findViewById(R.id.tv_relative_time);
        }
    }
}