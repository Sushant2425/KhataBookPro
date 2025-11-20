package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CustomerReportAdapter;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerReportFragment extends Fragment {

    private static final String TAG = "CustomerReportFragment";

    private RecyclerView rvCustomerList;
    private FloatingActionButton fabExportAll;
    private SearchView searchView;

    private DatabaseReference customerRef, transRef;

    private List<CustomerReport> customerReports = new ArrayList<>();
    private List<CustomerReport> filteredReports = new ArrayList<>();
    private CustomerReportAdapter adapter;

    private int totalCustomers = 0;
    private int loadedCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_reports, container, false);

        rvCustomerList = v.findViewById(R.id.rvCustomerList);
        fabExportAll = v.findViewById(R.id.fabExportAll);
        searchView = v.findViewById(R.id.searchView);

        rvCustomerList.setLayoutManager(new LinearLayoutManager(getContext()));

        PrefManager prefManager = new PrefManager(requireContext());
        String userEmail = prefManager.getUserEmail();

        if (userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return v;
        }

        String emailKey = userEmail.replace(".", ",");
        String basePath = "Khatabook/" + emailKey;
        Log.d(TAG, "Fetching data from: " + basePath);

        customerRef = FirebaseDatabase.getInstance().getReference(basePath + "/customers");
        transRef = FirebaseDatabase.getInstance().getReference(basePath + "/transactions");

        fabExportAll.setOnClickListener(view -> exportAllCustomersPdf());

        setupSearchView();
        loadCustomersWithBalances();

        return v;
    }

    private void setupSearchView() {
        searchView.setQueryHint("Search by name or mobile number");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCustomers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCustomers(newText);
                return true;
            }
        });
    }

    private void filterCustomers(String query) {
        if (TextUtils.isEmpty(query)) {
            filteredReports.clear();
            filteredReports.addAll(customerReports);
        } else {
            filteredReports.clear();
            String lowerQuery = query.toLowerCase();
            for (CustomerReport r : customerReports) {
                if (r.getName().toLowerCase().contains(lowerQuery)
                        || r.getPhone().toLowerCase().contains(lowerQuery)) {
                    filteredReports.add(r);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadCustomersWithBalances() {
        customerRef.get().addOnSuccessListener(customerSnap -> {
            Log.d(TAG, "Customers count: " + customerSnap.getChildrenCount());
            customerReports.clear();
            filteredReports.clear();
            totalCustomers = (int) customerSnap.getChildrenCount();
            loadedCount = 0;

            if (totalCustomers == 0) {
                showEmptyState();
                return;
            }

            for (DataSnapshot c : customerSnap.getChildren()) {
                String phone = c.child("phone").getValue(String.class);
                String name = c.child("name").getValue(String.class);

                if (phone == null || name == null) {
                    Log.w(TAG, "Invalid customer data: " + c.getKey());
                    continue;
                }

                fetchTransactionsAndAddCustomer(name, phone);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load customers", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showEmptyState();
        });
    }

    private void fetchTransactionsAndAddCustomer(String name, String phone) {
        transRef.child(phone).get().addOnSuccessListener(tsnap -> {
            double gave = 0, received = 0;
            List<Transaction> txns = new ArrayList<>();

            for (DataSnapshot t : tsnap.getChildren()) {
                Double amt = t.child("amount").getValue(Double.class);
                String type = t.child("type").getValue(String.class);
                String date = t.child("date").getValue(String.class);
                String note = t.child("note").getValue(String.class);

                if (amt == null) amt = 0.0;
                if ("gave".equalsIgnoreCase(type)) gave += amt;
                else if ("received".equalsIgnoreCase(type)) received += amt;

                txns.add(new Transaction(date, type, amt, note));
            }

            double balance = received - gave;
            customerReports.add(new CustomerReport(name, phone, balance, txns));
            Log.d(TAG, "Loaded: " + name + " | Balance: ₹" + balance);

            loadedCount++;
            if (loadedCount == totalCustomers) {
                updateAdapter();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load transactions for " + phone, e);
            customerReports.add(new CustomerReport(name, phone, 0.0, new ArrayList<>()));
            loadedCount++;
            if (loadedCount == totalCustomers) updateAdapter();
        });
    }

    private void updateAdapter() {
        filteredReports.clear();
        filteredReports.addAll(customerReports);
        adapter = new CustomerReportAdapter(filteredReports, this::generateSingleCustomerPdf);
        rvCustomerList.setAdapter(adapter);
        Log.d(TAG, "Adapter updated with " + customerReports.size() + " customers");
    }

    private void showEmptyState() {
        updateAdapter();
        Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show();
    }

    // ===================== PDF GENERATION METHODS =====================

    private void generateSingleCustomerPdf(CustomerReport cr) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        drawPdfContent(canvas, cr);

        pdfDocument.finishPage(page);
        savePdfAndShare(pdfDocument, "Report_" + cr.getPhone() + ".pdf");
    }

    private void exportAllCustomersPdf() {
        if (customerReports.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();

        int pageNumber = 1;
        for (CustomerReport cr : customerReports) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber++).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            drawPdfContent(canvas, cr);

            pdfDocument.finishPage(page);
        }

        savePdfAndShare(pdfDocument, "All_Customers_Report.pdf");
    }

    private void drawPdfContent(Canvas canvas, CustomerReport cr) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float yPos = 60;

        // ========== HEADER ==========
        paint.setColor(Color.parseColor("#2C3E50"));
        paint.setTextSize(24);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Customer Transaction Report", 50, yPos, paint);
        yPos += 10;

        paint.setStrokeWidth(2);
        canvas.drawLine(50, yPos, 545, yPos, paint);
        yPos += 30;

        // ========== CUSTOMER DETAILS ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.BLACK);
        canvas.drawText("Customer Name: " + cr.getName(), 50, yPos, paint);
        yPos += 25;
        canvas.drawText("Phone: " + cr.getPhone(), 50, yPos, paint);
        yPos += 25;

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(cr.getBalance() < 0 ? Color.RED : Color.parseColor("#27AE60"));
        canvas.drawText("Balance: ₹" + String.format("%.2f", cr.getBalance()), 50, yPos, paint);
        yPos += 35;

        paint.setColor(Color.BLACK);

        // ========== TABLE HEADER ==========
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#34495E"));
        canvas.drawRect(50, yPos, 545, yPos + 30, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Date", 60, yPos + 20, paint);
        canvas.drawText("Type", 170, yPos + 20, paint);
        canvas.drawText("Amount", 280, yPos + 20, paint);
        canvas.drawText("Note", 390, yPos + 20, paint);
        yPos += 30;

        // ========== TABLE ROWS ==========
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(11);

        int row = 0;
        for (Transaction t : cr.getTransactions()) {
            if (yPos > 750) break; // prevent overflow

            // Alternate row background
            if (row % 2 == 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#ECF0F1"));
                canvas.drawRect(50, yPos, 545, yPos + 25, paint);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            canvas.drawText(t.date != null ? t.date : "-", 60, yPos + 18, paint);
            canvas.drawText(t.type != null ? t.type : "-", 170, yPos + 18, paint);
            canvas.drawText(String.format("₹%.2f", t.amount), 280, yPos + 18, paint);
            canvas.drawText(t.note != null ? t.note : "-", 390, yPos + 18, paint);

            // Row border
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(1);
            canvas.drawLine(50, yPos + 25, 545, yPos + 25, paint);

            yPos += 25;
            row++;
        }

        // Table bottom border
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        canvas.drawLine(50, yPos, 545, yPos, paint);

        // ========== FOOTER ==========
        paint.setColor(Color.GRAY);
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        String footer = "Generated on: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(new Date());
        canvas.drawText(footer, 50, 800, paint);
        canvas.drawText("Powered by MyKhata Pro", 380, 800, paint);
    }

    private void savePdfAndShare(PdfDocument pdfDocument, String fileName) {
        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();

            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF"));

            Toast.makeText(requireContext(), "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving PDF", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ===================== INNER CLASSES =====================

    public static class CustomerReport {
        private final String name, phone;
        private final double balance;
        private final List<Transaction> transactions;

        public CustomerReport(String n, String p, double b, List<Transaction> t) {
            name = n;
            phone = p;
            balance = b;
            transactions = t;
        }

        public String getName() { return name; }
        public String getPhone() { return phone; }
        public double getBalance() { return balance; }
        public List<Transaction> getTransactions() { return transactions; }
    }

    public static class Transaction {
        public final String date, type, note;
        public final double amount;

        public Transaction(String d, String t, double a, String n) {
            date = d;
            type = t;
            amount = a;
            note = n;
        }
    }
}
