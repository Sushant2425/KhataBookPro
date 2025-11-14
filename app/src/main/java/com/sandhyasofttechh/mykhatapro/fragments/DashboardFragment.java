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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment implements FilterBottomSheetFragment.FilterListener {

    private TextView tvBalance, tvIncome, tvExpense, tvEmpty;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTransaction, fabAddCustomer;
    private SearchView searchView;
    private ImageView ivFilter;

    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> displayedTransactions = new ArrayList<>();
    private TransactionAdapter adapter;

    private DatabaseReference transactionRef;
    private ValueEventListener transactionListener;
    private PrefManager prefManager;
    
    private FilterBottomSheetFragment.FilterOption currentFilter = FilterBottomSheetFragment.FilterOption.ALL;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupRecyclerView();
        setupFABs();
        setupSearchAndFilter();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        loadTransactions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeFirebaseListener();
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
        ivFilter = view.findViewById(R.id.iv_filter);
    }

    private void initFirebase() {
        if (getContext() == null) return;
        prefManager = new PrefManager(getContext());
        String userEmail = prefManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userNode = userEmail.replace(".", ",");
        transactionRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(userNode)
                .child("transactions");
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(displayedTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        // NO OnScrollListener HERE. It is handled by the Behavior in XML.
    }

    private void setupFABs() {
        fabAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddTransactionActivity.class)));
        fabAddCustomer.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddCustomerActivity.class)));
    }

    private void setupSearchAndFilter() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });

        ivFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });
    }

    private void loadTransactions() {
        if (transactionRef == null) return;
        removeFirebaseListener();

        transactionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                allTransactions.clear();
                double totalIncome = 0;
                double totalExpense = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t != null) {
                        allTransactions.add(t);
                        if ("got".equals(t.getType())) {
                            totalIncome += t.getAmount();
                        } else {
                            totalExpense += t.getAmount();
                        }
                    }
                }
                updateSummary(totalIncome, totalExpense);
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
        List<Transaction> filteredList = new ArrayList<>(allTransactions);

        // Apply search query
        if (!currentSearchQuery.isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(t -> t.getCustomerName() != null && t.getCustomerName().toLowerCase().contains(currentSearchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Apply filter and sort options
        switch (currentFilter) {
            case YOU_GAVE:
                filteredList = filteredList.stream().filter(t -> "gave".equals(t.getType())).collect(Collectors.toList());
                Collections.sort(filteredList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp())); // Default sort
                break;
            case YOU_GOT:
                filteredList = filteredList.stream().filter(t -> "got".equals(t.getType())).collect(Collectors.toList());
                Collections.sort(filteredList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp())); // Default sort
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
            case ALL:
            case MOST_RECENT:
            default:
                Collections.sort(filteredList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                break;
        }

        displayedTransactions.clear();
        displayedTransactions.addAll(filteredList);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateSummary(double income, double expense) {
        if (!isAdded()) return;
        double balance = income - expense;
        tvIncome.setText(String.format("₹%.2f", income));
        tvExpense.setText(String.format("₹%.2f", expense));
        tvBalance.setText(String.format("₹%.2f", balance));
        int color = balance >= 0 ? R.color.green : R.color.error;
        tvBalance.setTextColor(getResources().getColor(color, requireContext().getTheme()));
    }

    private void updateEmptyState() {
        if (!isAdded()) return;
        if (displayedTransactions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void removeFirebaseListener() {
        if (transactionRef != null && transactionListener != null) {
            transactionRef.removeEventListener(transactionListener);
        }
    }
}