package com.sandhyasofttechh.mykhatapro.fragments;

import android.graphics.Color;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.TopCustomerAdapter;
import com.sandhyasofttechh.mykhatapro.model.Customer;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private TextView tvTotalIncome, tvTotalExpense, tvBalance, tvEmpty;
    private BarChart barChart;
    private PieChart pieChart;
    private RecyclerView recyclerTopCustomers;
    private ChipGroup chipGroup;
    private Chip chip7Days, chip30Days, chipAll;

    private DatabaseReference transactionRef, customerRef;
    private ValueEventListener transactionListener, customerListener;
    private PrefManager prefManager;
    private String userNode;

    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Customer> topCustomers = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.US);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initFirebase();
        setupChips();
        loadData(Filter.ALL);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
    }

    private void initViews(View view) {
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvEmpty = view.findViewById(R.id.tv_empty);

        barChart = view.findViewById(R.id.bar_chart);
        pieChart = view.findViewById(R.id.pie_chart);
        recyclerTopCustomers = view.findViewById(R.id.recycler_top_customers);
        chipGroup = view.findViewById(R.id.chip_group);
        chip7Days = view.findViewById(R.id.chip_7days);
        chip30Days = view.findViewById(R.id.chip_30days);
        chipAll = view.findViewById(R.id.chip_all);

        recyclerTopCustomers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTopCustomers.setAdapter(new TopCustomerAdapter(topCustomers));
    }

    private void initFirebase() {
        if (!isAdded()) return;
        prefManager = new PrefManager(requireContext());
        String email = prefManager.getUserEmail();
        if (email == null || email.isEmpty()) return;

        userNode = email.replace(".", ",");
        transactionRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook").child(userNode).child("transactions");
        customerRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook").child(userNode).child("customers");
    }

    private void setupChips() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Filter filter = Filter.ALL;
            if (checkedId == R.id.chip_7days) filter = Filter.LAST_7_DAYS;
            else if (checkedId == R.id.chip_30days) filter = Filter.LAST_30_DAYS;
            loadData(filter);
        });
    }

    private void loadData(Filter filter) {
        removeListeners();
        transactions.clear();
        topCustomers.clear();

        long cutoff = getCutoffTime(filter);

        transactionListener = transactionRef
                .orderByChild("timestamp")
                .startAt(cutoff)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactions.clear();
                        double income = 0, expense = 0;
                        Map<String, Double> dailyMap = new HashMap<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Transaction t = ds.getValue(Transaction.class);
                            if (t != null && t.getTimestamp() > 0) {
                                transactions.add(t);
                                String dateKey = dateFormat.format(t.getTimestamp());
                                double amt = dailyMap.getOrDefault(dateKey, 0.0);

                                if ("got".equals(t.getType())) {
                                    income += t.getAmount();
                                    amt += t.getAmount();
                                } else if ("gave".equals(t.getType())) {
                                    expense += t.getAmount();
                                    amt -= t.getAmount();
                                }
                                dailyMap.put(dateKey, amt);
                            }
                        }

                        updateSummary(income, expense);
                        setupBarChart(dailyMap);
                        setupPieChart(income, expense);
                        loadTopCustomers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showError(error.getMessage());
                    }
                });
    }

    private void loadTopCustomers() {
        customerListener = customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Customer> customerMap = new HashMap<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Customer c = ds.getValue(Customer.class);
                    if (c != null) {
                        c.setCustomerId(ds.getKey());
                        customerMap.put(c.getPhone(), c);
                    }
                }

                for (Transaction t : transactions) {
                    Customer c = customerMap.get(t.getCustomerPhone());
                    if (c != null) {
                        double due = c.getPendingAmount();
                        if ("gave".equals(t.getType())) due += t.getAmount();
                        else if ("got".equals(t.getType())) due -= t.getAmount();
                        c.setPendingAmount(Math.max(0, due));
                    }
                }

                topCustomers.clear();
                for (Customer c : customerMap.values()) {
                    if (c.getPendingAmount() > 0) topCustomers.add(c);
                }
                Collections.sort(topCustomers, (a, b) -> Double.compare(b.getPendingAmount(), a.getPendingAmount()));
                if (topCustomers.size() > 5) topCustomers.subList(5, topCustomers.size()).clear();

                recyclerTopCustomers.getAdapter().notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError(error.getMessage());
            }
        });
    }

    private void updateSummary(double income, double expense) {
        tvTotalIncome.setText(String.format("₹%.2f", income));
        tvTotalExpense.setText(String.format("₹%.2f", expense));
        double balance = income - expense;
        tvBalance.setText(String.format("₹%.2f", balance));
        tvBalance.setTextColor(balance >= 0 ? getResources().getColor(R.color.green, null)
                : getResources().getColor(R.color.error, null));
    }

    private void setupBarChart(Map<String, Double> dailyMap) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>(dailyMap.keySet());
        Collections.sort(labels);

        for (int i = 0; i < labels.size(); i++) {
            float value = dailyMap.get(labels.get(i)).floatValue();
            entries.add(new BarEntry(i, value));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Balance");
        dataSet.setColors(valueToColor(entries));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelRotationAngle(-45f);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private int[] valueToColor(List<BarEntry> entries) {
        int[] colors = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            colors[i] = entries.get(i).getY() >= 0
                    ? getResources().getColor(R.color.green, null)
                    : getResources().getColor(R.color.error, null);
        }
        return colors;
    }

    private void setupPieChart(double income, double expense) {
        List<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Income"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Expense"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private long getCutoffTime(Filter filter) {
        Calendar cal = Calendar.getInstance();
        switch (filter) {
            case LAST_7_DAYS: cal.add(Calendar.DAY_OF_YEAR, -7); break;
            case LAST_30_DAYS: cal.add(Calendar.DAY_OF_YEAR, -30); break;
            case ALL: return 0;
        }
        return cal.getTimeInMillis();
    }

    private void updateEmptyState() {
        boolean empty = transactions.isEmpty() && topCustomers.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void removeListeners() {
        if (transactionRef != null && transactionListener != null)
            transactionRef.removeEventListener(transactionListener);
        if (customerRef != null && customerListener != null)
            customerRef.removeEventListener(customerListener);
    }

    private void showError(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
    }

    enum Filter { LAST_7_DAYS, LAST_30_DAYS, ALL }

    // Optional: Custom % formatter
    static class PercentFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%.1f%%", value);
        }
    }
}