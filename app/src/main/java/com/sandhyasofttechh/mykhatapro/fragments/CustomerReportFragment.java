//
//package com.sandhyasofttechh.mykhatapro.fragments;
//
//import android.content.Intent;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Typeface;
//import android.graphics.pdf.PdfDocument;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.widget.SearchView;
//import androidx.core.content.FileProvider;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.adapter.CustomerReportAdapter;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class CustomerReportFragment extends Fragment {
//
//    private static final String TAG = "CustomerReportFragment";
//
//    private RecyclerView rvCustomerList;
//    private FloatingActionButton fabExportAll;
//    private SearchView searchView;
//
//    private DatabaseReference customerRef, transRef;
//
//    private List<CustomerReport> customerReports = new ArrayList<>();
//    private List<CustomerReport> filteredReports = new ArrayList<>();
//    private CustomerReportAdapter adapter;
//
//    private int totalCustomers = 0;
//    private int loadedCount = 0;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_customer_reports, container, false);
//
//        rvCustomerList = v.findViewById(R.id.rvCustomerList);
//        fabExportAll = v.findViewById(R.id.fabExportAll);
//        searchView = v.findViewById(R.id.searchView);
//
//        rvCustomerList.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        PrefManager prefManager = new PrefManager(requireContext());
//        String userEmail = prefManager.getUserEmail();
//
//        if (userEmail.isEmpty()) {
//            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
//            return v;
//        }
//
//        String emailKey = userEmail.replace(".", ",");
//        String basePath = "Khatabook/" + emailKey;
//        Log.d(TAG, "Fetching data from: " + basePath);
//
//        customerRef = FirebaseDatabase.getInstance().getReference(basePath + "/customers");
//        transRef = FirebaseDatabase.getInstance().getReference(basePath + "/transactions");
//
//        fabExportAll.setOnClickListener(view -> exportAllCustomersPdf());
//
//        setupSearchView();
//        loadCustomersWithBalances();
//
//        return v;
//    }
//
//    private void setupSearchView() {
//        searchView.setQueryHint("Search by name or mobile number");
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                filterCustomers(query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterCustomers(newText);
//                return true;
//            }
//        });
//    }
//
//    private void filterCustomers(String query) {
//        if (TextUtils.isEmpty(query)) {
//            filteredReports.clear();
//            filteredReports.addAll(customerReports);
//        } else {
//            filteredReports.clear();
//            String lowerQuery = query.toLowerCase();
//            for (CustomerReport r : customerReports) {
//                if (r.getName().toLowerCase().contains(lowerQuery)
//                        || r.getPhone().toLowerCase().contains(lowerQuery)) {
//                    filteredReports.add(r);
//                }
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }
//
//    private void loadCustomersWithBalances() {
//        customerRef.get().addOnSuccessListener(customerSnap -> {
//            Log.d(TAG, "Customers count: " + customerSnap.getChildrenCount());
//            customerReports.clear();
//            filteredReports.clear();
//            totalCustomers = (int) customerSnap.getChildrenCount();
//            loadedCount = 0;
//
//            if (totalCustomers == 0) {
//                showEmptyState();
//                return;
//            }
//
//            for (DataSnapshot c : customerSnap.getChildren()) {
//                String phone = c.child("phone").getValue(String.class);
//                String name = c.child("name").getValue(String.class);
//
//                if (phone == null || name == null) {
//                    Log.w(TAG, "Invalid customer data: " + c.getKey());
//                    continue;
//                }
//
//                fetchTransactionsAndAddCustomer(name, phone);
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Failed to load customers", e);
//            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            showEmptyState();
//        });
//    }
//
//    private void fetchTransactionsAndAddCustomer(String name, String phone) {
//        transRef.child(phone).get().addOnSuccessListener(tsnap -> {
//            double gave = 0, received = 0;
//            List<Transaction> txns = new ArrayList<>();
//
//            for (DataSnapshot t : tsnap.getChildren()) {
//                Double amt = t.child("amount").getValue(Double.class);
//                String type = t.child("type").getValue(String.class);
//                String date = t.child("date").getValue(String.class);
//                String note = t.child("note").getValue(String.class);
//
//                if (amt == null) amt = 0.0;
//                if ("gave".equalsIgnoreCase(type)) gave += amt;
//                else if ("received".equalsIgnoreCase(type)) received += amt;
//
//                txns.add(new Transaction(date, type, amt, note));
//            }
//
//            double balance = received - gave;
//            customerReports.add(new CustomerReport(name, phone, balance, txns, gave, received));
//            Log.d(TAG, "Loaded: " + name + " | Balance: ₹" + balance);
//
//            loadedCount++;
//            if (loadedCount == totalCustomers) {
//                updateAdapter();
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Failed to load transactions for " + phone, e);
//            customerReports.add(new CustomerReport(name, phone, 0.0, new ArrayList<>(), 0.0, 0.0));
//            loadedCount++;
//            if (loadedCount == totalCustomers) updateAdapter();
//        });
//    }
//
//    private void updateAdapter() {
//        filteredReports.clear();
//        filteredReports.addAll(customerReports);
//        adapter = new CustomerReportAdapter(filteredReports, this::generateSingleCustomerPdf);
//        rvCustomerList.setAdapter(adapter);
//        Log.d(TAG, "Adapter updated with " + customerReports.size() + " customers");
//    }
//
//    private void showEmptyState() {
//        updateAdapter();
//        Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show();
//    }
//
//    // ===================== PROFESSIONAL PDF GENERATION =====================
//
//    private void generateSingleCustomerPdf(CustomerReport cr) {
//        PdfDocument pdfDocument = new PdfDocument();
//
//        // Calculate pages needed based on transactions
//        int maxTransactionsPerPage = 18;
//        int totalPages = Math.max(1, (int) Math.ceil((double) cr.getTransactions().size() / maxTransactionsPerPage));
//
//        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
//            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNum + 1).create();
//            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
//            Canvas canvas = page.getCanvas();
//
//            int startIdx = pageNum * maxTransactionsPerPage;
//            int endIdx = Math.min(startIdx + maxTransactionsPerPage, cr.getTransactions().size());
//            List<Transaction> pageTransactions = cr.getTransactions().subList(startIdx, endIdx);
//
//            drawPdfContent(canvas, cr, pageTransactions, pageNum + 1, totalPages);
//            pdfDocument.finishPage(page);
//        }
//
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
//        savePdfAndShare(pdfDocument, "CustomerReport_" + cr.getPhone() + "_" + timestamp + ".pdf");
//    }
//
//    private void exportAllCustomersPdf() {
//        if (customerReports.isEmpty()) {
//            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        PdfDocument pdfDocument = new PdfDocument();
//        int globalPageNumber = 1;
//
//        for (CustomerReport cr : customerReports) {
//            int maxTransactionsPerPage = 18;
//            int pagesForCustomer = Math.max(1, (int) Math.ceil((double) cr.getTransactions().size() / maxTransactionsPerPage));
//
//            for (int pageNum = 0; pageNum < pagesForCustomer; pageNum++) {
//                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, globalPageNumber++).create();
//                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
//                Canvas canvas = page.getCanvas();
//
//                int startIdx = pageNum * maxTransactionsPerPage;
//                int endIdx = Math.min(startIdx + maxTransactionsPerPage, cr.getTransactions().size());
//                List<Transaction> pageTransactions = cr.getTransactions().subList(startIdx, endIdx);
//
//                drawPdfContent(canvas, cr, pageTransactions, pageNum + 1, pagesForCustomer);
//                pdfDocument.finishPage(page);
//            }
//        }
//
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
//        savePdfAndShare(pdfDocument, "AllCustomersReport_" + timestamp + ".pdf");
//    }
//
//    private void drawPdfContent(Canvas canvas, CustomerReport cr, List<Transaction> transactions,
//                                int currentPage, int totalPages) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//
//        float pageWidth = 595f;
//        float pageHeight = 842f;
//        float margin = 40f;
//        float contentWidth = pageWidth - (2 * margin);
//
//        // ===== HEADER SECTION =====
//        float yPos = margin;
//
//        // Header background with gradient effect
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.parseColor("#1A237E")); // Deep blue
//        canvas.drawRect(0, 0, pageWidth, 100, paint);
//
//        // Accent bar
//        paint.setColor(Color.parseColor("#FFC107")); // Amber
//        canvas.drawRect(0, 95, pageWidth, 100, paint);
//
//        // Title
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(22);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        canvas.drawText("CUSTOMER TRANSACTION REPORT", margin, 45, paint);
//
//        // Report type indicator
//        paint.setTextSize(10);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//        canvas.drawText("Detailed Statement", margin, 70, paint);
//
//        // Page number
//        String pageInfo = "Page " + currentPage + " of " + totalPages;
//        float pageInfoWidth = paint.measureText(pageInfo);
//        canvas.drawText(pageInfo, pageWidth - margin - pageInfoWidth, 70, paint);
//
//        yPos = 120;
//
//        // ===== CUSTOMER INFO CARD (Only on first page) =====
//        if (currentPage == 1) {
//            // Card background
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(Color.parseColor("#F8F9FA"));
//            canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 110, 8, 8, paint);
//
//            // Card border
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(1.5f);
//            paint.setColor(Color.parseColor("#E0E0E0"));
//            canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 110, 8, 8, paint);
//            paint.setStyle(Paint.Style.FILL);
//
//            // Customer details
//            float infoY = yPos + 25;
//            paint.setColor(Color.parseColor("#424242"));
//            paint.setTextSize(11);
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText("Customer Name:", margin + 15, infoY, paint);
//
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//            paint.setTextSize(12);
//            canvas.drawText(cr.getName(), margin + 130, infoY, paint);
//
//            infoY += 22;
//            paint.setTextSize(11);
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText("Phone Number:", margin + 15, infoY, paint);
//
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//            paint.setTextSize(12);
//            canvas.drawText(cr.getPhone(), margin + 130, infoY, paint);
//
//            infoY += 22;
//            paint.setTextSize(11);
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText("Total Transactions:", margin + 15, infoY, paint);
//
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//            paint.setTextSize(12);
//            canvas.drawText(String.valueOf(cr.getTransactions().size()), margin + 130, infoY, paint);
//
//            // Financial summary box (right side)
//            float summaryX = pageWidth - margin - 180;
//
//            // Total Gave
//            paint.setTextSize(9);
//            paint.setColor(Color.parseColor("#757575"));
//            canvas.drawText("Total Given:", summaryX, yPos + 25, paint);
//            paint.setTextSize(11);
//            paint.setColor(Color.parseColor("#C62828"));
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText(String.format("₹%.2f", cr.getTotalGave()), summaryX, yPos + 42, paint);
//
//            // Total Received
//            paint.setTextSize(9);
//            paint.setColor(Color.parseColor("#757575"));
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//            canvas.drawText("Total Received:", summaryX, yPos + 60, paint);
//            paint.setTextSize(11);
//            paint.setColor(Color.parseColor("#2E7D32"));
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText(String.format("₹%.2f", cr.getTotalReceived()), summaryX, yPos + 77, paint);
//
//            // Net Balance (highlighted)
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(cr.getBalance() >= 0 ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFEBEE"));
//            canvas.drawRoundRect(summaryX - 10, yPos + 85, summaryX + 170, yPos + 107, 5, 5, paint);
//
//            paint.setTextSize(10);
//            paint.setColor(Color.parseColor("#424242"));
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText("Net Balance:", summaryX, yPos + 100, paint);
//
//            paint.setTextSize(14);
//            paint.setColor(cr.getBalance() >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
//            String balanceText = String.format("₹%.2f", Math.abs(cr.getBalance()));
//            if (cr.getBalance() < 0) balanceText += " DR";
//            canvas.drawText(balanceText, summaryX + 80, yPos + 100, paint);
//
//            yPos += 125;
//        }
//
//        // ===== TRANSACTIONS TABLE =====
//        float tableTop = yPos;
//        float rowHeight = 32f;
//
//        // Column configuration
//        float col1Width = 45f;   // S.No
//        float col2Width = 85f;   // Date
//        float col3Width = 75f;   // Type
//        float col4Width = 85f;   // Amount
//        float col5Width = contentWidth - col1Width - col2Width - col3Width - col4Width; // Note
//
//        // Table header background
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.parseColor("#37474F"));
//        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paint);
//
//        // Header text
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(10);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//
//        float textY = yPos + 20;
//        float currentX = margin;
//        canvas.drawText("S.No", currentX + 8, textY, paint);
//        currentX += col1Width;
//        canvas.drawText("Date", currentX + 8, textY, paint);
//        currentX += col2Width;
//        canvas.drawText("Type", currentX + 8, textY, paint);
//        currentX += col3Width;
//        canvas.drawText("Amount", currentX + 8, textY, paint);
//        currentX += col4Width;
//        canvas.drawText("Description", currentX + 8, textY, paint);
//
//        yPos += rowHeight;
//
//        // ===== TRANSACTION ROWS =====
//        paint.setTextSize(9);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//
//        int startSerialNo = (currentPage - 1) * 18 + 1;
//        int rowNum = 0;
//
//        for (Transaction t : transactions) {
//            // Alternate row background
//            if (rowNum % 2 == 0) {
//                paint.setColor(Color.parseColor("#FAFAFA"));
//                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paint);
//            }
//
//            textY = yPos + 20;
//            currentX = margin;
//
//            // Serial number
//            paint.setColor(Color.parseColor("#616161"));
//            canvas.drawText(String.valueOf(startSerialNo + rowNum), currentX + 8, textY, paint);
//            currentX += col1Width;
//
//            // Date
//            paint.setColor(Color.parseColor("#424242"));
//            String dateStr = (t.date != null && !t.date.isEmpty()) ? t.date : "N/A";
//            canvas.drawText(dateStr, currentX + 8, textY, paint);
//            currentX += col2Width;
//
//            // Type with badge
//            String type = (t.type != null) ? t.type.toUpperCase() : "N/A";
//            if ("GAVE".equals(type)) {
//                paint.setStyle(Paint.Style.FILL);
//                paint.setColor(Color.parseColor("#FFEBEE"));
//                canvas.drawRoundRect(currentX + 5, yPos + 8, currentX + 65, yPos + 24, 3, 3, paint);
//                paint.setColor(Color.parseColor("#C62828"));
//                canvas.drawText("GAVE", currentX + 15, textY, paint);
//            } else if ("RECEIVED".equals(type)) {
//                paint.setStyle(Paint.Style.FILL);
//                paint.setColor(Color.parseColor("#E8F5E9"));
//                canvas.drawRoundRect(currentX + 5, yPos + 8, currentX + 75, yPos + 24, 3, 3, paint);
//                paint.setColor(Color.parseColor("#2E7D32"));
//                canvas.drawText("RECEIVED", currentX + 10, textY, paint);
//            } else {
//                paint.setColor(Color.parseColor("#757575"));
//                canvas.drawText(type, currentX + 8, textY, paint);
//            }
//            currentX += col3Width;
//
//            // Amount
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            if ("GAVE".equals(type)) {
//                paint.setColor(Color.parseColor("#C62828"));
//            } else if ("RECEIVED".equals(type)) {
//                paint.setColor(Color.parseColor("#2E7D32"));
//            } else {
//                paint.setColor(Color.parseColor("#424242"));
//            }
//            canvas.drawText(String.format("₹%.2f", t.amount), currentX + 8, textY, paint);
//            currentX += col4Width;
//
//            // Note (truncated if too long)
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//            paint.setColor(Color.parseColor("#616161"));
//            String note = (t.note != null && !t.note.isEmpty()) ? t.note : "-";
//            if (note.length() > 30) {
//                note = note.substring(0, 27) + "...";
//            }
//            canvas.drawText(note, currentX + 8, textY, paint);
//
//            // Row separator
//            paint.setColor(Color.parseColor("#E0E0E0"));
//            paint.setStrokeWidth(0.5f);
//            canvas.drawLine(margin, yPos + rowHeight, pageWidth - margin, yPos + rowHeight, paint);
//
//            yPos += rowHeight;
//            rowNum++;
//        }
//
//        // ===== TABLE BORDER =====
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(2f);
//        paint.setColor(Color.parseColor("#37474F"));
//        canvas.drawRect(margin, tableTop, pageWidth - margin, yPos, paint);
//
//        // Vertical grid lines
//        paint.setStrokeWidth(1f);
//        paint.setColor(Color.parseColor("#BDBDBD"));
//        currentX = margin + col1Width;
//        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
//        currentX += col2Width;
//        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
//        currentX += col3Width;
//        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
//        currentX += col4Width;
//        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
//
//        // ===== FOOTER =====
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.parseColor("#F5F5F5"));
//        canvas.drawRect(0, pageHeight - 50, pageWidth, pageHeight, paint);
//
//        paint.setColor(Color.parseColor("#757575"));
//        paint.setTextSize(8);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
//
//        String dateStr = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(new Date());
//        canvas.drawText("Generated on: " + dateStr, margin, pageHeight - 20, paint);
//
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//        canvas.drawText("© MyKhata Pro - Confidential Document", pageWidth - margin - 200, pageHeight - 20, paint);
//    }
//
//    private void savePdfAndShare(PdfDocument pdfDocument, String fileName) {
//        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MyKhataReports");
//        if (!dir.exists()) dir.mkdirs();
//
//        File file = new File(dir, fileName);
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(file));
//            pdfDocument.close();
//
//            Uri uri = FileProvider.getUriForFile(
//                    requireContext(),
//                    "com.sandhyasofttechh.mykhatapro.fileprovider",  // EXACT MATCH
//                    file
//            );
//
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri, "application/pdf");
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            startActivity(Intent.createChooser(intent, "Open PDF"));
//            Toast.makeText(requireContext(), "PDF Opened Successfully!", Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//    // ===================== INNER CLASSES =====================
//
//    public static class CustomerReport {
//        private final String name, phone;
//        private final double balance;
//        private final List<Transaction> transactions;
//        private final double totalGave;
//        private final double totalReceived;
//
//        public CustomerReport(String n, String p, double b, List<Transaction> t, double gave, double received) {
//            name = n;
//            phone = p;
//            balance = b;
//            transactions = t;
//            totalGave = gave;
//            totalReceived = received;
//        }
//
//        public String getName() { return name; }
//        public String getPhone() { return phone; }
//        public double getBalance() { return balance; }
//        public List<Transaction> getTransactions() { return transactions; }
//        public double getTotalGave() { return totalGave; }
//        public double getTotalReceived() { return totalReceived; }
//    }
//
//    public static class Transaction {
//        public final String date, type, note;
//        public final double amount;
//
//        public Transaction(String d, String t, double a, String n) {
//            date = d;
//            type = t;
//            amount = a;
//            note = n;
//        }
//    }
//}



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
        if (adapter != null) adapter.notifyDataSetChanged();
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
                    // still count as loaded to avoid hanging
                    loadedCount++;
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
                if (type == null) type = "";

                // ---- FIX: Expecting exactly "gave" and "got" (case-insensitive) ----
                if (type.equalsIgnoreCase("gave")) {
                    gave += amt;
                } else if (type.equalsIgnoreCase("got")) {
                    received += amt;
                }

                txns.add(new Transaction(date, type, amt, note));
            }

            double balance = received - gave;
            customerReports.add(new CustomerReport(name, phone, balance, txns, gave, received));
            Log.d(TAG, "Loaded: " + name + " | Balance: ₹" + balance + " | Gave: " + gave + " | Received: " + received);

            loadedCount++;
            if (loadedCount == totalCustomers) {
                updateAdapter();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load transactions for " + phone, e);
            customerReports.add(new CustomerReport(name, phone, 0.0, new ArrayList<>(), 0.0, 0.0));
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

    // ===================== PROFESSIONAL PDF GENERATION =====================

    private void generateSingleCustomerPdf(CustomerReport cr) {
        PdfDocument pdfDocument = new PdfDocument();

        // Calculate pages needed based on transactions
        int maxTransactionsPerPage = 18;
        int totalPages = Math.max(1, (int) Math.ceil((double) cr.getTransactions().size() / maxTransactionsPerPage));

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNum + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int startIdx = pageNum * maxTransactionsPerPage;
            int endIdx = Math.min(startIdx + maxTransactionsPerPage, cr.getTransactions().size());
            List<Transaction> pageTransactions = cr.getTransactions().subList(startIdx, endIdx);

            drawPdfContent(canvas, cr, pageTransactions, pageNum + 1, totalPages);
            pdfDocument.finishPage(page);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        savePdfAndShare(pdfDocument, "CustomerReport_" + cr.getPhone() + "_" + timestamp + ".pdf");
    }

    private void exportAllCustomersPdf() {
        if (customerReports.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        int globalPageNumber = 1;

        for (CustomerReport cr : customerReports) {
            int maxTransactionsPerPage = 18;
            int pagesForCustomer = Math.max(1, (int) Math.ceil((double) cr.getTransactions().size() / maxTransactionsPerPage));

            for (int pageNum = 0; pageNum < pagesForCustomer; pageNum++) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, globalPageNumber++).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                int startIdx = pageNum * maxTransactionsPerPage;
                int endIdx = Math.min(startIdx + maxTransactionsPerPage, cr.getTransactions().size());
                List<Transaction> pageTransactions = cr.getTransactions().subList(startIdx, endIdx);

                drawPdfContent(canvas, cr, pageTransactions, pageNum + 1, pagesForCustomer);
                pdfDocument.finishPage(page);
            }
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        savePdfAndShare(pdfDocument, "AllCustomersReport_" + timestamp + ".pdf");
    }

    private void drawPdfContent(Canvas canvas, CustomerReport cr, List<Transaction> transactions,
                                int currentPage, int totalPages) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float pageWidth = 595f;
        float pageHeight = 842f;
        float margin = 40f;
        float contentWidth = pageWidth - (2 * margin);

        // ===== HEADER SECTION =====
        float yPos = margin;

        // Header background with gradient effect
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#1A237E")); // Deep blue
        canvas.drawRect(0, 0, pageWidth, 100, paint);

        // Accent bar
        paint.setColor(Color.parseColor("#FFC107")); // Amber
        canvas.drawRect(0, 95, pageWidth, 100, paint);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("CUSTOMER TRANSACTION REPORT", margin, 45, paint);

        // Report type indicator
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Detailed Statement", margin, 70, paint);

        // Page number
        String pageInfo = "Page " + currentPage + " of " + totalPages;
        float pageInfoWidth = paint.measureText(pageInfo);
        canvas.drawText(pageInfo, pageWidth - margin - pageInfoWidth, 70, paint);

        yPos = 120;

        // ===== CUSTOMER INFO CARD (Only on first page) =====
        if (currentPage == 1) {
            // Card background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#F8F9FA"));
            canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 110, 8, 8, paint);

            // Card border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 110, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            // Customer details
            float infoY = yPos + 25;
            paint.setColor(Color.parseColor("#424242"));
            paint.setTextSize(11);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Customer Name:", margin + 15, infoY, paint);

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(12);
            canvas.drawText(cr.getName(), margin + 130, infoY, paint);

            infoY += 22;
            paint.setTextSize(11);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Phone Number:", margin + 15, infoY, paint);

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(12);
            canvas.drawText(cr.getPhone(), margin + 130, infoY, paint);

            infoY += 22;
            paint.setTextSize(11);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Total Transactions:", margin + 15, infoY, paint);

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(12);
            canvas.drawText(String.valueOf(cr.getTransactions().size()), margin + 130, infoY, paint);

            // Financial summary box (right side)
            float summaryX = pageWidth - margin - 180;

            // Total Gave
            paint.setTextSize(9);
            paint.setColor(Color.parseColor("#757575"));
            canvas.drawText("Total Given:", summaryX, yPos + 25, paint);
            paint.setTextSize(11);
            paint.setColor(Color.parseColor("#C62828"));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(String.format("₹%.2f", cr.getTotalGave()), summaryX, yPos + 42, paint);

            // Total Received
            paint.setTextSize(9);
            paint.setColor(Color.parseColor("#757575"));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText("Total Received:", summaryX, yPos + 60, paint);
            paint.setTextSize(11);
            paint.setColor(Color.parseColor("#2E7D32"));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(String.format("₹%.2f", cr.getTotalReceived()), summaryX, yPos + 77, paint);

            // Net Balance (highlighted)
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(cr.getBalance() >= 0 ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFEBEE"));
            canvas.drawRoundRect(summaryX - 10, yPos + 85, summaryX + 170, yPos + 107, 5, 5, paint);

            paint.setTextSize(10);
            paint.setColor(Color.parseColor("#424242"));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Net Balance:", summaryX, yPos + 100, paint);

            paint.setTextSize(14);
            paint.setColor(cr.getBalance() >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
            String balanceText = String.format("₹%.2f", Math.abs(cr.getBalance()));
            if (cr.getBalance() < 0) balanceText += " DR";
            canvas.drawText(balanceText, summaryX + 80, yPos + 100, paint);

            yPos += 125;
        }

        // ===== TRANSACTIONS TABLE =====
        float tableTop = yPos;
        float rowHeight = 32f;

        // Column configuration
        float col1Width = 45f;   // S.No
        float col2Width = 85f;   // Date
        float col3Width = 75f;   // Type
        float col4Width = 85f;   // Amount
        float col5Width = contentWidth - col1Width - col2Width - col3Width - col4Width; // Note

        // Table header background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#37474F"));
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paint);

        // Header text
        paint.setColor(Color.WHITE);
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float textY = yPos + 20;
        float currentX = margin;
        canvas.drawText("S.No", currentX + 8, textY, paint);
        currentX += col1Width;
        canvas.drawText("Date", currentX + 8, textY, paint);
        currentX += col2Width;
        canvas.drawText("Type", currentX + 8, textY, paint);
        currentX += col3Width;
        canvas.drawText("Amount", currentX + 8, textY, paint);
        currentX += col4Width;
        canvas.drawText("Description", currentX + 8, textY, paint);

        yPos += rowHeight;

        // ===== TRANSACTION ROWS =====
        paint.setTextSize(9);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        int startSerialNo = (currentPage - 1) * 18 + 1;
        int rowNum = 0;

        for (Transaction t : transactions) {
            // Alternate row background
            if (rowNum % 2 == 0) {
                paint.setColor(Color.parseColor("#FAFAFA"));
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paint);
            }

            textY = yPos + 20;
            currentX = margin;

            // Serial number
            paint.setColor(Color.parseColor("#616161"));
            canvas.drawText(String.valueOf(startSerialNo + rowNum), currentX + 8, textY, paint);
            currentX += col1Width;

            // Date
            paint.setColor(Color.parseColor("#424242"));
            String dateStr = (t.date != null && !t.date.isEmpty()) ? t.date : "N/A";
            canvas.drawText(dateStr, currentX + 8, textY, paint);
            currentX += col2Width;

            // Type with badge
            String typeLower = (t.type != null) ? t.type.toLowerCase() : "";
            if ("gave".equals(typeLower)) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#FFEBEE"));
                canvas.drawRoundRect(currentX + 5, yPos + 8, currentX + 65, yPos + 24, 3, 3, paint);
                paint.setColor(Color.parseColor("#C62828"));
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("GAVE", currentX + 15, textY, paint);
            } else if ("got".equals(typeLower)) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#E8F5E9"));
                canvas.drawRoundRect(currentX + 5, yPos + 8, currentX + 75, yPos + 24, 3, 3, paint);
                paint.setColor(Color.parseColor("#2E7D32"));
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("RECEIVED", currentX + 10, textY, paint);
            } else {
                paint.setColor(Color.parseColor("#757575"));
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                canvas.drawText((t.type != null ? t.type.toUpperCase() : "N/A"), currentX + 8, textY, paint);
            }
            currentX += col3Width;

            // Amount
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            if ("gave".equals(typeLower)) {
                paint.setColor(Color.parseColor("#C62828"));
            } else if ("got".equals(typeLower)) {
                paint.setColor(Color.parseColor("#2E7D32"));
            } else {
                paint.setColor(Color.parseColor("#424242"));
            }
            canvas.drawText(String.format("₹%.2f", t.amount), currentX + 8, textY, paint);
            currentX += col4Width;

            // Note (truncated if too long)
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setColor(Color.parseColor("#616161"));
            String note = (t.note != null && !t.note.isEmpty()) ? t.note : "-";
            if (note.length() > 30) {
                note = note.substring(0, 27) + "...";
            }
            canvas.drawText(note, currentX + 8, textY, paint);

            // Row separator
            paint.setColor(Color.parseColor("#E0E0E0"));
            paint.setStrokeWidth(0.5f);
            canvas.drawLine(margin, yPos + rowHeight, pageWidth - margin, yPos + rowHeight, paint);

            yPos += rowHeight;
            rowNum++;
        }

        // ===== TABLE BORDER =====
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.parseColor("#37474F"));
        canvas.drawRect(margin, tableTop, pageWidth - margin, yPos, paint);

        // Vertical grid lines
        paint.setStrokeWidth(1f);
        paint.setColor(Color.parseColor("#BDBDBD"));
        currentX = margin + col1Width;
        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
        currentX += col2Width;
        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
        currentX += col3Width;
        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);
        currentX += col4Width;
        canvas.drawLine(currentX, tableTop, currentX, yPos, paint);

        // ===== FOOTER =====
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(0, pageHeight - 50, pageWidth, pageHeight, paint);

        paint.setColor(Color.parseColor("#757575"));
        paint.setTextSize(8);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

        String dateStr = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(new Date());
        canvas.drawText("Generated on: " + dateStr, margin, pageHeight - 20, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("© MyKhata Pro - Confidential Document", pageWidth - margin - 200, pageHeight - 20, paint);
    }

    private void savePdfAndShare(PdfDocument pdfDocument, String fileName) {
        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MyKhataReports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.sandhyasofttechh.mykhatapro.fileprovider",  // EXACT MATCH
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(Intent.createChooser(intent, "Open PDF"));
            Toast.makeText(requireContext(), "PDF Opened Successfully!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    // ===================== INNER CLASSES =====================

    public static class CustomerReport {
        private final String name, phone;
        private final double balance;
        private final List<Transaction> transactions;
        private final double totalGave;
        private final double totalReceived;

        public CustomerReport(String n, String p, double b, List<Transaction> t, double gave, double received) {
            name = n;
            phone = p;
            balance = b;
            transactions = t;
            totalGave = gave;
            totalReceived = received;
        }

        public String getName() { return name; }
        public String getPhone() { return phone; }
        public double getBalance() { return balance; }
        public List<Transaction> getTransactions() { return transactions; }
        public double getTotalGave() { return totalGave; }
        public double getTotalReceived() { return totalReceived; }
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
