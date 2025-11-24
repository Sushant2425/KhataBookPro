//package com.sandhyasofttechh.mykhatapro.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.model.Transaction;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
//public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
//
//    private final List<Transaction> transactions;
//    private final OnItemClickListener listener;
//
//    private final SimpleDateFormat dfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//
//    public interface OnItemClickListener {
//        void onItemClick(Transaction transaction);
//    }
//
//    public TransactionAdapter(List<Transaction> transactions, OnItemClickListener listener) {
//        this.transactions = transactions;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_transaction, parent, false);
//        return new ViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
//
//        Transaction t = transactions.get(position);
//        Context ctx = h.itemView.getContext();
//
//        // -------------------------
//        // USE FIREBASE DATE (IMPORTANT FIX)
//        // -------------------------
//        String thisDate = t.getDate();   // <-- NOW USING THIS
//        String thisTime = dfTime.format(new Date(t.getTimestamp()));
//
//        h.tvLeftDate.setText(thisDate);
//        h.tvLeftTime.setText(thisTime);
//
//        // -------------------------
//        // DATE HEADER GROUPING using FIREBASE DATE
//        // -------------------------
//
//        if (position == 0) {
//            h.tvTopHeader.setVisibility(View.VISIBLE);
//            h.tvTopHeader.setText(thisDate + " · " + getDaysAgo(t.getTimestamp()));
//        } else {
//            String prevDate = transactions.get(position - 1).getDate();
//
//            if (prevDate.equals(thisDate)) {
//                h.tvTopHeader.setVisibility(View.GONE);
//            } else {
//                h.tvTopHeader.setVisibility(View.VISIBLE);
//                h.tvTopHeader.setText(thisDate + " · " + getDaysAgo(t.getTimestamp()));
//            }
//        }
//
//        // -------------------------
//        // AMOUNT POSITION
//        // -------------------------
//        String amt = "₹ " + t.getAmount();
//        String type = t.getType().trim().toLowerCase();
//
//        if (type.equals("gave")) {
//            h.tvMiddleAmount.setVisibility(View.VISIBLE);
//            h.tvMiddleAmount.setTextColor(ctx.getColor(R.color.error));
//            h.tvMiddleAmount.setText(amt);
//
//            h.tvEndAmount.setVisibility(View.GONE);
//
//        } else if (type.equals("got")) {
//            h.tvMiddleAmount.setVisibility(View.GONE);
//
//            h.tvEndAmount.setVisibility(View.VISIBLE);
//            h.tvEndAmount.setTextColor(ctx.getColor(R.color.green));
//            h.tvEndAmount.setText(amt);
//
//        } else {
//            h.tvMiddleAmount.setVisibility(View.GONE);
//            h.tvEndAmount.setVisibility(View.GONE);
//        }
//
//        h.itemView.setOnClickListener(v -> listener.onItemClick(t));
//    }
//
//    private String getDaysAgo(long ts) {
//        long diff = System.currentTimeMillis() - ts;
//        long days = TimeUnit.MILLISECONDS.toDays(diff);
//
//        if (days == 0) return "Today";
//        if (days == 1) return "1 day ago";
//        return days + " days ago";
//    }
//
//    @Override
//    public int getItemCount() {
//        return transactions.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//
//        TextView tvTopHeader, tvLeftDate, tvLeftTime, tvMiddleAmount, tvEndAmount;
//
//        public ViewHolder(@NonNull View v) {
//            super(v);
//
//            tvTopHeader = v.findViewById(R.id.tv_top_header);
//            tvLeftDate = v.findViewById(R.id.tv_left_date);
//            tvLeftTime = v.findViewById(R.id.tv_left_time);
//            tvMiddleAmount = v.findViewById(R.id.tv_middle_amount);
//            tvEndAmount = v.findViewById(R.id.tv_end_amount);
//        }
//    }
//}




package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.text.format.DateUtils;
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
    private final OnItemClickListener listener;

    private final SimpleDateFormat dfDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());

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
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Transaction t = transactions.get(position);
        Context ctx = h.itemView.getContext();

        // -------------------------
        // DATE + TIME
        // -------------------------
        String thisDate = t.getDate();   // Firebase date string
        String thisTime = dfTime.format(new Date(t.getTimestamp()));

        h.tvLeftDate.setText(thisDate);
        h.tvLeftTime.setText(thisTime);

        // -------------------------
        // DATE HEADER USING FIREBASE DATE
        // -------------------------
        if (position == 0) {
            h.tvTopHeader.setVisibility(View.VISIBLE);
            h.tvTopHeader.setText(thisDate + " " + getRelativeLabel(thisDate));
        } else {
            String prevDate = transactions.get(position - 1).getDate();

            if (prevDate.equals(thisDate)) {
                h.tvTopHeader.setVisibility(View.GONE);
            } else {
                h.tvTopHeader.setVisibility(View.VISIBLE);
                h.tvTopHeader.setText(thisDate + " " + getRelativeLabel(thisDate));
            }
        }

        // -------------------------
        // AMOUNT POSITION
        // -------------------------
        String amt = "₹ " + t.getAmount();
        String type = t.getType().trim().toLowerCase();

        if (type.equals("gave")) {

            h.tvMiddleAmount.setVisibility(View.VISIBLE);
            h.tvMiddleAmount.setTextColor(ctx.getColor(R.color.error));
            h.tvMiddleAmount.setText(amt);

            h.tvEndAmount.setVisibility(View.GONE);

        } else if (type.equals("got")) {

            h.tvMiddleAmount.setVisibility(View.GONE);

            h.tvEndAmount.setVisibility(View.VISIBLE);
            h.tvEndAmount.setTextColor(ctx.getColor(R.color.green));
            h.tvEndAmount.setText(amt);

        } else {
            h.tvMiddleAmount.setVisibility(View.GONE);
            h.tvEndAmount.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }

    // -------------------------
    // SAME LOGIC FROM CustomerSummaryAdapter
    // -------------------------
    private String getRelativeLabel(String dateString) {
        try {
            Date date = dfDate.parse(dateString);
            if (date == null) return "";

            if (DateUtils.isToday(date.getTime())) return "(Today)";

            long now = System.currentTimeMillis();
            long diff = now - date.getTime();
            if (diff < 0) return "";

            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (days < 7)
                return "(" + days + (days == 1 ? " day ago)" : " days ago)");

            long weeks = days / 7;
            if (weeks < 5)
                return "(" + weeks + (weeks == 1 ? " week ago)" : " weeks ago)");

            Calendar start = Calendar.getInstance();
            start.setTime(date);
            Calendar end = Calendar.getInstance();

            int monthDiff = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
                    (end.get(Calendar.MONTH) - start.get(Calendar.MONTH));

            if (monthDiff < 12)
                return "(" + monthDiff + (monthDiff == 1 ? " month ago)" : " months ago)");

            int yearDiff = monthDiff / 12;

            return "(" + yearDiff + (yearDiff == 1 ? " year ago)" : " years ago)");

        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTopHeader, tvLeftDate, tvLeftTime, tvMiddleAmount, tvEndAmount;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvTopHeader = v.findViewById(R.id.tv_top_header);
            tvLeftDate = v.findViewById(R.id.tv_left_date);
            tvLeftTime = v.findViewById(R.id.tv_left_time);
            tvMiddleAmount = v.findViewById(R.id.tv_middle_amount);
            tvEndAmount = v.findViewById(R.id.tv_end_amount);
        }
    }
}
