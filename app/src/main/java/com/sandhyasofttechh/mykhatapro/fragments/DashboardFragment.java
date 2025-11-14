package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
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
import com.sandhyasofttechh.mykhatapro.adapter.TransactionAdapter;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import com.sandhyasofttechh.mykhatapro.utils.TransactionCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment implements FilterBottomSheetFragment.FilterListener {

    private TextView tvBalance, tvIncome, tvExpense, tvEmpty;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTransaction, fabAddCustomer;
    private SearchView searchView;
    private ImageView ivFilterButton, ivPdfReport;

    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> displayedTransactions = new ArrayList<>(); // **FIXED**: Back to holding Transactions
    private TransactionAdapter adapter;

    private DatabaseReference transactionRef;
    private ValueEventListener transactionListener;
    private PrefManager prefManager;
    
    private FilterBottomSheetFragment.FilterOption currentFilter = FilterBottomSheetFragment.FilterOption.ALL;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupRecyclerView();
        setupFABs();
        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        loadTransactions();
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tv_balance_amount);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvEmpty = view.findViewById(R.id.tv_empty);
        recyclerView = view.findViewById(R.id.recycler_transactions);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        searchView = view.findViewById(R.id.search_view);
        ivFilterButton = view.findViewById(R.id.iv_filter_button);
        ivPdfReport = view.findViewById(R.id.iv_pdf_report);
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(displayedTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        if (transactionRef == null) return;
        removeFirebaseListener();

        transactionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                allTransactions.clear();
                for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot transactionSnapshot : customerSnapshot.getChildren()) {
                        Transaction t = transactionSnapshot.getValue(Transaction.class);
                        if (t != null) {
                            allTransactions.add(t);
                        }
                    }
                }
                
                TransactionCalculator.Summary summary = TransactionCalculator.calculate(allTransactions);
                updateHeaderSummary(summary);
                
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) Toast.makeText(getContext(), "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        transactionRef.addValueEventListener(transactionListener);
    }
    
    @Override
    public void onFilterSelected(FilterBottomSheetFragment.FilterOption option) {
        currentFilter = option;
        applyFilters();
    }

    private void applyFilters() {
        // **FIXED**: This logic now filters and sorts the list of individual transactions.
        List<Transaction> filteredList = new ArrayList<>(allTransactions);

        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(t -> t.getCustomerName() != null && t.getCustomerName().toLowerCase().contains(currentSearchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        switch (currentFilter) {
            case YOU_GAVE:
                filteredList = filteredList.stream().filter(t -> "gave".equals(t.getType())).collect(Collectors.toList());
                break;
            case YOU_GOT:
                filteredList = filteredList.stream().filter(t -> "got".equals(t.getType())).collect(Collectors.toList());
                break;
            case HIGHEST_AMOUNT:
                Collections.sort(filteredList, (t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
                break;
            case LOWEST_AMOUNT:
                Collections.sort(filteredList, (t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));
                break;
            case OLDEST:
                Collections.sort(filteredList, (t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp()));
                break;
            default: // MOST_RECENT and ALL
                Collections.sort(filteredList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                break;
        }

        displayedTransactions.clear();
        displayedTransactions.addAll(filteredList);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateHeaderSummary(TransactionCalculator.Summary summary) {
        if (!isAdded() || getContext() == null) return;

        double finalBalance = summary.balance;
        tvIncome.setText(String.format("₹%.2f", summary.totalGot));
        tvExpense.setText(String.format("₹%.2f", summary.totalGave));
        tvBalance.setText(String.format("₹%.2f", finalBalance));

        int colorRes = (finalBalance > 0) ? R.color.green : (finalBalance < 0) ? R.color.error : R.color.black;
        tvBalance.setTextColor(ContextCompat.getColor(getContext(), colorRes));
    }

    private void updateEmptyState() {
        if (!isAdded()) return;
        tvEmpty.setVisibility(displayedTransactions.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(displayedTransactions.isEmpty() ? View.GONE : View.VISIBLE);
    }
    
    // All other methods (initFirebase, setupFABs, etc.) remain unchanged.
    // ...
    private void initFirebase() {
        if (getContext() == null) return;
        prefManager = new PrefManager(getContext());
        String userEmail = prefManager.getUserEmail();
        String userNode = userEmail.replace(".", ",");
        transactionRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(userNode).child("transactions");
    }

    private void setupFABs() {
        fabAddTransaction.setOnClickListener(v -> startActivity(new Intent(getContext(), AddTransactionActivity.class)));
        fabAddCustomer.setOnClickListener(v -> startActivity(new Intent(getContext(), AddCustomerActivity.class)));
    }

    private void setupClickListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });
        ivFilterButton.setOnClickListener(v -> {
            FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });
        ivPdfReport.setOnClickListener(v -> Toast.makeText(getContext(), "PDF Report Clicked!", Toast.LENGTH_SHORT).show());
    }
    
    private void removeFirebaseListener() {
        if (transactionRef != null && transactionListener != null) {
            transactionRef.removeEventListener(transactionListener);
        }
    }
}