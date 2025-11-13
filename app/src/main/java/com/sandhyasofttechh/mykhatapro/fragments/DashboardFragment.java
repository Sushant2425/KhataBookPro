package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvBalance, tvIncome, tvExpense, tvEmpty;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTransaction, fabAddCustomer;

    private List<Transaction> transactionList = new ArrayList<>();
    private TransactionAdapter adapter;

    private DatabaseReference transactionRef;
    private ValueEventListener transactionListener;
    private PrefManager prefManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        setupRecyclerView();
        setupFABs();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase(); // ← Safe here: context is attached
        loadTransactions();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Reattach listener if needed (optional)
    }

    @Override
    public void onStop() {
        super.onStop();
        removeFirebaseListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeFirebaseListener();
        transactionRef = null;
        transactionListener = null;
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tv_balance_amount);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvEmpty = view.findViewById(R.id.tv_empty);

        recyclerView = view.findViewById(R.id.recycler_transactions);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
    }

    private void initFirebase() {
        if (getContext() == null) return;

        prefManager = new PrefManager(getContext()); // ← Use getContext()
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
        adapter = new TransactionAdapter(transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFABs() {
        fabAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddTransactionActivity.class)));

        fabAddCustomer.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddCustomerActivity.class)));
    }

    private void loadTransactions() {
        if (transactionRef == null) return;

        removeFirebaseListener(); // Prevent duplicate listeners

        transactionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return; // ← SAFETY CHECK

                transactionList.clear();
                double totalIncome = 0;
                double totalExpense = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t != null) {
                        transactionList.add(0, t);

                        if ("got".equals(t.getType())) {
                            totalIncome += t.getAmount();
                        } else if ("gave".equals(t.getType())) {
                            totalExpense += t.getAmount();
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                updateSummary(totalIncome, totalExpense);
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(),
                            "Failed to load: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        transactionRef.orderByChild("timestamp").limitToLast(10)
                .addValueEventListener(transactionListener);
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

        if (transactionList.isEmpty()) {
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