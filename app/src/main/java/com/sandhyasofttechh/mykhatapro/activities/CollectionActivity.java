package com.sandhyasofttechh.mykhatapro.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CollectionAdapter;
import com.sandhyasofttechh.mykhatapro.fragments.CollectionFragment;
import com.sandhyasofttechh.mykhatapro.model.CollectionModel;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class CollectionActivity extends AppCompatActivity {

    TextView txtTotalDue, txtEmptyState, txtShopTitle;
    ProgressBar progressBar;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    ArrayList<CollectionModel> duePaymentsList = new ArrayList<>();
    ArrayList<CollectionModel> todayList = new ArrayList<>();
    ArrayList<CollectionModel> incomingList = new ArrayList<>();

    PrefManager prefManager;
    DatabaseReference rootRef;
    String userEmailPath;
    String shopId;
    String shopName;
    HashSet<String> processedPhones = new HashSet<>();
    int totalCustomers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        initViews();
        initData();
        setupTabs();
        loadCollectionData();
    }

    private void initViews() {
        txtTotalDue = findViewById(R.id.txtTotalDue);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        txtShopTitle = findViewById(R.id.txtShopTitle);
        progressBar = findViewById(R.id.progressBarCollection);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void initData() {
        prefManager = new PrefManager(this);
        userEmailPath = prefManager.getUserEmail().replace(".", ",");
        shopId = prefManager.getCurrentShopId();
        shopName = prefManager.getCurrentShopName();

        if (shopId.isEmpty() || shopId.equals("")) {
            txtShopTitle.setText("Default Account");
        } else {
            txtShopTitle.setText(shopName.isEmpty() ? "Shop: " + shopId : shopName);
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void setupTabs() {
        viewPager.setAdapter(new CollectionPagerAdapter());
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Due Payments"); break;
                case 1: tab.setText("Today"); break;
                case 2: tab.setText("Incoming"); break;
            }
        }).attach();
    }

    private void loadCollectionData() {
        showProgress(true);
        duePaymentsList.clear();
        todayList.clear();
        incomingList.clear();
        processedPhones.clear();
        totalCustomers = 0;

        if (userEmailPath.isEmpty()) {
            showError("Please login first");
            return;
        }

        DatabaseReference customersRef = (!shopId.isEmpty() && !shopId.equals("")) ?
                rootRef.child("Khatabook").child(userEmailPath).child("shops").child(shopId).child("customers") :
                rootRef.child("Khatabook").child(userEmailPath).child("customers");

        customersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot customerSnap) {
                if (!customerSnap.exists() || customerSnap.getChildrenCount() == 0) {
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

                totalCustomers = customerPhones.size();
                fetchAllTransactions(customerNames, customerPhones);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadRootCustomers();
            }
        });
    }

    private void loadRootCustomers() {
        DatabaseReference rootCustomersRef = rootRef.child("Khatabook").child(userEmailPath).child("customers");
        rootCustomersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot customerSnap) {
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

                totalCustomers = customerPhones.size();
                fetchAllTransactions(customerNames, customerPhones);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Error loading customers");
            }
        });
    }

    private void fetchAllTransactions(HashMap<String, String> customerNames, ArrayList<String> customerPhones) {
        for (String phone : customerPhones) {
            String customerName = customerNames.get(phone);
            processPhoneTransactions(phone, customerName);
        }
    }

    // 🔥 FIXED: Null-safe transaction processing
    private void processPhoneTransactions(String phone, String customerName) {
        if (processedPhones.contains(phone)) return;
        processedPhones.add(phone);

        final double[] gave = {0};
        final double[] got = {0};
        final long[] customerDueDate = {0};

        // 1. SHOP TRANSACTIONS (if shop exists)
        if (!shopId.isEmpty() && !shopId.equals("")) {
            DatabaseReference shopTxnRef = rootRef.child("Khatabook")
                    .child(userEmailPath).child("shops").child(shopId)
                    .child("transactions").child(phone);

            shopTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // 🔥 FIXED: Null-safe check
                    if (snapshot != null && snapshot.exists()) {
                        for (DataSnapshot txn : snapshot.getChildren()) {
                            processTransactionWithDueDate(txn, gave, got, customerDueDate);
                        }
                    }
                    // Always process root transactions next
                    processRootTransactions(phone, customerName, gave, got, customerDueDate);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    processRootTransactions(phone, customerName, gave, got, customerDueDate);
                }
            });
        } else {
            // No shop - directly process root transactions
            processRootTransactions(phone, customerName, gave, got, customerDueDate);
        }
    }

    private void processRootTransactions(String phone, String customerName, final double[] gave,
                                         final double[] got, final long[] customerDueDate) {
        DatabaseReference rootTxnRef = rootRef.child("Khatabook")
                .child(userEmailPath).child("transactions").child(phone);

        rootTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 🔥 FIXED: Null-safe check
                if (snapshot != null && snapshot.exists()) {
                    for (DataSnapshot txn : snapshot.getChildren()) {
                        processTransactionWithDueDate(txn, gave, got, customerDueDate);
                    }
                }

                // Categorize based on actual due date
                double pending = gave[0] - got[0];
                if (pending > 0) {
                    categorizeCustomer(customerName, phone, pending, customerDueDate[0]);
                }

                checkAllCustomersProcessed();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkAllCustomersProcessed();
            }
        });
    }

    private void processTransactionWithDueDate(DataSnapshot txn, final double[] gave,
                                               final double[] got, final long[] customerDueDate) {
        Long deletedAt = txn.child("deletedAt").getValue(Long.class);
        if (deletedAt != null && deletedAt > 0) return;

        Double amount = getAmount(txn.child("amount"));
        String type = txn.child("type").getValue(String.class);
        Long dueDate = txn.child("dueDate").getValue(Long.class);

        if (amount == null || type == null) return;

        if ("gave".equals(type)) {
            gave[0] += amount;
            // Capture earliest due date from "gave" transactions
            if (dueDate != null && dueDate > 0) {
                if (customerDueDate[0] == 0 || dueDate < customerDueDate[0]) {
                    customerDueDate[0] = dueDate;
                }
            }
        } else if ("got".equals(type)) {
            got[0] += amount;
        }
    }

    private void categorizeCustomer(String customerName, String phone, double pending, long dueDate) {
        long todayStart = getTodayStart();
        long tomorrowStart = todayStart + (24 * 60 * 60 * 1000L);

        CollectionModel model = new CollectionModel(customerName, phone, pending, dueDate);

        if (dueDate == 0) {
            duePaymentsList.add(model);
        } else if (dueDate >= todayStart && dueDate < tomorrowStart) {
            todayList.add(model);
        } else if (dueDate >= tomorrowStart) {
            incomingList.add(model);
        } else {
            duePaymentsList.add(model); // Past due
        }
    }

    private void checkAllCustomersProcessed() {
        if (processedPhones.size() >= totalCustomers) {
            updateUI();
        }
    }

    private long getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
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
        showProgress(false);

        double grandTotalDue = 0;
        for (CollectionModel item : duePaymentsList) grandTotalDue += item.getPendingAmount();
        for (CollectionModel item : todayList) grandTotalDue += item.getPendingAmount();
        for (CollectionModel item : incomingList) grandTotalDue += item.getPendingAmount();

        txtTotalDue.setText(String.format(Locale.getDefault(), "Total Due: ₹%.2f", grandTotalDue));

        if (duePaymentsList.isEmpty() && todayList.isEmpty() && incomingList.isEmpty()) {
            showEmptyState("No pending collections");
        } else {
            viewPager.getAdapter().notifyDataSetChanged();
            viewPager.setVisibility(View.VISIBLE);
            txtEmptyState.setVisibility(View.GONE);
        }
    }

    public void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        viewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        tabLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        showProgress(false);
        txtEmptyState.setText(message);
        txtEmptyState.setVisibility(View.VISIBLE);
        txtTotalDue.setText("Total Due: ₹0.00");
    }

    private void showEmptyState(String message) {
        showProgress(false);
        txtEmptyState.setText(message);
        txtEmptyState.setVisibility(View.VISIBLE);
        txtTotalDue.setText("Total Due: ₹0.00");
    }

    public void showDatePicker(CollectionModel model) {
        Calendar cal = Calendar.getInstance();
        if (model.getDueDate() > 0) {
            cal.setTimeInMillis(model.getDueDate());
        }

        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            long dueDateMillis = selected.getTimeInMillis();

            updateAllGaveTransactions(model.getPhone(), dueDateMillis);

            Toast.makeText(this, "Due date set for " + model.getName(), Toast.LENGTH_LONG).show();
            loadCollectionData(); // Refresh immediately
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateAllGaveTransactions(String phone, long dueDate) {
        // Update ROOT transactions
        DatabaseReference rootTxnRef = rootRef.child("Khatabook").child(userEmailPath)
                .child("transactions").child(phone);

        rootTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.exists()) {
                    for (DataSnapshot txn : snapshot.getChildren()) {
                        String type = txn.child("type").getValue(String.class);
                        if ("gave".equals(type)) {
                            txn.getRef().child("dueDate").setValue(dueDate);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Update SHOP transactions if shop exists
        if (!shopId.isEmpty() && !shopId.equals("")) {
            DatabaseReference shopTxnRef = rootRef.child("Khatabook").child(userEmailPath)
                    .child("shops").child(shopId).child("transactions").child(phone);

            shopTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null && snapshot.exists()) {
                        for (DataSnapshot txn : snapshot.getChildren()) {
                            String type = txn.child("type").getValue(String.class);
                            if ("gave".equals(type)) {
                                txn.getRef().child("dueDate").setValue(dueDate);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCollectionData();
    }

    private class CollectionPagerAdapter extends FragmentStateAdapter {
        public CollectionPagerAdapter() {
            super(CollectionActivity.this);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return CollectionFragment.newInstance(duePaymentsList);
                case 1: return CollectionFragment.newInstance(todayList);
                case 2: return CollectionFragment.newInstance(incomingList);
                default: return CollectionFragment.newInstance(duePaymentsList);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
