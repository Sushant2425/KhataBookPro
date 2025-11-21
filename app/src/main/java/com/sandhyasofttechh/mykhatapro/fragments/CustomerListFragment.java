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
//import com.sandhyasofttechh.mykhatapro.adapter.CustomerBalanceAdapter;
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
//public class CustomerListFragment extends Fragment {
//
//    private static final String TAG = "CustomerListFragment";
//
//    private RecyclerView rvCustomerList;
//    private FloatingActionButton fabExportAll;
//    private SearchView searchView;
//    private DatabaseReference customerRef, transRef;
//
//    private List<CustomerSummary> customerSummaries = new ArrayList<>();
//    private List<CustomerSummary> filteredSummaries = new ArrayList<>();
//    private CustomerBalanceAdapter adapter;
//
//    private int totalCustomers = 0;
//    private int loadedCount = 0;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_customer_list, container, false);
//
//        rvCustomerList = v.findViewById(R.id.rvCustomerList);
//        fabExportAll = v.findViewById(R.id.fabExportAll);
//        searchView = v.findViewById(R.id.searchView);
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
//        customerRef = FirebaseDatabase.getInstance().getReference(basePath + "/customers");
//        transRef = FirebaseDatabase.getInstance().getReference(basePath + "/transactions");
//
//        fabExportAll.setOnClickListener(view -> exportCustomerListPdf());
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
//            filteredSummaries.clear();
//            filteredSummaries.addAll(customerSummaries);
//        } else {
//            filteredSummaries.clear();
//            String lowerQuery = query.toLowerCase();
//            for (CustomerSummary cs : customerSummaries) {
//                if (cs.name.toLowerCase().contains(lowerQuery)
//                        || cs.phone.toLowerCase().contains(lowerQuery)) {
//                    filteredSummaries.add(cs);
//                }
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }
//
//    private void loadCustomersWithBalances() {
//        customerRef.get().addOnSuccessListener(customerSnap -> {
//            customerSummaries.clear();
//            filteredSummaries.clear();
//            totalCustomers = (int) customerSnap.getChildrenCount();
//            loadedCount = 0;
//
//            if (totalCustomers == 0) {
//                Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show();
//                updateAdapter();
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
//                fetchBalanceForCustomer(name, phone);
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(requireContext(), "Failed to load customers: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            updateAdapter();
//        });
//    }
//
//    private void fetchBalanceForCustomer(String name, String phone) {
//        transRef.child(phone).get().addOnSuccessListener(tsnap -> {
//            double gave = 0, received = 0;
//
//            for (DataSnapshot t : tsnap.getChildren()) {
//                Double amt = t.child("amount").getValue(Double.class);
//                String type = t.child("type").getValue(String.class);
//
//                if (amt == null) amt = 0.0;
//                if ("gave".equalsIgnoreCase(type)) gave += amt;
//                else if ("received".equalsIgnoreCase(type)) received += amt;
//            }
//
//            double balance = received - gave;
//            customerSummaries.add(new CustomerSummary(name, phone, balance));
//            loadedCount++;
//
//            if (loadedCount == totalCustomers)
//                updateAdapter();
//        }).addOnFailureListener(e -> {
//            customerSummaries.add(new CustomerSummary(name, phone, 0.0));
//            loadedCount++;
//            if (loadedCount == totalCustomers)
//                updateAdapter();
//        });
//    }
//
//    private void updateAdapter() {
//        filteredSummaries.clear();
//        filteredSummaries.addAll(customerSummaries);
//        adapter = new CustomerBalanceAdapter(filteredSummaries);
//        rvCustomerList.setAdapter(adapter);
//    }
//
//    // ========== PDF GENERATION ==========
//
//    private void exportCustomerListPdf() {
//        if (customerSummaries.isEmpty()) {
//            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        PdfDocument pdfDocument = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
//        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//
//        drawCustomerListPdf(canvas);
//
//        pdfDocument.finishPage(page);
//        savePdfAndShare(pdfDocument, "Customer_List_Report.pdf");
//    }
//
//    private void drawCustomerListPdf(Canvas canvas) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//
//        float startX = 50f;
//        float yPos = 60f;
//        float tableWidth = 495f;
//        float rowHeight = 30f;
//
//        // Column widths
//        float colNameWidth = 200f;
//        float colPhoneWidth = 150f;
//        float colBalanceWidth = tableWidth - (colNameWidth + colPhoneWidth);
//
//        // ===== HEADER =====
//        paint.setColor(Color.parseColor("#2C3E50"));
//        paint.setTextSize(24);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        canvas.drawText("Customer List Report", startX, yPos, paint);
//        yPos += 10;
//
//        paint.setStrokeWidth(2);
//        paint.setColor(Color.DKGRAY);
//        canvas.drawLine(startX, yPos, startX + tableWidth, yPos, paint);
//        yPos += 40;
//
//        // ===== TABLE HEADER =====
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.parseColor("#34495E"));
//        canvas.drawRect(startX, yPos, startX + tableWidth, yPos + rowHeight, paint);
//
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(12);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//
//        float currentX = startX;
//        float textPadding = 8f;
//        canvas.drawText("Customer Name", currentX + textPadding, yPos + 20, paint);
//        currentX += colNameWidth;
//        canvas.drawText("Phone Number", currentX + textPadding, yPos + 20, paint);
//        currentX += colPhoneWidth;
//        canvas.drawText("Balance", currentX + textPadding, yPos + 20, paint);
//
//        yPos += rowHeight;
//
//        // ===== DRAW GRID LINES =====
//        paint.setColor(Color.parseColor("#FF888888"));
//        paint.setStrokeWidth(1.5f);
//
//        int rowCount = customerSummaries.size();
//        float tableBottom = yPos + rowHeight * rowCount;
//
//        // Vertical lines
//        float x1 = startX;
//        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);
//
//        x1 += colNameWidth;
//        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);
//
//        x1 += colPhoneWidth;
//        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);
//
//        x1 = startX + tableWidth;
//        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);
//
//        // Horizontal lines
//        float yLine = yPos - rowHeight;
//        for (int i = 0; i <= rowCount; i++) {
//            canvas.drawLine(startX, yLine, startX + tableWidth, yLine, paint);
//            yLine += rowHeight;
//        }
//
//        // ===== DRAW TABLE ROWS =====
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(11);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//
//        float textY;
//        int row = 0;
//        for (CustomerSummary cs : customerSummaries) {
//            textY = yPos + row * rowHeight + 20;
//
//            // Alternate row background
//            if ((row % 2) == 0) {
//                paint.setStyle(Paint.Style.FILL);
//                paint.setColor(Color.parseColor("#ECF0F1"));
//                canvas.drawRect(startX, yPos + row * rowHeight, startX + tableWidth, yPos + (row + 1) * rowHeight, paint);
//                paint.setColor(Color.BLACK);
//            }
//
//            currentX = startX;
//            canvas.drawText(cs.name, currentX + textPadding, textY, paint);
//            currentX += colNameWidth;
//
//            canvas.drawText(cs.phone, currentX + textPadding, textY, paint);
//            currentX += colPhoneWidth;
//
//            paint.setColor(cs.balance < 0 ? Color.RED : Color.parseColor("#27AE60"));
//            canvas.drawText(String.format("₹%.2f", cs.balance), currentX + textPadding, textY, paint);
//            paint.setColor(Color.BLACK);
//
//            row++;
//
//            if (textY > 780) break;
//        }
//
//        // ===== OUTER BORDER =====
//        paint.setColor(Color.parseColor("#FF888888"));
//        paint.setStrokeWidth(2f);
//        paint.setStyle(Paint.Style.STROKE);
//        canvas.drawRect(startX, yPos - rowHeight, startX + tableWidth, yPos + rowHeight * rowCount, paint);
//
//        paint.setStyle(Paint.Style.FILL);
//
//        // ===== FOOTER =====
//        paint.setColor(Color.GRAY);
//        paint.setTextSize(10);
//        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
//        String footer = "Generated on: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(new Date());
//        canvas.drawText(footer, startX, 810, paint);
//        canvas.drawText("Powered by MyKhata Pro", startX + 350f, 810, paint);
//    }
//
//    private void savePdfAndShare(PdfDocument pdfDocument, String fileName) {
//        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports");
//        if (!dir.exists()) dir.mkdirs();
//
//        File file = new File(dir, fileName);
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(file));
//            pdfDocument.close();
//
//            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri, "application/pdf");
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(Intent.createChooser(intent, "Open PDF"));
//
//            Toast.makeText(requireContext(), "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            Log.e(TAG, "Error saving PDF", e);
//            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // ========== INNER CLASS ==========
//
//    public static class CustomerSummary {
//        public final String name;
//        public final String phone;
//        public final double balance;
//
//        public CustomerSummary(String name, String phone, double balance) {
//            this.name = name;
//            this.phone = phone;
//            this.balance = balance;
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
import com.sandhyasofttechh.mykhatapro.adapter.CustomerBalanceAdapter;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerListFragment extends Fragment {

    private static final String TAG = "CustomerListFragment";

    private RecyclerView rvCustomerList;
    private FloatingActionButton fabExportAll;
    private SearchView searchView;
    private DatabaseReference customerRef, transRef;

    private List<CustomerSummary> customerSummaries = new ArrayList<>();
    private List<CustomerSummary> filteredSummaries = new ArrayList<>();
    private CustomerBalanceAdapter adapter;

    private int totalCustomers = 0;
    private int loadedCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_list, container, false);

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
        customerRef = FirebaseDatabase.getInstance().getReference(basePath + "/customers");
        transRef = FirebaseDatabase.getInstance().getReference(basePath + "/transactions");

        fabExportAll.setOnClickListener(view -> exportCustomerListPdf());

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
            filteredSummaries.clear();
            filteredSummaries.addAll(customerSummaries);
        } else {
            filteredSummaries.clear();
            String lowerQuery = query.toLowerCase();
            for (CustomerSummary cs : customerSummaries) {
                if (cs.name.toLowerCase().contains(lowerQuery)
                        || cs.phone.toLowerCase().contains(lowerQuery)) {
                    filteredSummaries.add(cs);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadCustomersWithBalances() {
        customerRef.get().addOnSuccessListener(customerSnap -> {
            customerSummaries.clear();
            filteredSummaries.clear();
            totalCustomers = (int) customerSnap.getChildrenCount();
            loadedCount = 0;

            if (totalCustomers == 0) {
                Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show();
                updateAdapter();
                return;
            }

            for (DataSnapshot c : customerSnap.getChildren()) {
                String phone = c.child("phone").getValue(String.class);
                String name = c.child("name").getValue(String.class);

                if (phone == null || name == null) {
                    Log.w(TAG, "Invalid customer data: " + c.getKey());
                    continue;
                }

                fetchBalanceForCustomer(name, phone);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load customers: " + e.getMessage(), Toast.LENGTH_LONG).show();
            updateAdapter();
        });
    }

    private void fetchBalanceForCustomer(String name, String phone) {
        transRef.child(phone).get().addOnSuccessListener(tsnap -> {
            double gave = 0, received = 0;

            for (DataSnapshot t : tsnap.getChildren()) {
                Double amt = t.child("amount").getValue(Double.class);
                String type = t.child("type").getValue(String.class);

                if (amt == null) amt = 0.0;
                if ("gave".equalsIgnoreCase(type)) gave += amt;
                else if ("received".equalsIgnoreCase(type)) received += amt;
            }

            double balance = received - gave;
            customerSummaries.add(new CustomerSummary(name, phone, balance));
            loadedCount++;

            if (loadedCount == totalCustomers)
                updateAdapter();
        }).addOnFailureListener(e -> {
            customerSummaries.add(new CustomerSummary(name, phone, 0.0));
            loadedCount++;
            if (loadedCount == totalCustomers)
                updateAdapter();
        });
    }

    private void updateAdapter() {
        filteredSummaries.clear();
        filteredSummaries.addAll(customerSummaries);
        adapter = new CustomerBalanceAdapter(filteredSummaries);
        rvCustomerList.setAdapter(adapter);
    }

    // ========== PROFESSIONAL PDF GENERATION ==========

    private void exportCustomerListPdf() {
        if (customerSummaries.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate required pages
        int maxRowsPerPage = 20;
        int totalPages = (int) Math.ceil((double) customerSummaries.size() / maxRowsPerPage);

        PdfDocument pdfDocument = new PdfDocument();

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNum + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int startIdx = pageNum * maxRowsPerPage;
            int endIdx = Math.min(startIdx + maxRowsPerPage, customerSummaries.size());
            List<CustomerSummary> pageData = customerSummaries.subList(startIdx, endIdx);

            drawCustomerListPdf(canvas, pageData, pageNum + 1, totalPages);
            pdfDocument.finishPage(page);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        savePdfAndShare(pdfDocument, "CustomerList_" + timestamp + ".pdf");
    }

    private void drawCustomerListPdf(Canvas canvas, List<CustomerSummary> data, int currentPage, int totalPages) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float pageWidth = 595f;
        float pageHeight = 842f;
        float margin = 40f;
        float contentWidth = pageWidth - (2 * margin);

        // ===== HEADER SECTION WITH GRADIENT EFFECT =====
        float yPos = margin;

        // Header background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#1A237E")); // Deep blue
        canvas.drawRect(0, 0, pageWidth, 120, paint);

        // Accent bar
        paint.setColor(Color.parseColor("#FFC107")); // Amber
        canvas.drawRect(0, 115, pageWidth, 120, paint);

        // Company name/logo area
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("MyKhata Pro", margin, 50, paint);

        // Report title
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Customer Balance Report", margin, 80, paint);

        // Date and page info
        paint.setTextSize(10);
        String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date());
        canvas.drawText("Generated: " + dateStr, margin, 100, paint);

        String pageInfo = "Page " + currentPage + " of " + totalPages;
        float pageInfoWidth = paint.measureText(pageInfo);
        canvas.drawText(pageInfo, pageWidth - margin - pageInfoWidth, 100, paint);

        yPos = 140;

        // ===== SUMMARY STATISTICS BOX =====
        if (currentPage == 1) {
            double totalBalance = 0;
            int positiveCount = 0;
            int negativeCount = 0;

            for (CustomerSummary cs : customerSummaries) {
                totalBalance += cs.balance;
                if (cs.balance > 0) positiveCount++;
                else if (cs.balance < 0) negativeCount++;
            }

            // Stats box background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#F5F5F5"));
            canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 70, 8, 8, paint);

            // Stats box border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 70, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            // Draw statistics
            paint.setColor(Color.parseColor("#424242"));
            paint.setTextSize(11);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            float statY = yPos + 25;
            canvas.drawText("Total Customers:", margin + 15, statY, paint);
            canvas.drawText("To Receive:", margin + 15, statY + 20, paint);
            canvas.drawText("To Pay:", margin + 15, statY + 40, paint);

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(String.valueOf(customerSummaries.size()), margin + 140, statY, paint);

            paint.setColor(Color.parseColor("#2E7D32")); // Green
            canvas.drawText(positiveCount + " customers", margin + 140, statY + 20, paint);

            paint.setColor(Color.parseColor("#C62828")); // Red
            canvas.drawText(negativeCount + " customers", margin + 140, statY + 40, paint);

            // Net balance
            paint.setTextSize(13);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Net Balance:", pageWidth - margin - 200, statY + 10, paint);

            paint.setColor(totalBalance >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
            paint.setTextSize(16);
            canvas.drawText(String.format("₹%.2f", totalBalance), pageWidth - margin - 200, statY + 35, paint);

            yPos += 85;
        }

        // ===== TABLE HEADER =====
        float tableTop = yPos;
        float rowHeight = 35f;

        // Column widths
        float col1Width = 40f;  // S.No
        float col2Width = 200f; // Name
        float col3Width = 120f; // Phone
        float col4Width = contentWidth - col1Width - col2Width - col3Width; // Balance

        // Table header background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#37474F")); // Blue grey
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paint);

        // Header text
        paint.setColor(Color.WHITE);
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float textY = yPos + 22;
        canvas.drawText("S.No", margin + 10, textY, paint);
        canvas.drawText("Customer Name", margin + col1Width + 10, textY, paint);
        canvas.drawText("Phone Number", margin + col1Width + col2Width + 10, textY, paint);
        canvas.drawText("Balance", margin + col1Width + col2Width + col3Width + 10, textY, paint);

        yPos += rowHeight;

        // ===== TABLE ROWS =====
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        int rowNum = (currentPage - 1) * 20 + 1;
        for (CustomerSummary cs : data) {
            // Alternate row colors
            if (rowNum % 2 == 0) {
                paint.setColor(Color.parseColor("#FAFAFA"));
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, paint);
            }

            textY = yPos + 22;

            // S.No
            paint.setColor(Color.parseColor("#616161"));
            canvas.drawText(String.valueOf(rowNum), margin + 10, textY, paint);

            // Customer name
            paint.setColor(Color.parseColor("#212121"));
            String displayName = cs.name.length() > 25 ? cs.name.substring(0, 22) + "..." : cs.name;
            canvas.drawText(displayName, margin + col1Width + 10, textY, paint);

            // Phone number
            paint.setColor(Color.parseColor("#424242"));
            canvas.drawText(cs.phone, margin + col1Width + col2Width + 10, textY, paint);

            // Balance with color coding
            String balanceText;
            if (cs.balance > 0) {
                paint.setColor(Color.parseColor("#2E7D32")); // Green
                balanceText = "₹" + String.format("%.2f", cs.balance) + " ↑";
            } else if (cs.balance < 0) {
                paint.setColor(Color.parseColor("#C62828")); // Red
                balanceText = "₹" + String.format("%.2f", Math.abs(cs.balance)) + " ↓";
            } else {
                paint.setColor(Color.parseColor("#757575")); // Grey
                balanceText = "₹0.00";
            }

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(balanceText, margin + col1Width + col2Width + col3Width + 10, textY, paint);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

            // Row separator line
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

        // Vertical lines
        paint.setStrokeWidth(1f);
        paint.setColor(Color.parseColor("#BDBDBD"));
        canvas.drawLine(margin + col1Width, tableTop, margin + col1Width, yPos, paint);
        canvas.drawLine(margin + col1Width + col2Width, tableTop, margin + col1Width + col2Width, yPos, paint);
        canvas.drawLine(margin + col1Width + col2Width + col3Width, tableTop, margin + col1Width + col2Width + col3Width, yPos, paint);

        // ===== FOOTER =====
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRect(0, pageHeight - 50, pageWidth, pageHeight, paint);

        paint.setColor(Color.parseColor("#757575"));
        paint.setTextSize(8);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

        String footer = "This is a computer-generated report. • Confidential Document";
        canvas.drawText(footer, margin, pageHeight - 20, paint);

        canvas.drawText("© MyKhata Pro - All Rights Reserved", pageWidth - margin - 180, pageHeight - 20, paint);
    }

    private void savePdfAndShare(PdfDocument pdfDocument, String fileName) {
        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MyKhataReports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(Intent.createChooser(intent, "View Customer Report"));

            Toast.makeText(requireContext(), "PDF generated successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error saving PDF", e);
            Toast.makeText(requireContext(), "Failed to generate PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ========== INNER CLASS ==========

    public static class CustomerSummary {
        public final String name;
        public final String phone;
        public final double balance;

        public CustomerSummary(String name, String phone, double balance) {
            this.name = name;
            this.phone = phone;
            this.balance = balance;
        }
    }
}
