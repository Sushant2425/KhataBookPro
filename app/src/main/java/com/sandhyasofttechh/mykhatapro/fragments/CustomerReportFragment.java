package com.sandhyasofttechh.mykhatapro.fragments;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CustomerReportAdapter;
import com.sandhyasofttechh.mykhatapro.utils.PdfUtils;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import com.sandhyasofttechh.mykhatapro.utils.TableDrawer;

import java.util.ArrayList;
import java.util.List;

public class CustomerReportFragment extends Fragment {

    private static final String TAG = "CustomerReportFragment";
    // Inside onCreateView()

    private RecyclerView rvCustomerList;
    private FloatingActionButton fabExportAll;
    private DatabaseReference customerRef, transRef;
    private List<CustomerReport> customerReports = new ArrayList<>();
    private CustomerReportAdapter adapter;
    private int totalCustomers = 0;
    private int loadedCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_reports, container, false);
        rvCustomerList = v.findViewById(R.id.rvCustomerList);
        fabExportAll = v.findViewById(R.id.fabExportAll);
        rvCustomerList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get logged-in user email from PrefManager
        PrefManager prefManager = new PrefManager(requireContext());
        String userEmail = prefManager.getUserEmail();

        if (userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return v;
        }

        // Build Firebase key: replace '.' → ','  (e.g. sandhyacomputer1@gmail,com)
        String emailKey = userEmail.replace(".", ",");

        String basePath = "Khatabook/" + emailKey;
        Log.d(TAG, "Fetching data from: " + basePath);

        customerRef = FirebaseDatabase.getInstance().getReference(basePath + "/customers");
        transRef = FirebaseDatabase.getInstance().getReference(basePath + "/transactions");

        fabExportAll.setOnClickListener(view -> exportAllCustomersPdf());
        loadCustomersWithBalances();

        return v;
    }

    private void loadCustomersWithBalances() {
        customerRef.get().addOnSuccessListener(customerSnap -> {
            Log.d(TAG, "Customers count: " + customerSnap.getChildrenCount());
            customerReports.clear();
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
        adapter = new CustomerReportAdapter(customerReports, this::generateAndSharePdf);
        rvCustomerList.setAdapter(adapter);
        Log.d(TAG, "Adapter updated with " + customerReports.size() + " customers");
    }

    private void showEmptyState() {
        updateAdapter(); // Show empty list
        Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show();
    }

    // === PDF EXPORT: SINGLE CUSTOMER ===
    private void generateAndSharePdf(CustomerReport cr) {
        PdfDocument doc = new PdfDocument();
        PdfUtils utils = new PdfUtils(requireContext(), doc);
        PdfUtils.Page page = utils.startPage();
        TableDrawer drawer = new TableDrawer(page.canvas, utils);
        drawer.drawHeader(cr.getName(), cr.getPhone(), String.format("₹%.2f", cr.getBalance()));
        drawer.drawTransactionTable(cr.getTransactions());
        utils.finishPage(page);
        utils.saveAndShare("Report_" + cr.getPhone() + ".pdf");
    }

    // === PDF EXPORT: ALL CUSTOMERS ===
    private void exportAllCustomersPdf() {
        if (customerReports.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }
        PdfDocument doc = new PdfDocument();
        PdfUtils utils = new PdfUtils(requireContext(), doc);
        for (CustomerReport cr : customerReports) {
            PdfUtils.Page page = utils.startPage();
            TableDrawer drawer = new TableDrawer(page.canvas, utils);
            drawer.drawHeader(cr.getName(), cr.getPhone(), String.format("₹%.2f", cr.getBalance()));
            drawer.drawTransactionTable(cr.getTransactions());
            utils.finishPage(page);
        }
        utils.saveAndShare("All_Customers_Report.pdf");
    }

    // === INNER CLASSES ===
    public static class CustomerReport {
        private final String name, phone;
        private final double balance;
        private final List<Transaction> transactions;

        public CustomerReport(String n, String p, double b, List<Transaction> t) {
            name = n; phone = p; balance = b; transactions = t;
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
            date = d; type = t; amount = a; note = n;
        }
    }
}