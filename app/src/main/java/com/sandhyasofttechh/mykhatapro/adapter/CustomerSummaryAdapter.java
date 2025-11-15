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
import com.sandhyasofttechh.mykhatapro.model.CustomerSummary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CustomerSummaryAdapter extends RecyclerView.Adapter<CustomerSummaryAdapter.ViewHolder> {

    private final List<CustomerSummary> customerSummaries;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public CustomerSummaryAdapter(List<CustomerSummary> customerSummaries) {
        this.customerSummaries = customerSummaries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_summary, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerSummary summary = customerSummaries.get(position);
        Context context = holder.itemView.getContext();

        holder.tvCustomerName.setText(summary.getCustomerName());
        
        // Set last transaction date and relative time
        holder.tvLastDate.setText(summary.getLastTransactionDate());
        holder.tvRelativeTime.setText(getCustomRelativeTime(summary.getLastTransactionDate()));

        double balance = summary.getNetBalance();
        String balanceText = String.format(Locale.getDefault(), "₹%.2f", Math.abs(balance));
        holder.tvNetBalance.setText(balanceText);

        if (balance > 0) {
            // Net balance is positive -> You will GET money
            holder.tvBalanceLabel.setText(String.format("You will get: %s", balanceText));
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else if (balance < 0) {
            // Net balance is negative -> You will GIVE money
            holder.tvBalanceLabel.setText(String.format("You will give: %s", balanceText));
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else {
            // Settled up
            holder.tvBalanceLabel.setText("Settled up");
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
    }
    
    private String getCustomRelativeTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            Date date = sdf.parse(dateString);
            if (date == null) return "";
            
            if (DateUtils.isToday(date.getTime())) return "(Today)";
            
            long now = System.currentTimeMillis();
            long diff = now - date.getTime();
            if(diff < 0) return ""; // Future date

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
        return customerSummaries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvNetBalance, tvBalanceLabel, tvLastDate, tvRelativeTime;

        ViewHolder(View v) {
            super(v);
            tvCustomerName = v.findViewById(R.id.tv_customer_name_summary);
            tvNetBalance = v.findViewById(R.id.tv_net_balance);
            tvBalanceLabel = v.findViewById(R.id.tv_balance_label_summary);
            tvLastDate = v.findViewById(R.id.tv_last_transaction_date);
            tvRelativeTime = v.findViewById(R.id.tv_last_transaction_relative);
        }
    }
}