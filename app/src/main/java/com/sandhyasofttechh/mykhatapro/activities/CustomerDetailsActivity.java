package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.TransactionAdapter;
import com.sandhyasofttechh.mykhatapro.fragments.ReportOptionsBottomSheet;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.ImageGenerator;
import com.sandhyasofttechh.mykhatapro.utils.PdfGenerator;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerDetailsActivity extends AppCompatActivity
        implements ReportOptionsBottomSheet.ReportListener, TransactionAdapter.OnItemClickListener {

    private String customerPhone, customerName;
    private List<Transaction> transactionList = new ArrayList<>();
    private TransactionAdapter adapter;
    private TextView tvTotalGave, tvTotalGot;
    private double netBalance = 0;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        prefManager = new PrefManager(this);
        customerPhone = getIntent().getStringExtra("CUSTOMER_PHONE");
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");

        if (customerPhone == null || customerName == null) {
            Toast.makeText(this, "Error: Customer data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        tvTotalGave = findViewById(R.id.tv_total_gave);
        tvTotalGot = findViewById(R.id.tv_total_got);

        setupRecyclerView();
        setupClickListeners();
        loadTransactions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Back Arrow White
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

            // 3 Dots (overflow menu) White
            toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_white));

            getSupportActionBar().setTitle(customerName);
            getSupportActionBar().setSubtitle(formatPhoneNumber(customerPhone));
        }
    }

    private String formatPhoneNumber(String phone) {
        String clean = phone.replaceAll("[^\\d]", "");
        if (clean.startsWith("91") && clean.length() == 12) {
            clean = clean.substring(2);
        } else if (clean.startsWith("+91")) {
            clean = clean.substring(3);
        }
        if (clean.length() == 10) {
            return clean.substring(0, 5) + " " + clean.substring(5);
        }
        return phone;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_customer_details, menu);
        return true;
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            try {
                Method method = menu.getClass().getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception ignored) {}
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_edit_customer) {
            editCustomer();
            return true;
        }

        if (id == R.id.action_share_pdf) {
            shareFullPdfDirect();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editCustomer() {
        Intent intent = new Intent(this, AddCustomerActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("CUSTOMER_PHONE", customerPhone);
        intent.putExtra("CUSTOMER_NAME", customerName);
        startActivity(intent);
    }



    private void shareFullPdfDirect() {
        if (transactionList.isEmpty()) {
            Toast.makeText(this, "No transactions found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Full report range label
        String dateRange = "Full Report";

        // Opening balance (0 by default)
        double openingBalance = 0;

        File pdfFile = PdfGenerator.generatePdf(
                this,
                customerName,
                customerPhone,
                transactionList,
                dateRange,
                openingBalance
        );

        if (pdfFile != null) {
            sharePdfIntent(pdfFile);
        } else {
            Toast.makeText(this, "Failed to generate PDF.", Toast.LENGTH_SHORT).show();
        }
    }


    // Refresh name & phone when user returns after editing
    @Override
    protected void onResume() {
        super.onResume();

        // Get updated values if passed back (optional: you can also re-fetch from DB)
        String updatedName = getIntent().getStringExtra("UPDATED_CUSTOMER_NAME");
        String updatedPhone = getIntent().getStringExtra("UPDATED_CUSTOMER_PHONE");

        if (updatedName != null && updatedPhone != null) {
            customerName = updatedName;
            customerPhone = updatedPhone;

            // Update toolbar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(customerName);
                getSupportActionBar().setSubtitle(formatPhoneNumber(customerPhone));
            }
        }
    }

    private void sendWhatsAppMessage() {
        String appName = getString(R.string.app_name);
        File imageFile = ImageGenerator.generateShareableImage(this, customerName, netBalance, appName);
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "Failed to create image.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                imageFile
        );

        String phone = customerPhone.replaceAll("[^\\d+]", "");
        if (phone.startsWith("+")) phone = phone.substring(1);
        if (!phone.startsWith("91") && phone.length() == 10) {
            phone = "91" + phone;
        }
        if (phone.length() < 10) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        String message;
        if (netBalance > 0) {
            message = "Hello " + customerName + ",\nThis is a friendly reminder for your pending payment of ₹" +
                    String.format(Locale.getDefault(), "%.2f", netBalance) + ".\nThank you!";
        } else if (netBalance < 0) {
            message = "Hello " + customerName + ",\nThis is a confirmation that I have a pending payment to you of ₹" +
                    String.format(Locale.getDefault(), "%.2f", Math.abs(netBalance)) + ".\nThank you!";
        } else {
            message = "Hello " + customerName + ",\nJust to confirm, our account is currently settled. Thank you!";
        }

        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.setPackage("com.whatsapp");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.putExtra("jid", phone + "@s.whatsapp.net");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            try {
                String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.setPackage("com.whatsapp");
                startActivity(i);
            } catch (Exception ex) {
                Toast.makeText(this, "WhatsApp not installed or number invalid.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendSms() {
        String statusMessage;
        if (netBalance > 0) {
            statusMessage = String.format(Locale.getDefault(), "A friendly reminder that you have a pending payment of ₹%.2f.", netBalance);
        } else if (netBalance < 0) {
            statusMessage = String.format(Locale.getDefault(), "A confirmation that I have a pending payment to you of ₹%.2f.", Math.abs(netBalance));
        } else {
            statusMessage = "Just to confirm, our account is settled.";
        }

        String finalMessage = "Hello " + customerName + ", " + statusMessage + " Thank you! - " + getString(R.string.app_name);

        String phone = customerPhone.replaceAll("[^\\d+]", "");
        if (phone.startsWith("+")) phone = phone.substring(1);

        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
            intent.putExtra("sms_body", finalMessage);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReportGenerated(List<Transaction> transactions, String dateRangeLabel) {
        if (transactions.isEmpty()) {
            Toast.makeText(this, "No transactions for the selected period.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Opening balance (if you do not have opening balance, keep 0)
        double openingBalance = 0;

        File pdfFile = PdfGenerator.generatePdf(
                this,
                customerName,
                customerPhone,
                transactions,
                dateRangeLabel,
                openingBalance
        );

        if (pdfFile != null) {
            showPdfOptionsDialog(pdfFile);
        } else {
            Toast.makeText(this, "Failed to generate PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPdfOptionsDialog(final File pdfFile) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Report Generated")
                .setPositiveButton("Share", (d, w) -> sharePdfIntent(pdfFile))
                .setNeutralButton("View", (d, w) -> viewPdfIntent(pdfFile))
                .setNegativeButton("Dismiss", null)
                .show();
    }

    private void viewPdfIntent(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdfIntent(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Report - " + customerName);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share PDF"));
    }

    private void updateSummary(double totalGave, double totalGot) {
        TextView tvBalance = findViewById(R.id.tv_details_net_balance);
        TextView tvLabel = findViewById(R.id.tv_details_balance_label);
        tvTotalGave.setText(String.format(Locale.getDefault(), "₹%.2f", totalGave));
        tvTotalGot.setText(String.format(Locale.getDefault(), "₹%.2f", totalGot));
        this.netBalance = totalGave - totalGot;

        String balanceText = String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance));
        tvBalance.setText(balanceText);

        if (netBalance > 0) {
            tvLabel.setText("You will get");
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else if (netBalance < 0) {
            tvLabel.setText("You will give");
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            tvLabel.setText("Settled Up");
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_customer_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new TransactionAdapter(transactionList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.fab_add_specific_transaction).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("edit_customer_phone", customerPhone);
            intent.putExtra("edit_customer_name", customerName);
            startActivity(intent);
        });
        findViewById(R.id.btn_whatsapp).setOnClickListener(v -> sendWhatsAppMessage());
        findViewById(R.id.btn_sms).setOnClickListener(v -> sendSms());
        findViewById(R.id.btn_report).setOnClickListener(v -> {
            ReportOptionsBottomSheet bottomSheet = ReportOptionsBottomSheet.newInstance(transactionList);
            bottomSheet.show(getSupportFragmentManager(), "ReportBottomSheet");
        });
    }

    private void loadTransactions() {
        String userNode = prefManager.getUserEmail().replace(".", ",");
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(userNode)
                .child("transactions")
                .child(customerPhone);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                double totalGave = 0, totalGot = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t != null) {

                        transactionList.add(t);

                        if ("gave".equalsIgnoreCase(t.getType()))
                            totalGave += t.getAmount();
                        else
                            totalGot += t.getAmount();
                    }
                }

                // 🔥 SORT BY FIREBASE DATE STRING (dd MMM yyyy)
                Collections.sort(transactionList, (t1, t2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        Date d1 = sdf.parse(t1.getDate());
                        Date d2 = sdf.parse(t2.getDate());

                        if (d1 != null && d2 != null)
                            return d2.compareTo(d1);   // NEWEST DATE FIRST
                    } catch (Exception e) {
                        // fallback to timestamp if parsing fails
                        return Long.compare(t2.getTimestamp(), t1.getTimestamp());
                    }
                    return 0;
                });

                adapter.notifyDataSetChanged();
                updateSummary(totalGave, totalGot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerDetailsActivity.this,
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Transaction transaction) {
        Intent intent = new Intent(this, EntryDetailsActivity.class);
        intent.putExtra("TRANSACTION_DATA", transaction);
        intent.putExtra("CUSTOMER_NAME", customerName);
        startActivity(intent);
    }
}