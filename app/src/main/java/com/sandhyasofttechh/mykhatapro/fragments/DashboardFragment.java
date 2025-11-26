package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.AddCustomerActivity;
import com.sandhyasofttechh.mykhatapro.activities.AddTransactionActivity;
import com.sandhyasofttechh.mykhatapro.adapter.CustomerSummaryAdapter;
import com.sandhyasofttechh.mykhatapro.model.CustomerSummary;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private TextView tvBalance, tvYouWillGet, tvYouWillGive, tvEmpty;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTransaction, fabAddCustomer;
    private ImageView ivFilterButton, ivPdfReport;

    private List<CustomerSummary> allCustomerSummaries = new ArrayList<>();
    private List<CustomerSummary> displayedCustomerSummaries = new ArrayList<>();
    private CustomerSummaryAdapter adapter;

    private DatabaseReference transactionRef;
    private ValueEventListener transactionListener;
    private PrefManager prefManager;

    // Filtering / sorting state (kept simple here)
    // ... (if you have filter sheet, keep it)

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupFabScrollBehavior();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        loadTransactionData();
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tv_balance_amount);
        tvYouWillGet = view.findViewById(R.id.tv_income);
        tvYouWillGive = view.findViewById(R.id.tv_expense);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView = view.findViewById(R.id.recycler_transactions);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        ivFilterButton = view.findViewById(R.id.iv_filter_button);
        ivPdfReport = view.findViewById(R.id.iv_pdf_report);
    }

    private void setupRecyclerView() {
        adapter = new CustomerSummaryAdapter(displayedCustomerSummaries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        ViewCompat.setNestedScrollingEnabled(recyclerView, true);
    }

    // NEW METHOD: Setup FAB scroll behavior manually
    private void setupFabScrollBehavior() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy > 10 && fabAddTransaction.isShown()) {
                    fabAddTransaction.hide();
                    fabAddCustomer.hide();
                } else if (dy < -10 && !fabAddTransaction.isShown()) {
                    fabAddTransaction.show();
                    fabAddCustomer.show();
                }
            }
        });
    }

    private void initFirebase() {
        if (getContext() == null) return;
        prefManager = new PrefManager(getContext());
        String userEmail = prefManager.getUserEmail();
        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
        transactionRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(userNode).child("transactions");
    }

    private void setupClickListeners() {
        fabAddTransaction.setOnClickListener(v -> startActivity(new Intent(getContext(), AddTransactionActivity.class)));
        fabAddCustomer.setOnClickListener(v -> startActivity(new Intent(getContext(), AddCustomerActivity.class)));

        ivPdfReport.setOnClickListener(v -> {
            // If you want to open a PDF history screen, keep your existing implementation.
            // For now we'll show toast (or keep as before).
            Toast.makeText(getContext(), "Open Reports screen (implement if needed)", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadTransactionData() {
        if (transactionRef == null) return;
        removeFirebaseListener();
        transactionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                processTransactions(snapshot);
                // Update displayed list (no filters in this snippet)
                displayedCustomerSummaries.clear();
                displayedCustomerSummaries.addAll(allCustomerSummaries);
                // sort by most recent
                Collections.sort(displayedCustomerSummaries, (s1, s2) ->
                        Long.compare(s2.getLastTransactionTimestamp(), s1.getLastTransactionTimestamp()));
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) Toast.makeText(getContext(), "Failed to load.", Toast.LENGTH_SHORT).show();
            }
        };
        transactionRef.addValueEventListener(transactionListener);
    }

    private void processTransactions(DataSnapshot snapshot) {
        Map<String, CustomerSummary> summaryMap = new HashMap<>();
        for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
            // Structure assumed: transactions/{customerPhone}/{txnId} -> Transaction
            String customerPhoneKey = customerSnapshot.getKey();
            for (DataSnapshot transactionSnapshot : customerSnapshot.getChildren()) {
                Transaction t = transactionSnapshot.getValue(Transaction.class);
                if (t != null && t.getCustomerPhone() != null) {
                    String phone = t.getCustomerPhone();
                    if (!summaryMap.containsKey(phone)) {
                        CustomerSummary cs = new CustomerSummary(t.getCustomerName(), phone);
                        summaryMap.put(phone, cs);
                    }
                    CustomerSummary cs = summaryMap.get(phone);
                    cs.addTransaction(t); // ensure CustomerSummary has addTransaction(Transaction) method
                }
            }
        }

        allCustomerSummaries = new ArrayList<>(summaryMap.values());

        double headerTotalToGet = 0, headerTotalToGive = 0;
        for (CustomerSummary summary : allCustomerSummaries) {
            if (summary.getNetBalance() > 0) headerTotalToGet += summary.getNetBalance();
            else if (summary.getNetBalance() < 0) headerTotalToGive += Math.abs(summary.getNetBalance());
        }
        updateHeaderSummary(headerTotalToGet, headerTotalToGive);
    }

    private void updateHeaderSummary(double totalToGet, double totalToGive) {
        if (!isAdded() || getContext() == null) return;
        double finalBalance = totalToGet - totalToGive;
        tvYouWillGet.setText(String.format("₹%.2f", totalToGet));
        tvYouWillGive.setText(String.format("₹%.2f", totalToGive));
        tvBalance.setText(String.format("₹%.2f", finalBalance));
        tvBalance.setTextColor(ContextCompat.getColor(getContext(), (finalBalance >= 0) ? R.color.green : R.color.error));
    }

    private void updateEmptyState() {
        if (!isAdded()) return;
        tvEmpty.setVisibility(displayedCustomerSummaries.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(displayedCustomerSummaries.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void removeFirebaseListener() {
        if (transactionRef != null && transactionListener != null)
            transactionRef.removeEventListener(transactionListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeFirebaseListener();
    }
}
