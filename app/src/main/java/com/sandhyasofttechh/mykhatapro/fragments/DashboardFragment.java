package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment implements FilterBottomSheetFragment.FilterListener {

    private TextView tvBalance, tvYouWillGet, tvYouWillGive, tvEmpty;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTransaction, fabAddCustomer;
    private SearchView searchView;
    private ImageView ivFilterButton, ivPdfReport;


    private List<CustomerSummary> allCustomerSummaries = new ArrayList<>();
    private List<CustomerSummary> displayedCustomerSummaries = new ArrayList<>();
    private CustomerSummaryAdapter adapter;

    private DatabaseReference transactionRef;
    private ValueEventListener transactionListener;
    private PrefManager prefManager;
    
    // --- State Variables for Filtering & Sorting ---
    private FilterBottomSheetFragment.FilterType currentFilter = FilterBottomSheetFragment.FilterType.ALL;
    private FilterBottomSheetFragment.SortType currentSort = FilterBottomSheetFragment.SortType.MOST_RECENT;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        loadTransactionData();
    }
    
    // --- THIS IS THE CORRECT, IMPLEMENTED METHOD ---
    @Override
    public void onFiltersApplied(FilterBottomSheetFragment.FilterType filter, FilterBottomSheetFragment.SortType sort) {
        this.currentFilter = filter;
        this.currentSort = sort;
        applyFilters();
    }

    private void applyFilters() {
        List<CustomerSummary> filteredList = new ArrayList<>(allCustomerSummaries);

        // 1. Search Filter
        if (!currentSearchQuery.isEmpty()) {
            filteredList = filteredList.stream()
                .filter(s -> s.getCustomerName().toLowerCase().contains(currentSearchQuery.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // 2. Status Filter
        switch (currentFilter) {
            case YOU_WILL_GET:
                filteredList = filteredList.stream().filter(s -> s.getNetBalance() > 0).collect(Collectors.toList());
                break;
            case YOU_WILL_GIVE:
                filteredList = filteredList.stream().filter(s -> s.getNetBalance() < 0).collect(Collectors.toList());
                break;
            case SETTLED_UP:
                filteredList = filteredList.stream().filter(s -> s.getNetBalance() == 0).collect(Collectors.toList());
                break;
        }

        // 3. Sorting
        switch (currentSort) {
            case HIGHEST_AMOUNT:
                Collections.sort(filteredList, (s1, s2) -> Double.compare(s2.getNetBalance(), s1.getNetBalance()));
                break;
            case LOWEST_AMOUNT:
                Collections.sort(filteredList, (s1, s2) -> Double.compare(s1.getNetBalance(), s2.getNetBalance()));
                break;
            case NAME_AZ:
                Collections.sort(filteredList, (s1, s2) -> s1.getCustomerName().compareToIgnoreCase(s2.getCustomerName()));
                break;
            case NAME_ZA:
                Collections.sort(filteredList, (s1, s2) -> s2.getCustomerName().compareToIgnoreCase(s1.getCustomerName()));
                break;
            default: // MOST_RECENT
                Collections.sort(filteredList, (s1, s2) -> Long.compare(s2.getLastTransactionTimestamp(), s1.getLastTransactionTimestamp()));
                break;
        }

        displayedCustomerSummaries.clear();
        displayedCustomerSummaries.addAll(filteredList);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    // --- Unchanged Methods ---

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tv_balance_amount);
        tvYouWillGet = view.findViewById(R.id.tv_income);
        tvYouWillGive = view.findViewById(R.id.tv_expense);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView = view.findViewById(R.id.recycler_transactions);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        searchView = view.findViewById(R.id.search_view);
        ivFilterButton = view.findViewById(R.id.iv_filter_button);
        ivPdfReport = view.findViewById(R.id.iv_pdf_report);
    }
    
    private void setupRecyclerView() {
        adapter = new CustomerSummaryAdapter(displayedCustomerSummaries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadTransactionData() {
        if (transactionRef == null) return;
        removeFirebaseListener();
        transactionListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                processTransactions(snapshot);
                applyFilters();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { if (isAdded()) Toast.makeText(getContext(), "Failed to load.", Toast.LENGTH_SHORT).show(); }
        };
        transactionRef.addValueEventListener(transactionListener);
    }
    
    private void processTransactions(DataSnapshot snapshot) {
        Map<String, CustomerSummary> summaryMap = new HashMap<>();
        for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
            for (DataSnapshot transactionSnapshot : customerSnapshot.getChildren()) {
                Transaction t = transactionSnapshot.getValue(Transaction.class);
                if (t != null && t.getCustomerPhone() != null) {
                    summaryMap.putIfAbsent(t.getCustomerPhone(), new CustomerSummary(t.getCustomerName(), t.getCustomerPhone()));
                    summaryMap.get(t.getCustomerPhone()).addTransaction(t);
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
    
    private void initFirebase() {
        if (getContext() == null) return;
        prefManager = new PrefManager(getContext());
        String userEmail = prefManager.getUserEmail();
        String userNode = userEmail.replace(".", ",");
        transactionRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(userNode).child("transactions");
    }

    private void setupClickListeners() {
        fabAddTransaction.setOnClickListener(v -> startActivity(new Intent(getContext(), AddTransactionActivity.class)));
        fabAddCustomer.setOnClickListener(v -> startActivity(new Intent(getContext(), AddCustomerActivity.class)));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });
        ivFilterButton.setOnClickListener(v -> {
            FilterBottomSheetFragment bottomSheet = FilterBottomSheetFragment.newInstance(currentFilter, currentSort);
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });
        ivPdfReport.setOnClickListener(v -> Toast.makeText(getContext(), "PDF Report Clicked!", Toast.LENGTH_SHORT).show());
    }
    
    private void removeFirebaseListener() {
        if (transactionRef != null && transactionListener != null) transactionRef.removeEventListener(transactionListener);
    }
}