package com.sandhyasofttechh.mykhatapro.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CustomerDetailsActivity extends AppCompatActivity implements ReportOptionsBottomSheet.ReportListener, TransactionAdapter.OnItemClickListener {

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

        Toolbar toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(customerName);

        tvTotalGave = findViewById(R.id.tv_total_gave);
        tvTotalGot = findViewById(R.id.tv_total_got);
        
        setupRecyclerView();
        setupClickListeners();
        loadTransactions();
    }
    
    // --- THIS IS THE NEW, PROFESSIONAL WHATSAPP METHOD ---
    private void sendWhatsAppMessage() {
        String appName = getString(R.string.app_name);
        File imageFile = ImageGenerator.generateShareableImage(this, customerName, netBalance, appName);
        if (imageFile == null) {
            Toast.makeText(this, "Failed to create shareable image.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Uri imageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);

        // --- FIXED: Clearer and more professional message ---
        String finalMessage;
        if (netBalance > 0) { // Customer owes you money
            finalMessage = "Hello " + customerName + ",\nThis is a friendly reminder for your pending payment. Thank you!";
        } else if (netBalance < 0) { // You owe the customer money
            finalMessage = "Hello " + customerName + ",\nThis is a confirmation of my pending payment to you. Thank you!";
        } else {
            finalMessage = "Hello " + customerName + ",\nJust to confirm, our account is currently settled. Thank you for your business!";
        }
        
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setPackage("com.whatsapp");
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra(Intent.EXTRA_TEXT, finalMessage);
            intent.putExtra("jid", customerPhone.replace("+", "") + "@s.whatsapp.net");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed or contact not on WhatsApp.", Toast.LENGTH_LONG).show();
        }
    }
    
    // --- FIXED: Clearer and more professional SMS message ---
    private void sendSms() {
        String statusMessage;
        if (netBalance > 0) { // Customer owes you money
            statusMessage = String.format(Locale.getDefault(), "A friendly reminder that you have a pending payment of ₹%.2f.", netBalance);
        } else if (netBalance < 0) { // You owe the customer money
            statusMessage = String.format(Locale.getDefault(), "A confirmation that I have a pending payment to you of ₹%.2f.", Math.abs(netBalance));
        } else {
            statusMessage = "Just to confirm, our account is settled.";
        }
        
        String finalMessage = "Hello " + customerName + ", " + statusMessage + " Thank you. - " + getString(R.string.app_name);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + customerPhone));
        intent.putExtra("sms_body", finalMessage);
        startActivity(intent);
    }
    
    // --- All other methods are unchanged and correct ---

    @Override
    public void onReportGenerated(List<Transaction> transactions, String dateRangeLabel) {
        if (transactions.isEmpty()) {
            Toast.makeText(this, "No transactions for the selected period.", Toast.LENGTH_SHORT).show();
            return;
        }
        File pdfFile = PdfGenerator.generatePdf(this, customerName, customerPhone, transactions, dateRangeLabel);
        if (pdfFile != null) {
            showPdfOptionsDialog(pdfFile);
        } else {
            Toast.makeText(this, "Failed to generate PDF.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showPdfOptionsDialog(final File pdfFile) {
        new AlertDialog.Builder(this)
            .setTitle("Report Generated")
            .setPositiveButton("Share", (dialog, which) -> sharePdfIntent(pdfFile))
            .setNeutralButton("View", (dialog, which) -> viewPdfIntent(pdfFile))
            .setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss())
            .show();
    }
    
    private void viewPdfIntent(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No application available to view PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdfIntent(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Transaction Report for " + customerName);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Report via..."));
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
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else if (netBalance < 0) {
            tvLabel.setText("You will give");
            tvBalance.setTextColor(ContextCompat.getColor(this, R.color.error));
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
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void loadTransactions() {
        PrefManager prefManager = new PrefManager(this);
        String userNode = prefManager.getUserEmail().replace(".", ",");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(userNode).child("transactions").child(customerPhone);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                double totalGave = 0, totalGot = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction transaction = ds.getValue(Transaction.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                        if ("gave".equals(transaction.getType())) totalGave += transaction.getAmount();
                        else totalGot += transaction.getAmount();
                    }
                }
                Collections.sort(transactionList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                adapter.notifyDataSetChanged();
                updateSummary(totalGave, totalGot);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(CustomerDetailsActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show(); }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Transaction transaction) {
        Intent intent = new Intent(this, EntryDetailsActivity.class);
        intent.putExtra("TRANSACTION_DATA", transaction);
        intent.putExtra("CUSTOMER_NAME", customerName);
        startActivity(intent);
    }
}