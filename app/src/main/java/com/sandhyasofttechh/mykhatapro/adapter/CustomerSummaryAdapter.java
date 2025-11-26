package com.sandhyasofttechh.mykhatapro.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.CustomerDetailsActivity;
import com.sandhyasofttechh.mykhatapro.model.CustomerSummary;
import com.sandhyasofttechh.mykhatapro.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        CustomerSummary summary = customerSummaries.get(position);
//        Context context = holder.itemView.getContext();
//
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, CustomerDetailsActivity.class);
//            intent.putExtra("CUSTOMER_PHONE", summary.getCustomerPhone());
//            intent.putExtra("CUSTOMER_NAME", summary.getCustomerName());
//            context.startActivity(intent);
//        });
//
//        holder.tvCustomerName.setText(summary.getCustomerName());
//        holder.tvLastDate.setText(summary.getLastTransactionDate());
//        holder.tvRelativeTime.setText(getRelative(summary.getLastTransactionDate()));
//
//        double balance = summary.getNetBalance();
//        holder.tvNetBalance.setText(String.format(Locale.getDefault(), "₹ %,.0f", Math.abs(balance)));
//
//        if (balance > 0) {
//            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
//            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_get);
//        } else if (balance < 0) {
//            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
//            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_give);
//        } else {
//            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.black));
//            holder.balanceContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
//        }
//
//        if (balance > 0) {
//            holder.tvReminderText.setVisibility(View.VISIBLE);
//            holder.tvReminderText.setText("Reminder");
//            holder.tvReminderText.setOnClickListener(v -> showReminderBottomSheet(context, summary));
//        } else {
//            holder.tvReminderText.setVisibility(View.GONE);
//        }
//    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CustomerSummary summary = customerSummaries.get(position);
        Context context = holder.itemView.getContext();

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerDetailsActivity.class);
            intent.putExtra("CUSTOMER_PHONE", summary.getCustomerPhone());
            intent.putExtra("CUSTOMER_NAME", summary.getCustomerName());
            context.startActivity(intent);
        });

        holder.tvCustomerName.setText(summary.getCustomerName());
        holder.tvLastDate.setText(summary.getLastTransactionDate());
        holder.tvRelativeTime.setText(getRelative(summary.getLastTransactionDate()));

        double balance = summary.getNetBalance();
        holder.tvNetBalance.setText(String.format(Locale.getDefault(), "₹ %,.0f", Math.abs(balance)));

        // -----------------------------------------------
        // CORRECT COLOR + BACKGROUND + REMINDER LOGIC
        // -----------------------------------------------

        // 🔴 Case 1 — Customer owes YOU (You Will Get) → balance < 0
        if (balance < 0) {
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_give);  // your red bg

            holder.tvReminderText.setVisibility(View.VISIBLE);
            holder.tvReminderText.setText("Reminder");
            holder.tvReminderText.setOnClickListener(v -> showReminderBottomSheet(context, summary));
        }

        // 🟢 Case 2 — YOU owe customer (You Will Give) → balance > 0
        else if (balance > 0) {
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.balanceContainer.setBackgroundResource(R.drawable.background_balance_get); // your green bg

            holder.tvReminderText.setVisibility(View.GONE);
        }

        // ⚪ Case 3 — ZERO balance
        else {
            holder.tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.balanceContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
            holder.tvReminderText.setVisibility(View.GONE);
        }
    }





    private void showReminderBottomSheet(Context context, CustomerSummary summary) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_reminder_options, null);
        dialog.setContentView(view);

        TextView tvCustomerName = view.findViewById(R.id.tvCustomerName);
        TextView tvDueAmount = view.findViewById(R.id.tvDueAmount);
        CardView btnWhatsApp = view.findViewById(R.id.btnWhatsAppReminder);
        CardView btnSms = view.findViewById(R.id.btnSmsReminder);

        tvCustomerName.setText(summary.getCustomerName());
        tvDueAmount.setText(String.format(Locale.getDefault(), "₹%,.0f", summary.getNetBalance()));

        btnWhatsApp.setOnClickListener(v -> {
            new GeneratePdfAndUploadTask(context, summary, (link) -> {
                if (link != null) {
                    sendWhatsAppReminder(context, summary, link);
                } else {
                    Toast.makeText(context, "Failed to generate statement", Toast.LENGTH_SHORT).show();
                }
            }).execute();
            dialog.dismiss();
        });

        btnSms.setOnClickListener(v -> {
            new GeneratePdfAndUploadTask(context, summary, (link) -> {
                if (link != null) {
                    sendSmsReminder(context, summary, link);
                } else {
                    Toast.makeText(context, "Failed to generate statement", Toast.LENGTH_SHORT).show();
                }
            }).execute();
            dialog.dismiss();
        });

        dialog.show();
    }

    private static class GeneratePdfAndUploadTask extends AsyncTask<Void, Void, String> {

        interface Callback {
            void onComplete(String downloadLink);
        }

        private final Context context;
        private final CustomerSummary summary;
        private final Callback callback;
        private ProgressDialog progress;

        GeneratePdfAndUploadTask(Context context, CustomerSummary summary, Callback callback) {
            this.context = context;
            this.summary = summary;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            progress.setMessage("Generating professional statement...");
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                List<Transaction> txList = summary.getTransactions();
                if (txList == null) txList = new ArrayList<>();

                File pdfFile = createBankStylePdf(context, summary, txList);
                if (pdfFile == null) return null;

                final String[] downloadUrlHolder = new String[1];
                final boolean[] finished = {false};

                String timestamp = String.valueOf(System.currentTimeMillis());
                String fileName = "stmt_" + summary.getCustomerPhone().replaceAll("[^\\d]", "") + "_" + timestamp + ".pdf";

                StorageReference ref = FirebaseStorage.getInstance()
                        .getReference()
                        .child("Statements/" + fileName);

                Uri fileUri = Uri.fromFile(pdfFile);
                ref.putFile(fileUri)
                        .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    downloadUrlHolder[0] = uri.toString();
                                    finished[0] = true;
                                })
                                .addOnFailureListener(e -> finished[0] = true))
                        .addOnFailureListener(e -> finished[0] = true);

                int waited = 0;
                while (!finished[0] && waited < 30000) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {
                    }
                    waited += 300;
                }

                return downloadUrlHolder[0];
            } catch (Exception e) {
                Log.e("GenPdfTask", "Error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String link) {
            if (progress != null && progress.isShowing()) progress.dismiss();
            callback.onComplete(link);
        }

        /**
         * Creates a professional bank-style PDF with complete grid lines
         */
        private static File createBankStylePdf(Context context, CustomerSummary summary, List<Transaction> transactions) {
            try {
                File folder = new File(context.getExternalFilesDir(null), "MyKhataPro/Statements");
                if (!folder.exists()) folder.mkdirs();

                String filename = "statement_" + System.currentTimeMillis() + ".pdf";
                File pdfFile = new File(folder, filename);

                PdfDocument document = new PdfDocument();
                int pageWidth = 595;
                int pageHeight = 842;
                int pageNumber = 1;

                // Table dimensions
                int marginLeft = 30;
                int marginRight = 30;
                int tableWidth = pageWidth - marginLeft - marginRight;

                // Column widths
                int col1X = marginLeft;                    // Date start
                int col2X = marginLeft + 110;              // Debit start
                int col3X = marginLeft + 240;              // Credit start
                int col4X = marginLeft + 370;              // Balance start
                int tableEndX = pageWidth - marginRight;   // Table end

                int rowHeight = 28;

                // Paint setup
                Paint borderPaint = new Paint();
                borderPaint.setColor(Color.BLACK);
                borderPaint.setStrokeWidth(1.5f);
                borderPaint.setStyle(Paint.Style.STROKE);

                Paint headerBgPaint = new Paint();
                headerBgPaint.setColor(Color.BLACK);
                headerBgPaint.setStyle(Paint.Style.FILL);

                Paint headerTextPaint = new Paint();
                headerTextPaint.setColor(Color.WHITE);
                headerTextPaint.setTextSize(11);
                headerTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                headerTextPaint.setAntiAlias(true);

                Paint titlePaint = new Paint();
                titlePaint.setColor(Color.BLACK);
                titlePaint.setTextSize(20);
                titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                titlePaint.setAntiAlias(true);

                Paint labelPaint = new Paint();
                labelPaint.setColor(Color.BLACK);
                labelPaint.setTextSize(11);
                labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                labelPaint.setAntiAlias(true);

                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(10);
                textPaint.setAntiAlias(true);

                Paint debitPaint = new Paint();
                debitPaint.setColor(Color.parseColor("#C62828"));
                debitPaint.setTextSize(10);
                debitPaint.setAntiAlias(true);

                Paint creditPaint = new Paint();
                creditPaint.setColor(Color.parseColor("#2E7D32"));
                creditPaint.setTextSize(10);
                creditPaint.setAntiAlias(true);

                Paint redBgPaint = new Paint();
                redBgPaint.setColor(Color.parseColor("#FFEBEE"));
                redBgPaint.setStyle(Paint.Style.FILL);

                Paint greenBgPaint = new Paint();
                greenBgPaint.setColor(Color.parseColor("#E8F5E9"));
                greenBgPaint.setStyle(Paint.Style.FILL);

                // Start first page
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                int y = 40;

                // Title
                canvas.drawText("ACCOUNT STATEMENT", marginLeft, y, titlePaint);
                y += 30;

                // Date
                textPaint.setTextSize(9);
                canvas.drawText("Generated: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()),
                        marginLeft, y, textPaint);
                y += 25;

                // Customer box
                canvas.drawRect(marginLeft, y, tableEndX, y + 70, borderPaint);

                // Horizontal line in customer box
                canvas.drawLine(marginLeft, y + 35, tableEndX, y + 35, borderPaint);

                // Vertical line in customer box
                canvas.drawLine(marginLeft + (tableWidth / 2), y, marginLeft + (tableWidth / 2), y + 70, borderPaint);

                textPaint.setTextSize(10);
                canvas.drawText("Customer Name:", marginLeft + 5, y + 20, labelPaint);
                canvas.drawText(summary.getCustomerName(), marginLeft + 5, y + 32, textPaint);

                canvas.drawText("Phone Number:", marginLeft + (tableWidth / 2) + 5, y + 20, labelPaint);
                canvas.drawText(summary.getCustomerPhone(), marginLeft + (tableWidth / 2) + 5, y + 32, textPaint);

                canvas.drawText("Statement Date:", marginLeft + 5, y + 50, labelPaint);
                canvas.drawText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()),
                        marginLeft + 5, y + 62, textPaint);

                canvas.drawText("Outstanding Balance:", marginLeft + (tableWidth / 2) + 5, y + 50, labelPaint);
                Paint balancePaint = new Paint();
                balancePaint.setColor(Color.parseColor("#D32F2F"));
                balancePaint.setTextSize(11);
                balancePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                balancePaint.setAntiAlias(true);
                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", summary.getNetBalance()),
                        marginLeft + (tableWidth / 2) + 5, y + 62, balancePaint);

                y += 85;

                // Transaction table title
                canvas.drawText("TRANSACTION DETAILS", marginLeft, y, labelPaint);
                y += 20;

                int tableStartY = y;

                // Draw table header with black background
                canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, headerBgPaint);

                // Header borders
                canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
                canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
                canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
                canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);

                // Header text
                canvas.drawText("Date", col1X + 5, y + 18, headerTextPaint);
                canvas.drawText("Debit (You Gave)", col2X + 5, y + 18, headerTextPaint);
                canvas.drawText("Credit (You Got)", col3X + 5, y + 18, headerTextPaint);
                canvas.drawText("Balance", col4X + 5, y + 18, headerTextPaint);

                y += rowHeight;

                // Calculate running balance
                double runningBalance = 0;
                List<TransactionRow> rows = new ArrayList<>();

                for (Transaction t : transactions) {
                    String date = t.getDate() != null ? t.getDate() : "-";
                    String type = t.getType() != null ? t.getType().toUpperCase() : "";
                    double amount = t.getAmount();

                    double debit = 0;
                    double credit = 0;

                    if (type.equals("GAVE") || type.equals("PAYMENT")) {
                        debit = amount;
                        runningBalance -= amount;
                    } else {
                        credit = amount;
                        runningBalance += amount;
                    }

                    rows.add(new TransactionRow(date, debit, credit, runningBalance));
                }

                // Draw transaction rows
                for (TransactionRow row : rows) {
                    // Check for new page
                    if (y > pageHeight - 100) {
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = 40;

                        // Redraw header on new page
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, headerBgPaint);
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
                        canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
                        canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
                        canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);

                        canvas.drawText("Date", col1X + 5, y + 18, headerTextPaint);
                        canvas.drawText("Debit (You Gave)", col2X + 5, y + 18, headerTextPaint);
                        canvas.drawText("Credit (You Got)", col3X + 5, y + 18, headerTextPaint);
                        canvas.drawText("Balance", col4X + 5, y + 18, headerTextPaint);

                        y += rowHeight;
                    }

                    // Draw row background
                    if (row.debit > 0) {
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, redBgPaint);
                    } else if (row.credit > 0) {
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, greenBgPaint);
                    }

                    // Draw all cell borders - COMPLETE GRID
                    // Outer rectangle
                    canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
                    // Vertical lines
                    canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
                    canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
                    canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);

                    // Draw cell content
                    canvas.drawText(row.date, col1X + 5, y + 18, textPaint);

                    if (row.debit > 0) {
                        canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", row.debit),
                                col2X + 5, y + 18, debitPaint);
                    } else {
                        canvas.drawText("-", col2X + 5, y + 18, textPaint);
                    }

                    if (row.credit > 0) {
                        canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", row.credit),
                                col3X + 5, y + 18, creditPaint);
                    } else {
                        canvas.drawText("-", col3X + 5, y + 18, textPaint);
                    }

                    Paint balanceAmtPaint = new Paint();
                    balanceAmtPaint.setTextSize(10);
                    balanceAmtPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    balanceAmtPaint.setColor(row.balance >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
                    balanceAmtPaint.setAntiAlias(true);
                    canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", Math.abs(row.balance)),
                            col4X + 5, y + 18, balanceAmtPaint);

                    y += rowHeight;
                }

                // Summary section
                y += 15;

                // Summary box with complete borders
                int summaryBoxHeight = 90;
                canvas.drawRect(marginLeft, y, tableEndX, y + summaryBoxHeight, borderPaint);

                // Internal horizontal lines
                canvas.drawLine(marginLeft, y + 30, tableEndX, y + 30, borderPaint);
                canvas.drawLine(marginLeft, y + 60, tableEndX, y + 60, borderPaint);

                // Vertical line
                canvas.drawLine(marginLeft + 300, y, marginLeft + 300, y + summaryBoxHeight, borderPaint);

                double totalDebit = 0;
                double totalCredit = 0;
                for (TransactionRow row : rows) {
                    totalDebit += row.debit;
                    totalCredit += row.credit;
                }

                canvas.drawText("Total Debit (You Gave):", marginLeft + 5, y + 20, labelPaint);
                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", totalDebit),
                        marginLeft + 305, y + 20, debitPaint);

                canvas.drawText("Total Credit (You Got):", marginLeft + 5, y + 50, labelPaint);
                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", totalCredit),
                        marginLeft + 305, y + 50, creditPaint);

                Paint finalBalancePaint = new Paint();
                finalBalancePaint.setTextSize(12);
                finalBalancePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                finalBalancePaint.setColor(Color.BLACK);
                finalBalancePaint.setAntiAlias(true);

                canvas.drawText("NET BALANCE:", marginLeft + 5, y + 80, finalBalancePaint);

                Paint netAmountPaint = new Paint();
                netAmountPaint.setTextSize(12);
                netAmountPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                netAmountPaint.setColor(summary.getNetBalance() >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
                netAmountPaint.setAntiAlias(true);
                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", Math.abs(summary.getNetBalance())),
                        marginLeft + 305, y + 80, netAmountPaint);

                // Footer
                y = pageHeight - 40;
                textPaint.setTextSize(8);
                textPaint.setColor(Color.GRAY);
                canvas.drawText("This is a computer-generated statement from MyKhata Pro | Page " + pageNumber,
                        marginLeft, y, textPaint);

                document.finishPage(page);

                // Write to file
                FileOutputStream fos = new FileOutputStream(pdfFile);
                document.writeTo(fos);
                document.close();
                fos.close();

                return pdfFile;
            } catch (Exception e) {
                Log.e("CreatePDF", "Error", e);
                return null;
            }
        }

        static class TransactionRow {
            String date;
            double debit;
            double credit;
            double balance;

            TransactionRow(String date, double debit, double credit, double balance) {
                this.date = date;
                this.debit = debit;
                this.credit = credit;
                this.balance = balance;
            }
        }
    }

    private void sendWhatsAppReminder(Context context, CustomerSummary summary, String link) {
        String phone = summary.getCustomerPhone().replaceAll("[^\\d]", "");
        if (phone.length() == 10) phone = "91" + phone;

        String message = String.format(
                "Dear %s,\n\n" +
                        "You have a pending payment of *₹%,.0f*\n\n" +
                        "📄 View your complete statement:\n%s\n\n" +
                        "Please settle the payment at your earliest convenience.\n\n" +
                        "Thank you,\n*MyKhata Pro*",
                summary.getCustomerName(),
                summary.getNetBalance(),
                link
        );

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e("WhatsApp", "Error", ex);
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSmsReminder(Context context, CustomerSummary summary, String link) {
        String phone = summary.getCustomerPhone();
        String message = String.format(
                "Dear %s, you have a pending payment of Rs.%,.0f. View statement: %s - MyKhata Pro",
                summary.getCustomerName(),
                summary.getNetBalance(),
                link
        );

        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phone));
            intent.putExtra("sms_body", message);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("SMS", "Error", e);
            Toast.makeText(context, "Cannot send SMS", Toast.LENGTH_SHORT).show();
        }
    }

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