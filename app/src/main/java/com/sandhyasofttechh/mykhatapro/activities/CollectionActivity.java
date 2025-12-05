package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.sandhyasofttechh.mykhatapro.fragments.CollectionFragment;
import com.sandhyasofttechh.mykhatapro.model.CollectionModel;
import com.sandhyasofttechh.mykhatapro.receiver.CollectionReminderReceiver;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CollectionActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    TextView txtTotalDue, txtEmptyState, txtShopTitle;
    ProgressBar progressBar;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    ArrayList<CollectionModel> duePaymentsList = new ArrayList<>();
    ArrayList<CollectionModel> todayList = new ArrayList<>();
    ArrayList<CollectionModel> incomingList = new ArrayList<>();

    PrefManager prefManager;
    DatabaseReference rootRef;
    String userEmailPath = "", shopId = "", shopName = "";

    private int totalCustomers = 0;
    private int processedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        initViews();
        initData();
        setupTabs();

        requestNotificationPermission();
        loadCollectionData();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
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

        if (shopId == null || shopId.isEmpty()) {
            txtShopTitle.setText("Default Account");
        } else {
            txtShopTitle.setText(shopName == null || shopName.isEmpty() ? "Shop: " + shopId : shopName);
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
        processedCount = 0;

        if (userEmailPath.isEmpty()) {
            showError("Please login first");
            return;
        }

        DatabaseReference customersRef = (!shopId.isEmpty()) ?
                rootRef.child("Khatabook").child(userEmailPath).child("shops").child(shopId).child("customers") :
                rootRef.child("Khatabook").child(userEmailPath).child("customers");

        customersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists() || snap.getChildrenCount() == 0) {
                    loadRootCustomers();
                } else {
                    processCustomerList(snap);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { loadRootCustomers(); }
        });
    }

    private void loadRootCustomers() {
        rootRef.child("Khatabook").child(userEmailPath).child("customers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) {
                            showEmptyState("No customers found");
                        } else {
                            processCustomerList(snap);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        showError("Failed to load data");
                    }
                });
    }

    private void processCustomerList(DataSnapshot snap) {
        HashMap<String, String> names = new HashMap<>();
        ArrayList<String> phones = new ArrayList<>();

        for (DataSnapshot child : snap.getChildren()) {
            String phone = child.getKey();
            String name = child.child("name").getValue(String.class);
            names.put(phone, name == null ? phone : name);
            phones.add(phone);
        }

        totalCustomers = phones.size();
        if (totalCustomers == 0) {
            showEmptyState("No customers");
            return;
        }

        for (String phone : phones) {
            processCustomerTransactions(phone, names.get(phone));
        }
    }

    private void processCustomerTransactions(String phone, String name) {
        final double[] gave = {0};
        final double[] got = {0};
        final long[] earliestDue = {0};

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot txn : snapshot.getChildren()) {
                        Long deleted = txn.child("deletedAt").getValue(Long.class);
                        if (deleted != null && deleted > 0) continue;

                        Double amt = getAmount(txn.child("amount"));
                        String type = txn.child("type").getValue(String.class);
                        Long due = txn.child("dueDate").getValue(Long.class);

                        if (amt == null || type == null) continue;

                        if ("gave".equals(type)) {
                            gave[0] += amt;
                            if (due != null && due > 0 && (earliestDue[0] == 0 || due < earliestDue[0])) {
                                earliestDue[0] = due;
                            }
                        } else if ("got".equals(type)) {
                            got[0] += amt;
                        }
                    }
                }

                double pending = gave[0] - got[0];
                if (pending > 0) {
                    categorizeCustomer(name, phone, pending, earliestDue[0]);
                }

                processedCount++;
                if (processedCount >= totalCustomers) {
                    runOnUiThread(() -> updateUIAndScheduleReminders());
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                processedCount++;
                if (processedCount >= totalCustomers) {
                    runOnUiThread(() -> updateUIAndScheduleReminders());
                }
            }
        };

        DatabaseReference ref = (!shopId.isEmpty())
                ? rootRef.child("Khatabook").child(userEmailPath).child("shops").child(shopId)
                .child("transactions").child(phone)
                : rootRef.child("Khatabook").child(userEmailPath).child("transactions").child(phone);

        ref.addListenerForSingleValueEvent(listener);
    }

    private void categorizeCustomer(String name, String phone, double pending, long dueDate) {
        long todayStart = getTodayStart();
        long tomorrowStart = todayStart + 86400000L;

        if (isAlreadyAdded(phone)) return;

        CollectionModel model = new CollectionModel(name, phone, pending, dueDate);

        if (dueDate == 0 || dueDate < todayStart) {
            duePaymentsList.add(model);
        } else if (dueDate < tomorrowStart) {
            todayList.add(model);
        } else {
            incomingList.add(model);
        }

    }

    private boolean isAlreadyAdded(String phone) {
        for (CollectionModel m : duePaymentsList) {
            if (m.getPhone().equals(phone)) return true;
        }
        for (CollectionModel m : todayList) {
            if (m.getPhone().equals(phone)) return true;
        }
        for (CollectionModel m : incomingList) {
            if (m.getPhone().equals(phone)) return true;
        }
        return false;
    }


    private long getTodayStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private Double getAmount(DataSnapshot snap) {
        if (!snap.exists()) return 0.0;
        Object val = snap.getValue();
        return (val instanceof Number) ? ((Number) val).doubleValue() : 0.0;
    }

    private void updateUIAndScheduleReminders() {
        showProgress(false);

        double total = 0;
        for (CollectionModel m : duePaymentsList) total += m.getPendingAmount();
        for (CollectionModel m : todayList) total += m.getPendingAmount();
        for (CollectionModel m : incomingList) total += m.getPendingAmount();

        txtTotalDue.setText(String.format(Locale.getDefault(), "Total Due: ₹%.2f", total));

        if (duePaymentsList.isEmpty() && todayList.isEmpty() && incomingList.isEmpty()) {
            showEmptyState("No pending collections");
        } else {
            viewPager.getAdapter().notifyDataSetChanged();
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            txtEmptyState.setVisibility(View.GONE);
        }

        scheduleRemindersForTodayAndFuture();
    }

    private void scheduleRemindersForTodayAndFuture() {
        // TODAY WALE CUSTOMERS → NEXT 2 MINUTE MEIN NOTIFICATION
        for (CollectionModel m : todayList) {
            long testTrigger = System.currentTimeMillis() + 2 * 60 * 1000; // +2 minute
            scheduleSingleReminder(m, testTrigger);
        }

        // INCOMING WALE CUSTOMERS → NEXT 4 MINUTE MEIN NOTIFICATION
        for (CollectionModel m : incomingList) {
            long testTrigger = System.currentTimeMillis() + 4 * 60 * 1000; // +4 minute
            scheduleSingleReminder(m, testTrigger);
        }

        // OPTIONAL: Due Payments wale bhi test karna chahe to +6 minute
         for (CollectionModel m : duePaymentsList) {
             long testTrigger = System.currentTimeMillis() + 6 * 60 * 1000;
             scheduleSingleReminder(m, testTrigger);
         }

//        Toast.makeText(this, "TESTING MODE: Notifications 2-4 minute mein aayengi!", Toast.LENGTH_LONG).show();
    }
    private long getNext9AM() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        return c.getTimeInMillis();
    }

    // 100% CRASH-FREE EXACT ALARM (Android 12+ ke liye safe)
    private void scheduleSingleReminder(CollectionModel model, long triggerAt) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, CollectionReminderReceiver.class);
        intent.putExtra("name", model.getName());
        intent.putExtra("amount", model.getPendingAmount());
        intent.putExtra("phone", model.getPhone());

        int requestCode = ("remind_" + model.getPhone()).hashCode();

        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                // Permission nahi → user se maang lo
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
                // Fallback: Inexact alarm
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        viewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        tabLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        showProgress(false);
        txtEmptyState.setText(msg);
        txtEmptyState.setVisibility(View.VISIBLE);
        txtTotalDue.setText("Total Due: ₹0.00");
    }

    private void showEmptyState(String msg) {
        showProgress(false);
        txtEmptyState.setText(msg);
        txtEmptyState.setVisibility(View.VISIBLE);
        txtTotalDue.setText("Total Due: ₹0.00");
    }

    // Date picker from adapter
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
            loadCollectionData();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateAllGaveTransactions(String phone, long dueDate) {
        DatabaseReference rootTxnRef = rootRef.child("Khatabook").child(userEmailPath)
                .child("transactions").child(phone);

        rootTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot txn : snapshot.getChildren()) {
                        String type = txn.child("type").getValue(String.class);
                        if ("gave".equals(type)) {
                            txn.getRef().child("dueDate").setValue(dueDate);
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (!shopId.isEmpty()) {
            DatabaseReference shopTxnRef = rootRef.child("Khatabook").child(userEmailPath)
                    .child("shops").child(shopId).child("transactions").child(phone);

            shopTxnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot txn : snapshot.getChildren()) {
                            String type = txn.child("type").getValue(String.class);
                            if ("gave".equals(type)) {
                                txn.getRef().child("dueDate").setValue(dueDate);
                            }
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCollectionData();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.cancelAll();
    }

    private class CollectionPagerAdapter extends FragmentStateAdapter {
        public CollectionPagerAdapter() {
            super(CollectionActivity.this);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return CollectionFragment.newInstance(duePaymentsList);
                case 1:
                    return CollectionFragment.newInstance(todayList);
                case 2:
                    return CollectionFragment.newInstance(incomingList);
                default:
                    return CollectionFragment.newInstance(duePaymentsList);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }}



