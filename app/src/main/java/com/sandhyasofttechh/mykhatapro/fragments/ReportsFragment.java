//package com.sandhyasofttechh.mykhatapro.fragments;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.charts.PieChart;
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.data.PieData;
//import com.github.mikephil.charting.data.PieDataSet;
//import com.github.mikephil.charting.data.PieEntry;
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.fragments.CustomerListFragment;
//import com.sandhyasofttechh.mykhatapro.fragments.CustomerReportFragment;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Locale;
//
//public class ReportsFragment extends Fragment {
//
//    private LinearLayout rowCustomerReports, rowCustomerList;
//    private BarChart barChartMonthly;
//    private LineChart lineChartTrend;
//    private PieChart pieChartGaveGot;
//    private ProgressBar progressBar;
//
//    private DatabaseReference transactionsRef;
//
//    private HashMap<String, Float> monthlyGave = new HashMap<>();
//    private HashMap<String, Float> monthlyGot = new HashMap<>();
//
//    private float totalGave = 0f;
//    private float totalGot = 0f;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_reports, container, false);
//
//        // Initialize views
//        rowCustomerReports = rootView.findViewById(R.id.rowCustomerReports);
//        rowCustomerList = rootView.findViewById(R.id.rowCustomerList);
//        barChartMonthly = rootView.findViewById(R.id.barChartMonthly);
//        lineChartTrend = rootView.findViewById(R.id.lineChartTrend);
//        pieChartGaveGot = rootView.findViewById(R.id.pieChartGaveGot);
//        progressBar = rootView.findViewById(R.id.progressBar);
//
//        // Setup Firebase reference to your actual node path
//        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        if (userEmail == null) {
//            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
//            return rootView;
//        }
//
//        String sanitizedUserEmail = userEmail.replace(".", ",");  // Replace dot for Firebase key compatibility
//        transactionsRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(sanitizedUserEmail)
//                .child("transactions");
//
//        // Set click listeners to open corresponding fragments
//        rowCustomerReports.setOnClickListener(view -> {
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new CustomerReportFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        rowCustomerList.setOnClickListener(view -> {
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new CustomerListFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        // Fetch and display data
//        fetchTransactions();
//
//        return rootView;
//    }
//
//    private void fetchTransactions() {
//        progressBar.setVisibility(View.VISIBLE);
//
//        initializeMonthlyMaps();
//
//        transactionsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                monthlyGave.replaceAll((k, v) -> 0f);
//                monthlyGot.replaceAll((k, v) -> 0f);
//                totalGave = 0f;
//                totalGot = 0f;
//
//                for (DataSnapshot phoneSnapshot : snapshot.getChildren()) {
//                    for (DataSnapshot transactionSnapshot : phoneSnapshot.getChildren()) {
//                        String type = transactionSnapshot.child("type").getValue(String.class);
//                        String dateStr = transactionSnapshot.child("date").getValue(String.class);
//                        Object amountObj = transactionSnapshot.child("amount").getValue();
//
//                        if (type == null || dateStr == null || amountObj == null)
//                            continue;
//
//                        float amount = parseAmount(amountObj);
//                        String monthYear = parseMonthYear(dateStr);
//                        if (monthYear == null)
//                            continue;
//
//                        if (type.equalsIgnoreCase("gave")) {
//                            float current = monthlyGave.getOrDefault(monthYear, 0f);
//                            monthlyGave.put(monthYear, current + amount);
//                            totalGave += amount;
//                        } else if (type.equalsIgnoreCase("got")) {
//                            float current = monthlyGot.getOrDefault(monthYear, 0f);
//                            monthlyGot.put(monthYear, current + amount);
//                            totalGot += amount;
//                        }
//                    }
//                }
//
//                progressBar.setVisibility(View.GONE);
//                setupBarChart();
//                setupLineChart();
//                setupGaveGotPieChart();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void initializeMonthlyMaps() {
//        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
//                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//        for (String month : months) {
//            String key = month + " " + year;
//            monthlyGave.put(key, 0f);
//            monthlyGot.put(key, 0f);
//        }
//    }
//
//    private float parseAmount(Object amountObj) {
//        if (amountObj instanceof Long)
//            return ((Long) amountObj).floatValue();
//        if (amountObj instanceof Double)
//            return ((Double) amountObj).floatValue();
//        if (amountObj instanceof Integer)
//            return ((Integer) amountObj).floatValue();
//        if (amountObj instanceof String) {
//            try {
//                return Float.parseFloat((String) amountObj);
//            } catch (NumberFormatException e) {
//                return 0f;
//            }
//        }
//        return 0f;
//    }
//
//    private String parseMonthYear(String dateStr) {
//        try {
//            SimpleDateFormat sdfInput = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
//            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
//            return sdfOutput.format(sdfInput.parse(dateStr));
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private void setupBarChart() {
//        ArrayList<BarEntry> gaveEntries = new ArrayList<>();
//        ArrayList<BarEntry> gotEntries = new ArrayList<>();
//        ArrayList<String> labels = new ArrayList<>();
//        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
//                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//
//        for (int i = 0; i < months.length; i++) {
//            String key = months[i] + " " + year;
//            gaveEntries.add(new BarEntry(i, monthlyGave.getOrDefault(key, 0f)));
//            gotEntries.add(new BarEntry(i, monthlyGot.getOrDefault(key, 0f)));
//            labels.add(months[i]);
//        }
//
//        BarDataSet gaveDataSet = new BarDataSet(gaveEntries, "You Gave");
//        gaveDataSet.setColor(Color.rgb(255, 102, 102));
//        BarDataSet gotDataSet = new BarDataSet(gotEntries, "You Got");
//        gotDataSet.setColor(Color.rgb(102, 204, 102));
//
//        BarData barData = new BarData(gaveDataSet, gotDataSet);
//        barData.setBarWidth(0.35f);
//
//        barChartMonthly.setData(barData);
//        barChartMonthly.getDescription().setEnabled(false);
//        barChartMonthly.groupBars(0f, 0.15f, 0.05f);
//
//        XAxis xAxis = barChartMonthly.getXAxis();
//        xAxis.setGranularity(1f);
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
//        xAxis.setCenterAxisLabels(true);
//        xAxis.setAxisMinimum(0);
//        xAxis.setAxisMaximum(months.length);
//
//        barChartMonthly.getAxisRight().setEnabled(false);
//        barChartMonthly.invalidate();
//    }
//
//    private void setupLineChart() {
//        ArrayList<Entry> netEntries = new ArrayList<>();
//        ArrayList<String> labels = new ArrayList<>();
//        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
//                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//
//        for (int i = 0; i < months.length; i++) {
//            String key = months[i] + " " + year;
//            float net = monthlyGot.getOrDefault(key, 0f) - monthlyGave.getOrDefault(key, 0f);
//            netEntries.add(new Entry(i, net));
//            labels.add(months[i]);
//        }
//
//        LineDataSet lineDataSet = new LineDataSet(netEntries, "Net Balance Trend");
//        lineDataSet.setColor(Color.BLUE);
//        lineDataSet.setCircleColor(Color.BLUE);
//        lineDataSet.setLineWidth(2f);
//        lineDataSet.setCircleRadius(4f);
//        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//
//        LineData lineData = new LineData(lineDataSet);
//        lineChartTrend.setData(lineData);
//        lineChartTrend.getDescription().setEnabled(false);
//
//        XAxis xAxis = lineChartTrend.getXAxis();
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//
//        lineChartTrend.getAxisRight().setEnabled(false);
//        lineChartTrend.invalidate();
//    }
//
//    private void setupGaveGotPieChart() {
//        ArrayList<PieEntry> pieEntries = new ArrayList<>();
//
//        if (totalGave > 0) pieEntries.add(new PieEntry(totalGave, "You Gave"));
//        if (totalGot > 0) pieEntries.add(new PieEntry(totalGot, "You Got"));
//        if (pieEntries.isEmpty()) pieEntries.add(new PieEntry(1, "No Data"));
//
//        PieDataSet dataSet = new PieDataSet(pieEntries, "");
//        dataSet.setColors(Color.rgb(255, 102, 102), Color.rgb(102, 204, 102));
//        dataSet.setValueTextColor(Color.WHITE);
//        dataSet.setValueTextSize(14f);
//
//        PieData data = new PieData(dataSet);
//
//        pieChartGaveGot.setData(data);
//        pieChartGaveGot.setCenterText("Total\n₹" + (int) (totalGave + totalGot));
//        pieChartGaveGot.setHoleRadius(40f);
//        pieChartGaveGot.setTransparentCircleRadius(45f);
//        pieChartGaveGot.getDescription().setEnabled(false);
//
//        Legend legend = pieChartGaveGot.getLegend();
//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        legend.setDrawInside(false);
//
//        pieChartGaveGot.invalidate();
//    }
//}




package com.sandhyasofttechh.mykhatapro.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.*;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportsFragment extends Fragment {

    private LinearLayout rowCustomerReports, rowCustomerList;
    private BarChart barChartMonthly;
    private LineChart lineChartTrend;
    private PieChart pieChartGaveGot;
    private ProgressBar progressBar;

    private DatabaseReference transactionsRef;

    private HashMap<String, Float> monthlyGave = new HashMap<>();
    private HashMap<String, Float> monthlyGot = new HashMap<>();

    private float totalGave = 0f;
    private float totalGot = 0f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reports, container, false);

        rowCustomerReports = rootView.findViewById(R.id.rowCustomerReports);
        rowCustomerList = rootView.findViewById(R.id.rowCustomerList);
        barChartMonthly = rootView.findViewById(R.id.barChartMonthly);
        lineChartTrend = rootView.findViewById(R.id.lineChartTrend);
        pieChartGaveGot = rootView.findViewById(R.id.pieChartGaveGot);
        progressBar = rootView.findViewById(R.id.progressBar);

        setupFirebasePath();
        setupClickListeners();
        fetchTransactions();

        return rootView;
    }

    // -------------------------------------------------------
    // 🔥 UPDATED Firebase Path logic same as DashboardFragment
    // -------------------------------------------------------
    private void setupFirebasePath() {
        PrefManager prefManager = new PrefManager(requireContext());
        String userEmail = prefManager.getUserEmail();

        if (userEmail == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userNode = userEmail.replace(".", ",");
        String shopId = prefManager.getCurrentShopId();

        if (shopId == null || shopId.isEmpty()) {

            // NO SHOP → load MAIN DATA
            transactionsRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("transactions");

        } else {

            // SHOP SELECTED → load only that shop
            transactionsRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("shops")
                    .child(shopId)
                    .child("transactions");
        }
    }

    // -----------------------

    private void setupClickListeners() {
        rowCustomerReports.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CustomerReportFragment())
                    .addToBackStack(null)
                    .commit();
        });

        rowCustomerList.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CustomerListFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void fetchTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        initializeMonthlyMaps();

        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                monthlyGave.replaceAll((k, v) -> 0f);
                monthlyGot.replaceAll((k, v) -> 0f);
                totalGave = 0f;
                totalGot = 0f;

                for (DataSnapshot phoneNode : snapshot.getChildren()) {
                    for (DataSnapshot txnNode : phoneNode.getChildren()) {

                        String type = txnNode.child("type").getValue(String.class);
                        String dateStr = txnNode.child("date").getValue(String.class);
                        Object amountObj = txnNode.child("amount").getValue();

                        if (type == null || dateStr == null || amountObj == null) continue;

                        float amount = parseAmount(amountObj);
                        String monthYear = parseMonthYear(dateStr);

                        if (monthYear == null) continue;

                        if (type.equalsIgnoreCase("gave")) {
                            monthlyGave.put(monthYear, monthlyGave.get(monthYear) + amount);
                            totalGave += amount;
                        } else if (type.equalsIgnoreCase("got")) {
                            monthlyGot.put(monthYear, monthlyGot.get(monthYear) + amount);
                            totalGot += amount;
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                setupBarChart();
                setupLineChart();
                setupGaveGotPieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeMonthlyMaps() {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"};

        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (String m : months) {
            String key = m + " " + year;
            monthlyGave.put(key, 0f);
            monthlyGot.put(key, 0f);
        }
    }

    private float parseAmount(Object amountObj) {
        if (amountObj instanceof Long) return ((Long) amountObj).floatValue();
        if (amountObj instanceof Double) return ((Double) amountObj).floatValue();
        if (amountObj instanceof Integer) return ((Integer) amountObj).floatValue();
        if (amountObj instanceof String) {
            try { return Float.parseFloat((String) amountObj); }
            catch (Exception ignore) { }
        }
        return 0f;
    }

    private String parseMonthYear(String dateStr) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            SimpleDateFormat out = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
            return out.format(in.parse(dateStr));
        } catch (Exception e) {
            return null;
        }
    }

    private void setupBarChart() {
        ArrayList<BarEntry> gaveList = new ArrayList<>();
        ArrayList<BarEntry> gotList = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 0; i < months.length; i++) {
            String key = months[i] + " " + year;
            gaveList.add(new BarEntry(i, monthlyGave.get(key)));
            gotList.add(new BarEntry(i, monthlyGot.get(key)));
            labels.add(months[i]);
        }

        BarDataSet gaveSet = new BarDataSet(gaveList, "You Gave");
        gaveSet.setColor(Color.parseColor("#FF6666"));

        BarDataSet gotSet = new BarDataSet(gotList, "You Got");
        gotSet.setColor(Color.parseColor("#66CC66"));

        BarData data = new BarData(gaveSet, gotSet);
        data.setBarWidth(0.35f);

        barChartMonthly.setData(data);
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.groupBars(0f, 0.15f, 0.05f);

        XAxis x = barChartMonthly.getXAxis();
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setCenterAxisLabels(true);

        barChartMonthly.getAxisRight().setEnabled(false);
        barChartMonthly.invalidate();
    }

    private void setupLineChart() {
        ArrayList<Entry> netEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 0; i < months.length; i++) {
            String key = months[i] + " " + year;
            float net = monthlyGot.get(key) - monthlyGave.get(key);
            netEntries.add(new Entry(i, net));
            labels.add(months[i]);
        }

        LineDataSet set = new LineDataSet(netEntries, "Net Balance Trend");
        set.setColor(Color.BLUE);
        set.setCircleColor(Color.BLUE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);

        LineData data = new LineData(set);
        lineChartTrend.setData(data);
        lineChartTrend.getDescription().setEnabled(false);

        XAxis xAxis = lineChartTrend.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChartTrend.getAxisRight().setEnabled(false);
        lineChartTrend.invalidate();
    }

    private void setupGaveGotPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (totalGave > 0) entries.add(new PieEntry(totalGave, "You Gave"));
        if (totalGot > 0) entries.add(new PieEntry(totalGot, "You Got"));
        if (entries.isEmpty()) entries.add(new PieEntry(1, "No Data"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(Color.parseColor("#FF6666"), Color.parseColor("#66CC66"));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(14f);

        PieData data = new PieData(set);
        pieChartGaveGot.setData(data);
        pieChartGaveGot.setCenterText("Total\n₹" + (int)(totalGave + totalGot));
        pieChartGaveGot.setHoleRadius(40f);
        pieChartGaveGot.setTransparentCircleRadius(45f);
        pieChartGaveGot.getDescription().setEnabled(false);

        Legend legend = pieChartGaveGot.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        pieChartGaveGot.invalidate();
    }
}
