package com.sandhyasofttechh.mykhatapro.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CollectionAdapter;
import com.sandhyasofttechh.mykhatapro.model.CollectionModel;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CollectionActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView txtTotalDue, txtEmptyState, txtShopTitle;

    ArrayList<CollectionModel> list = new ArrayList<>();
    CollectionAdapter adapter;

    PrefManager prefManager;
    DatabaseReference rootRef;
    String userEmailPath;
    String shopId;
    String shopName;
    HashSet<String> processedPhones = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        initViews();
        initData();
        setupRecyclerView();
        loadCollectionData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCollection);
        progressBar = findViewById(R.id.progressBarCollection);
        txtTotalDue = findViewById(R.id.txtTotalDue);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        txtShopTitle = findViewById(R.id.txtShopTitle);
    }

    private void initData() {
        prefManager = new PrefManager(this);
        userEmailPath = prefManager.getUserEmail().replace(".", ",");
        shopId = prefManager.getCurrentShopId();
        shopName = prefManager.getCurrentShopName();

        // 🔥 SHOW CURRENT CONTEXT
        if (shopId.isEmpty() || shopId.equals("")) {
            txtShopTitle.setText("Default Account");
            Toast.makeText(this, "Using Default Account", Toast.LENGTH_SHORT).show();
        } else {
            txtShopTitle.setText(shopName.isEmpty() ? "Shop: " + shopId : shopName);
            Toast.makeText(this, "Using Shop: " + shopId, Toast.LENGTH_SHORT).show();
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CollectionAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    private void loadCollectionData() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        list.clear();
        adapter.notifyDataSetChanged();

        if (userEmailPath.isEmpty()) {
            showError("Please login first");
            return;
        }

        // 🔥 STEP 1: Load CUSTOMERS (Shop first, then ROOT fallback)
        DatabaseReference customersRef;
        if (!shopId.isEmpty() && !shopId.equals("")) {
            customersRef = rootRef.child("Khatabook").child(userEmailPath)
                    .child("shops").child(shopId).child("customers");
        } else {
            customersRef = rootRef.child("Khatabook").child(userEmailPath).child("customers");
        }

        customersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot customerSnap) {
                if (!customerSnap.exists() || customerSnap.getChildrenCount() == 0) {
                    // Fallback to ROOT customers
                    loadRootCustomers();
                    return;
                }

                HashMap<String, String> customerNames = new HashMap<>();
                ArrayList<String> customerPhones = new ArrayList<>();

                for (DataSnapshot cust : customerSnap.getChildren()) {
                    String phone = cust.getKey();
                    String name = cust.child("name").getValue(String.class);
                    if (name == null) name = phone;

                    customerNames.put(phone, name);
                    customerPhones.add(phone);
                }

                // 🔥 STEP 2: Load TRANSACTIONS for these customers
                fetchAllTransactions(customerNames, customerPhones);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                loadRootCustomers();
            }
        });
    }

    private void loadRootCustomers() {
        DatabaseReference rootCustomersRef = rootRef.child("Khatabook").child(userEmailPath).child("customers");
        rootCustomersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot customerSnap) {
                if (!customerSnap.exists()) {
                    showEmptyState("No customers found");
                    return;
                }

                HashMap<String, String> customerNames = new HashMap<>();
                ArrayList<String> customerPhones = new ArrayList<>();

                for (DataSnapshot cust : customerSnap.getChildren()) {
                    String phone = cust.getKey();
                    String name = cust.child("name").getValue(String.class);
                    if (name == null) name = phone;

                    customerNames.put(phone, name);
                    customerPhones.add(phone);
                }

                fetchAllTransactions(customerNames, customerPhones);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showError("Error loading customers");
            }
        });
    }

    // 🔥 CRITICAL: Fetch BOTH SHOP + ROOT Transactions
    private void fetchAllTransactions(HashMap<String, String> customerNames, ArrayList<String> customerPhones) {
        double grandTotalDue = 0;
        list.clear();

        // Process each customer
        for (String phone : customerPhones) {
            double gave = 0;
            double got = 0;
            String customerName = customerNames.get(phone);

            // 🔥 1. Check SHOP TRANSACTIONS FIRST (if shop selected)
            if (!shopId.isEmpty() && !shopId.equals("")) {
                DatabaseReference shopTxnRef = rootRef.child("Khatabook")
                        .child(userEmailPath).child("shops").child(shopId)
                        .child("transactions").child(phone);

                shopTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot shopTxnSnap) {
                        processPhoneTransactions(phone, customerName, shopTxnSnap, customerPhones, customerNames, grandTotalDue);
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
            } else {
                // 🔥 2. Only ROOT transactions for default account
                processPhoneTransactions(phone, customerName, null, customerPhones, customerNames, grandTotalDue);
            }
        }
    }

    private void processPhoneTransactions(String phone, String customerName,
                                          DataSnapshot shopTxnSnap, ArrayList<String> allPhones,
                                          HashMap<String, String> allNames, double currentGrandTotal) {

        // Prevent duplicate customer processing
        if (processedPhones.contains(phone)) {
            return;
        }
        processedPhones.add(phone);

// Track duplicate txn ids
        HashSet<String> processedTxnIds = new HashSet<>();


        final double[] gave = {0};
        final double[] got = { 0 };

        // 🔥 SHOP TRANSACTIONS
        if (shopTxnSnap != null && shopTxnSnap.exists()) {
            for (DataSnapshot txn : shopTxnSnap.getChildren()) {
                String txnId = txn.getKey();
                if (processedTxnIds.contains(txnId)) continue;
                processedTxnIds.add(txnId);

                Long deletedAt = txn.child("deletedAt").getValue(Long.class);
                if (deletedAt != null && deletedAt > 0) continue;

                Double amount = getAmount(txn.child("amount"));
                String type = txn.child("type").getValue(String.class);

                if ("gave".equals(type)) gave[0] += amount;
                else if ("got".equals(type)) got[0] += amount;
            }
        }

        // 🔥 ROOT TRANSACTIONS (ALWAYS check)
        DatabaseReference rootTxnRef = rootRef.child("Khatabook")
                .child(userEmailPath).child("transactions").child(phone);

        rootTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot rootTxnSnap) {
                if (rootTxnSnap.exists()) {
                    for (DataSnapshot txn : rootTxnSnap.getChildren()) {

                        String txnId = txn.getKey();
                        if (processedTxnIds.contains(txnId)) continue;
                        processedTxnIds.add(txnId);

                        Long deletedAt = txn.child("deletedAt").getValue(Long.class);
                        if (deletedAt != null && deletedAt > 0) continue;

                        Double amount = getAmount(txn.child("amount"));
                        String type = txn.child("type").getValue(String.class);

                        if ("gave".equals(type)) gave[0] += amount;
                        else if ("got".equals(type)) got[0] += amount;
                    }
                }

                // 🔥 Calculate & Add to list
                double pending = gave[0] - got[0];
                if (pending > 0) {
                    list.add(new CollectionModel(customerName, phone, pending));
                }

                // 🔥 Check if all processing done
                if (list.size() == allPhones.size() || phone.equals(allPhones.get(allPhones.size() - 1))) {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateUI(); // Continue even if error
            }
        });
    }

    private Double getAmount(DataSnapshot amountSnap) {
        if (!amountSnap.exists()) return 0.0;
        Object amountObj = amountSnap.getValue();
        if (amountObj instanceof Number) {
            return ((Number) amountObj).doubleValue();
        }
        return 0.0;
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        double grandTotalDue = 0;
        for (CollectionModel item : list) {
            grandTotalDue += item.getPendingAmount();
        }
        txtTotalDue.setText(String.format("Total Due: ₹%.2f", grandTotalDue));

        if (list.isEmpty()) {
            showEmptyState("No pending collections");
        } else {
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        txtEmptyState.setText(message);
        txtEmptyState.setVisibility(View.VISIBLE);
        txtTotalDue.setText("Total Due: ₹0.00");
    }

    private void showEmptyState(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        txtEmptyState.setText(message);
        txtEmptyState.setVisibility(View.VISIBLE);
        txtTotalDue.setText("Total Due: ₹0.00");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCollectionData();
    }
}
