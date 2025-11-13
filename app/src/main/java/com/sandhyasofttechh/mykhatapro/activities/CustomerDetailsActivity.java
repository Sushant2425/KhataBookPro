package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CustomerHistoryAdapter;
import com.sandhyasofttechh.mykhatapro.model.Customer;
import com.sandhyasofttechh.mykhatapro.model.PaymentEntry;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvDue, tvTotalGave, tvTotalGot, tvNoHistory;
    private RecyclerView recyclerHistory;
    private CustomerHistoryAdapter adapter;
    private MaterialButton btnSharePdf;
    private TextInputEditText searchHistory;
    private DatabaseReference transRef;
    private PrefManager prefManager;
    private MaterialButton btnCall, btnWhatsApp;
    private List<PaymentEntry> allEntries = new ArrayList<>();
    private List<PaymentEntry> filteredEntries = new ArrayList<>();
    private Customer customer;

    private static final int STORAGE_PERMISSION_CODE = 101;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        initViews();
        setupToolbar();
        getCustomerData();
        setupRecyclerView();
        setupSearch();
        setupShareButton();
        loadPaymentHistory();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvName = findViewById(R.id.tv_detail_name);
        tvPhone = findViewById(R.id.tv_detail_phone);
        tvDue = findViewById(R.id.tv_detail_due);
        tvTotalGave = findViewById(R.id.tv_total_gave);
        tvTotalGot = findViewById(R.id.tv_total_got);
        tvNoHistory = findViewById(R.id.tv_no_history);
        recyclerHistory = findViewById(R.id.recycler_history);
        btnSharePdf = findViewById(R.id.btn_share_pdf);
        searchHistory = findViewById(R.id.search_history);
        btnCall = findViewById(R.id.btn_call);
        btnWhatsApp = findViewById(R.id.btn_whatsapp);
        adapter = new CustomerHistoryAdapter();
        prefManager = new PrefManager(this);

        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + customer.getPhone()));
            startActivity(intent);
        });

        btnWhatsApp.setOnClickListener(v -> {
            String url = "https://api.whatsapp.com/send?phone=91" + customer.getPhone();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

    }

    private void setupToolbar() {
        // Title set later
    }

    private void getCustomerData() {
        customer = (Customer) getIntent().getSerializableExtra("customer");
        if (customer == null) {
            Toast.makeText(this, "Customer data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getSupportActionBar().setTitle(customer.getName());
        tvName.setText(customer.getName());
        tvPhone.setText(customer.getPhone());
        double due = customer.getPendingAmount();
        tvDue.setText(due > 0 ? String.format("₹%.2f due", due) : "No due amount");
    }

    private void setupRecyclerView() {
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(adapter);
    }

    private void setupSearch() {
        searchHistory.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String query) {
        filteredEntries.clear();
        if (query.isEmpty()) {
            filteredEntries.addAll(allEntries);
        } else {
            String q = query.toLowerCase(Locale.getDefault());
            for (PaymentEntry e : allEntries) {
                if (e.getNote() != null && e.getNote().toLowerCase(Locale.getDefault()).contains(q) ||
                        dateFormat.format(new Date(e.getDate())).toLowerCase(Locale.getDefault()).contains(q) ||
                        String.format("%.2f", e.getAmount()).contains(q)) {
                    filteredEntries.add(e);
                }
            }
        }
        adapter.setEntries(filteredEntries);
        updateEmptyState();
    }

    private void setupShareButton() {
        btnSharePdf.setOnClickListener(v -> generateAndSharePDF()); // DIRECT CALL
    }

    private void loadPaymentHistory() {
        String userEmail = prefManager.getUserEmail().replace(".", ",");
        transRef = FirebaseDatabase.getInstance().getReference("Khatabook")
                .child(userEmail).child("transactions");

        transRef.orderByChild("customerPhone").equalTo(customer.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allEntries.clear();
                        double gave = 0, got = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                PaymentEntry entry = ds.getValue(PaymentEntry.class);
                                if (entry != null && entry.getType() != null) {
                                    allEntries.add(entry);
                                    if ("gave".equals(entry.getType())) gave += entry.getAmount();
                                    else if ("got".equals(entry.getType())) got += entry.getAmount();
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                        filteredEntries = new ArrayList<>(allEntries);
                        adapter.setEntries(filteredEntries);
                        updateSummary(gave, got);
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CustomerDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSummary(double gave, double got) {
        tvTotalGave.setText(String.format("₹%.2f", gave));
        tvTotalGot.setText(String.format("₹%.2f", got));
    }

    private void updateEmptyState() {
        boolean empty = filteredEntries.isEmpty();
        tvNoHistory.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

//    private void checkPermissionAndShare() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    STORAGE_PERMISSION_CODE);
//        } else {
//            generateAndSharePDF();
//        }
//    }

//    @Override
//    public vo
//    id onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            generateAndSharePDF();
//        } else {
//            Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void generateAndSharePDF() {
        if (filteredEntries.isEmpty()) {
            Toast.makeText(this, "No data to share", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(800, 1200, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(28);
        paint.setFakeBoldText(true);

        int y = 100;
        canvas.drawText(customer.getName() + " - Payment History", 40, y, paint);
        y += 50;
        paint.setTextSize(20);
        canvas.drawText("Phone: " + customer.getPhone(), 40, y, paint);
        y += 40;
        canvas.drawText("Net Due: ₹" + String.format("%.2f", customer.getPendingAmount()), 40, y, paint);
        y += 70;

        paint.setTextSize(18);
        paint.setFakeBoldText(false);
        for (PaymentEntry e : filteredEntries) {
            String line = String.format("%s  ₹%.2f  %s",
                    e.getType().equals("gave") ? "You Gave" : "You Got",
                    e.getAmount(),
                    dateFormat.format(new Date(e.getDate())));
            if (e.getNote() != null && !e.getNote().isEmpty()) {
                line += "  [" + e.getNote() + "]";
            }
            canvas.drawText(line, 40, y, paint);
            y += 35;
            if (y > 1100) {
                pdf.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(800, 1200, pdf.getPages().size() + 1).create();
                page = pdf.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 80;
            }
        }
        pdf.finishPage(page);

        try {
            // NO PERMISSION NEEDED
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    customer.getName().replace(" ", "_") + "_History.pdf");
            pdf.writeTo(new FileOutputStream(file));

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share PDF via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdf.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}