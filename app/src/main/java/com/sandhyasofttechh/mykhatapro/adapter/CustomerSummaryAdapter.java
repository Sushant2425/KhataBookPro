////package com.sandhyasofttechh.mykhatapro.adapter;
////
////import android.content.Context;
////import android.content.Intent;
////import android.text.format.DateUtils;
////import android.util.Log;
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.LinearLayout;
////import android.widget.TextView;
////
////import androidx.annotation.NonNull;
////import androidx.core.content.ContextCompat;
////import androidx.recyclerview.widget.RecyclerView;
////
////import com.sandhyasofttechh.mykhatapro.R;
////import com.sandhyasofttechh.mykhatapro.activities.CustomerDetailsActivity;
////import com.sandhyasofttechh.mykhatapro.model.CustomerSummary;
////
////import java.text.ParseException;
////import java.text.SimpleDateFormat;
////import java.util.Calendar;
////import java.util.Date;
////import java.util.List;
////import java.util.Locale;
////import java.util.concurrent.TimeUnit;
////
////public class CustomerSummaryAdapter extends RecyclerView.Adapter<CustomerSummaryAdapter.ViewHolder> {
////
////    private final List<CustomerSummary> customerSummaries;
////    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
////
////    public CustomerSummaryAdapter(List<CustomerSummary> customerSummaries) {
////        this.customerSummaries = customerSummaries;
////    }
////
////    @NonNull
////    @Override
////    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////        View v = LayoutInflater.from(parent.getContext())
////                .inflate(R.layout.item_customer_summary, parent, false);
////        return new ViewHolder(v);
////    }
////
////    @Override
////    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
////        CustomerSummary summary = customerSummaries.get(position);
////        Context context = holder.itemView.getContext();
////
////        holder.itemView.setOnClickListener(v -> {
////            Intent intent = new Intent(context, CustomerDetailsActivity.class);
////            intent.putExtra("CUSTOMER_PHONE", summary.getCustomerPhone());
////            intent.putExtra("CUSTOMER_NAME", summary.getCustomerName());
////            context.startActivity(intent);
////        });
////
////        holder.tvCustomerName.setText(summary.getCustomerName());
////        holder.tvLastDate.setText(summary.getLastTransactionDate());
////        holder.tvRelativeTime.setText(getCustomRelativeTime(summary.getLastTransactionDate()));
////
////        double balance = summary.getNetBalance();
////        String balanceText = String.format(Locale.getDefault(), "₹ %,.0f", Math.abs(balance));
////        holder.tvNetBalance.setText(balanceText);
////
////        if (balance > 0) {
////            holder.tvStatusLabel.setText(String.format("You will get: ₹%,.2f", balance));
////            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
////            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_give);
////        } else if (balance < 0) {
////            holder.tvStatusLabel.setText(String.format("You will give: ₹%,.2f", Math.abs(balance)));
////            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
////            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_get);
////        } else {
////            holder.tvStatusLabel.setText("Settled Up");
////            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.black));
////            holder.balanceContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
////        }
////    }
////
////
////    private String getCustomRelativeTime(String dateString) {
////        if (dateString == null || dateString.isEmpty()) return "";
////        try {
////            Date date = sdf.parse(dateString);
////            if (date == null) return "";
////            if (DateUtils.isToday(date.getTime())) return "(Today)";
////            long now = System.currentTimeMillis();
////            long diff = now - date.getTime();
////            if (diff < 0) return "";
////            long days = TimeUnit.MILLISECONDS.toDays(diff);
////            if (days < 7) return "(" + days + (days == 1 ? " day ago)" : " days ago)");
////            long weeks = days / 7;
////            if (weeks < 5) return "(" + weeks + (weeks == 1 ? " week ago)" : " weeks ago)");
////            Calendar start = Calendar.getInstance();
////            start.setTime(date);
////            Calendar end = Calendar.getInstance();
////            int monthDiff = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
////                    (end.get(Calendar.MONTH) - start.get(Calendar.MONTH));
////            if (monthDiff < 12) return "(" + monthDiff + (monthDiff == 1 ? " month ago)" : " months ago)");
////            int yearDiff = monthDiff / 12;
////            return "(" + yearDiff + (yearDiff == 1 ? " year ago)" : " years ago)");
////        } catch (ParseException e) {
////            Log.e("Adapter", "Date parsing error", e);
////            return "";
////        }
////    }
////
////    @Override
////    public int getItemCount() {
////        return customerSummaries.size();
////    }
////
////    static class ViewHolder extends RecyclerView.ViewHolder {
////
////        TextView tvCustomerName, tvNetBalance, tvStatusLabel, tvLastDate, tvRelativeTime;
////        LinearLayout balanceContainer;  // <<<<<< IMPORTANT change
////
////        ViewHolder(View v) {
////            super(v);
////            tvCustomerName = v.findViewById(R.id.summary_customer_name);
////            tvNetBalance = v.findViewById(R.id.summary_net_balance);
////            tvStatusLabel = v.findViewById(R.id.summary_status_label);
////            tvLastDate = v.findViewById(R.id.summary_last_date);
////            tvRelativeTime = v.findViewById(R.id.summary_relative_time);
////            balanceContainer = v.findViewById(R.id.balance_container); // LinearLayout now
////        }
////    }
////}
//
//
//
//
//
//package com.sandhyasofttechh.mykhatapro.adapter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.text.format.DateUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.bottomsheet.BottomSheetDialog;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.activities.CustomerDetailsActivity;
//import com.sandhyasofttechh.mykhatapro.model.CustomerSummary;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
//public class CustomerSummaryAdapter extends RecyclerView.Adapter<CustomerSummaryAdapter.ViewHolder> {
//
//    private final List<CustomerSummary> customerSummaries;
//    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//
//    public CustomerSummaryAdapter(List<CustomerSummary> customerSummaries) {
//        this.customerSummaries = customerSummaries;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_customer_summary, parent, false);
//        return new ViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        CustomerSummary summary = customerSummaries.get(position);
//        Context context = holder.itemView.getContext();
//
//        // Open customer details
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, CustomerDetailsActivity.class);
//            intent.putExtra("CUSTOMER_PHONE", summary.getCustomerPhone());
//            intent.putExtra("CUSTOMER_NAME", summary.getCustomerName());
//            context.startActivity(intent);
//        });
//
//        // Set basic data
//        holder.tvCustomerName.setText(summary.getCustomerName());
//        holder.tvLastDate.setText(summary.getLastTransactionDate());
//        holder.tvRelativeTime.setText(getRelative(summary.getLastTransactionDate()));
//
//        double balance = summary.getNetBalance();
//        holder.tvNetBalance.setText(String.format(Locale.getDefault(), "₹ %,.0f", Math.abs(balance)));
//
//        // Background color logic
//        if (balance > 0) {
//            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
//            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_give);
//        } else if (balance < 0) {
//            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
//            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_get);
//        } else {
//            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.black));
//            holder.balanceContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
//        }
//
//        // -----------------------------------------------------
//        // SHOW REMINDER BELOW AMOUNT ONLY IF DUE (balance > 0)
//        // -----------------------------------------------------
//        if (balance > 0) {
//            holder.tvReminderText.setVisibility(View.VISIBLE);
//            holder.tvReminderText.setText("Reminder");
//            holder.tvReminderText.setOnClickListener(v -> showReminderOptions(context, summary));
//        } else {
//            holder.tvReminderText.setVisibility(View.GONE);
//        }
//    }
//
//    // Bottom sheet options
//    private void showReminderOptions(Context context, CustomerSummary summary) {
//        BottomSheetDialog dialog = new BottomSheetDialog(context);
//        View v = LayoutInflater.from(context).inflate(R.layout.bottomsheet_reminder_options, null);
//        dialog.setContentView(v);
//
//        LinearLayout whatsapp = v.findViewById(R.id.btnWhatsAppReminder);
//        LinearLayout sms = v.findViewById(R.id.btnSmsReminder);
//        TextView tvCustomerName = v.findViewById(R.id.tvCustomerName);
//        TextView tvDueAmount = v.findViewById(R.id.tvDueAmount);
//        TextView tvReminderMessage = v.findViewById(R.id.tvReminderMessage);
//
//        whatsapp.setOnClickListener(view -> {
//            sendWhatsApp(context, summary);
//            dialog.dismiss();
//        });
//        tvCustomerName.setText("Customer: " + summary.getCustomerName());
//        tvDueAmount.setText("Due Amount: ₹" + String.format("%.0f", summary.getNetBalance()));
//        tvReminderMessage.setText(
//                "This customer has a pending payment. You can remind them through WhatsApp or SMS."
//        );
//
//        sms.setOnClickListener(view -> {
//            sendSms(context, summary);
//            dialog.dismiss();
//        });
//
//        dialog.show();
//    }
//
//    // WhatsApp message sender
//    private void sendWhatsApp(Context context, CustomerSummary s) {
//        String phone = s.getCustomerPhone().replaceAll("[^\\d]", "");
//        if (phone.length() == 10) phone = "91" + phone;
//
//        String msg = "Hello " + s.getCustomerName() + ",\n"
//                + "You have a pending payment of ₹" + String.format("%.0f", s.getNetBalance()) + ".\n"
//                + "Please clear it as soon as possible.\n"
//                + "- MyKhata Pro";
//
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(msg)));
//            context.startActivity(intent);
//        } catch (Exception e) {
//            Log.e("Reminder", "WhatsApp error", e);
//        }
//    }
//
//    // SMS sender
//    private void sendSms(Context context, CustomerSummary s) {
//        String msg = "Hello " + s.getCustomerName() +
//                ", you have a pending payment of ₹" +
//                String.format("%.0f", s.getNetBalance()) +
//                ". Please clear it soon. - MyKhata Pro";
//
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("smsto:" + s.getCustomerPhone()));
//        intent.putExtra("sms_body", msg);
//
//        context.startActivity(intent);
//    }
//
//    // Relative time text
//    private String getRelative(String date) {
//        if (date == null || date.isEmpty()) return "";
//        try {
//            Date d = sdf.parse(date);
//            if (d == null) return "";
//            if (DateUtils.isToday(d.getTime())) return "(Today)";
//            long now = System.currentTimeMillis();
//            long diff = now - d.getTime();
//            long days = TimeUnit.MILLISECONDS.toDays(diff);
//            if (days < 7) return "(" + days + " days ago)";
//            long weeks = days / 7;
//            return "(" + weeks + " weeks ago)";
//        } catch (ParseException e) {
//            return "";
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return customerSummaries.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//
//        TextView tvCustomerName, tvNetBalance, tvLastDate, tvRelativeTime;
//        TextView tvReminderText;
//        LinearLayout balanceContainer;
//
//        ViewHolder(View v) {
//            super(v);
//            tvCustomerName = v.findViewById(R.id.summary_customer_name);
//            tvNetBalance = v.findViewById(R.id.summary_net_balance);
//            tvLastDate = v.findViewById(R.id.summary_last_date);
//            tvRelativeTime = v.findViewById(R.id.summary_relative_time);
//            balanceContainer = v.findViewById(R.id.balance_container);
//
//            // NEW reminder text below amount
//            tvReminderText = v.findViewById(R.id.summary_reminder_text);
//        }
//    }
//}





package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.CustomerDetailsActivity;
import com.sandhyasofttechh.mykhatapro.model.CustomerSummary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        // Open customer details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerDetailsActivity.class);
            intent.putExtra("CUSTOMER_PHONE", summary.getCustomerPhone());
            intent.putExtra("CUSTOMER_NAME", summary.getCustomerName());
            context.startActivity(intent);
        });

        // Set basic data
        holder.tvCustomerName.setText(summary.getCustomerName());
        holder.tvLastDate.setText(summary.getLastTransactionDate());
        holder.tvRelativeTime.setText(getRelative(summary.getLastTransactionDate()));

        double balance = summary.getNetBalance();
        holder.tvNetBalance.setText(String.format(Locale.getDefault(), "₹ %,.0f", Math.abs(balance)));

        // Background color logic
        if (balance > 0) {
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_give);
        } else if (balance < 0) {
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_get);
        } else {
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.balanceContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
        }

        // Show reminder below amount only if due (balance > 0)
        if (balance > 0) {
            holder.tvReminderText.setVisibility(View.VISIBLE);
            holder.tvReminderText.setText("Reminder");
            holder.tvReminderText.setOnClickListener(v -> showReminderBottomSheet(context, summary));
        } else {
            holder.tvReminderText.setVisibility(View.GONE);
        }
    }

    /**
     * Show professional payment reminder bottom sheet
     */
    private void showReminderBottomSheet(Context context, CustomerSummary summary) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_reminder_options, null);
        dialog.setContentView(view);

        // Find views
        TextView tvCustomerName = view.findViewById(R.id.tvCustomerName);
        TextView tvDueAmount = view.findViewById(R.id.tvDueAmount);
        CardView btnWhatsApp = view.findViewById(R.id.btnWhatsAppReminder);
        CardView btnSms = view.findViewById(R.id.btnSmsReminder);

        // Set customer data
        tvCustomerName.setText(summary.getCustomerName());
        tvDueAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", summary.getNetBalance()));

        // WhatsApp button click
        btnWhatsApp.setOnClickListener(v -> {
            sendWhatsAppReminder(context, summary);
            dialog.dismiss();
        });

        // SMS button click
        btnSms.setOnClickListener(v -> {
            sendSmsReminder(context, summary);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Send payment reminder via WhatsApp
     */
    private void sendWhatsAppReminder(Context context, CustomerSummary summary) {
        String phone = summary.getCustomerPhone().replaceAll("[^\\d]", "");

        // Add country code if needed
        if (phone.length() == 10) {
            phone = "91" + phone;
        }

        // Prepare message
        String message = String.format(
                "Hello %s,\n\n" +
                        "This is a friendly reminder about your pending payment.\n\n" +
                        "Outstanding Amount: ₹%,.0f\n\n" +
                        "Please make the payment at your earliest convenience.\n\n" +
                        "Thank you!\n" +
                        "- MyKhata Pro",
                summary.getCustomerName(),
                summary.getNetBalance()
        );

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);
        } catch (Exception e) {
            // If WhatsApp is not installed, open in browser
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e("CustomerAdapter", "Error opening WhatsApp", ex);
            }
        }
    }

    /**
     * Send payment reminder via SMS
     */
    private void sendSmsReminder(Context context, CustomerSummary summary) {
        String message = String.format(
                "Hello %s, this is a reminder about your pending payment of ₹%,.0f. " +
                        "Please make the payment at your earliest convenience. Thank you! - MyKhata Pro",
                summary.getCustomerName(),
                summary.getNetBalance()
        );

        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + summary.getCustomerPhone()));
            intent.putExtra("sms_body", message);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("CustomerAdapter", "Error opening SMS", e);
        }
    }

    /**
     * Get relative time string
     */
    private String getRelative(String date) {
        if (date == null || date.isEmpty()) return "";
        try {
            Date d = sdf.parse(date);
            if (d == null) return "";

            if (DateUtils.isToday(d.getTime())) {
                return "(Today)";
            }

            long now = System.currentTimeMillis();
            long diff = now - d.getTime();

            if (diff < 0) return "";

            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (days == 0) {
                return "(Today)";
            } else if (days == 1) {
                return "(Yesterday)";
            } else if (days < 7) {
                return "(" + days + " days ago)";
            } else if (days < 30) {
                long weeks = days / 7;
                return "(" + weeks + (weeks == 1 ? " week ago)" : " weeks ago)");
            } else if (days < 365) {
                long months = days / 30;
                return "(" + months + (months == 1 ? " month ago)" : " months ago)");
            } else {
                long years = days / 365;
                return "(" + years + (years == 1 ? " year ago)" : " years ago)");
            }
        } catch (ParseException e) {
            Log.e("CustomerAdapter", "Date parsing error", e);
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return customerSummaries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCustomerName, tvNetBalance, tvLastDate, tvRelativeTime;
        TextView tvReminderText;
        LinearLayout balanceContainer;

        ViewHolder(View v) {
            super(v);
            tvCustomerName = v.findViewById(R.id.summary_customer_name);
            tvNetBalance = v.findViewById(R.id.summary_net_balance);
            tvLastDate = v.findViewById(R.id.summary_last_date);
            tvRelativeTime = v.findViewById(R.id.summary_relative_time);
            balanceContainer = v.findViewById(R.id.balance_container);
            tvReminderText = v.findViewById(R.id.summary_reminder_text);
        }
    }
}