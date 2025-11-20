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

    // ========== PDF GENERATION ==========

    private void exportCustomerListPdf() {
        if (customerSummaries.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        drawCustomerListPdf(canvas);

        pdfDocument.finishPage(page);
        savePdfAndShare(pdfDocument, "Customer_List_Report.pdf");
    }

    private void drawCustomerListPdf(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float startX = 50f;
        float yPos = 60f;
        float tableWidth = 495f;
        float rowHeight = 30f;

        // Column widths
        float colNameWidth = 200f;
        float colPhoneWidth = 150f;
        float colBalanceWidth = tableWidth - (colNameWidth + colPhoneWidth);

        // ===== HEADER =====
        paint.setColor(Color.parseColor("#2C3E50"));
        paint.setTextSize(24);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Customer List Report", startX, yPos, paint);
        yPos += 10;

        paint.setStrokeWidth(2);
        paint.setColor(Color.DKGRAY);
        canvas.drawLine(startX, yPos, startX + tableWidth, yPos, paint);
        yPos += 40;

        // ===== TABLE HEADER =====
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#34495E"));
        canvas.drawRect(startX, yPos, startX + tableWidth, yPos + rowHeight, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float currentX = startX;
        float textPadding = 8f;
        canvas.drawText("Customer Name", currentX + textPadding, yPos + 20, paint);
        currentX += colNameWidth;
        canvas.drawText("Phone Number", currentX + textPadding, yPos + 20, paint);
        currentX += colPhoneWidth;
        canvas.drawText("Balance", currentX + textPadding, yPos + 20, paint);

        yPos += rowHeight;

        // ===== DRAW GRID LINES =====
        paint.setColor(Color.parseColor("#FF888888"));
        paint.setStrokeWidth(1.5f);

        int rowCount = customerSummaries.size();
        float tableBottom = yPos + rowHeight * rowCount;

        // Vertical lines
        float x1 = startX;
        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);

        x1 += colNameWidth;
        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);

        x1 += colPhoneWidth;
        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);

        x1 = startX + tableWidth;
        canvas.drawLine(x1, yPos - rowHeight, x1, tableBottom, paint);

        // Horizontal lines
        float yLine = yPos - rowHeight;
        for (int i = 0; i <= rowCount; i++) {
            canvas.drawLine(startX, yLine, startX + tableWidth, yLine, paint);
            yLine += rowHeight;
        }

        // ===== DRAW TABLE ROWS =====
        paint.setColor(Color.BLACK);
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        float textY;
        int row = 0;
        for (CustomerSummary cs : customerSummaries) {
            textY = yPos + row * rowHeight + 20;

            // Alternate row background
            if ((row % 2) == 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#ECF0F1"));
                canvas.drawRect(startX, yPos + row * rowHeight, startX + tableWidth, yPos + (row + 1) * rowHeight, paint);
                paint.setColor(Color.BLACK);
            }

            currentX = startX;
            canvas.drawText(cs.name, currentX + textPadding, textY, paint);
            currentX += colNameWidth;

            canvas.drawText(cs.phone, currentX + textPadding, textY, paint);
            currentX += colPhoneWidth;

            paint.setColor(cs.balance < 0 ? Color.RED : Color.parseColor("#27AE60"));
            canvas.drawText(String.format("₹%.2f", cs.balance), currentX + textPadding, textY, paint);
            paint.setColor(Color.BLACK);

            row++;

            if (textY > 780) break;
        }

        // ===== OUTER BORDER =====
        paint.setColor(Color.parseColor("#FF888888"));
        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(startX, yPos - rowHeight, startX + tableWidth, yPos + rowHeight * rowCount, paint);

        paint.setStyle(Paint.Style.FILL);

        // ===== FOOTER =====
        paint.setColor(Color.GRAY);
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        String footer = "Generated on: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(new Date());
        canvas.drawText(footer, startX, 810, paint);
        canvas.drawText("Powered by MyKhata Pro", startX + 350f, 810, paint);
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
