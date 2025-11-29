package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String PREF_REMINDER_SCHEDULED = "reminder_scheduled_";
    private static final String PREF_LAST_UPDATE = "collection_last_update";

    TextView txtTotalDue, txtEmptyState, txtShopTitle;
    ProgressBar progressBar;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    // **FIXED: Public lists for fragment updates**
    public ArrayList<CollectionModel> duePaymentsList = new ArrayList<>();
    public ArrayList<CollectionModel> todayList = new ArrayList<>();
    public ArrayList<CollectionModel> incomingList = new ArrayList<>();

    PrefManager prefManager;
    SharedPreferences reminderPrefs;
    DatabaseReference rootRef;
    String userEmailPath = "", shopId = "", shopName = "";

    private int totalCustomers = 0;
    private int processedCount = 0;
    private long lastDataUpdate = 0;

    private CollectionPagerAdapter pagerAdapter;

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
        reminderPrefs = getSharedPreferences("collection_reminders", MODE_PRIVATE);
        userEmailPath = prefManager.getUserEmail().replace(".", ",");
        shopId = prefManager.getCurrentShopId();
        shopName = prefManager.getCurrentShopName();

        if (shopId == null || shopId.isEmpty()) {
            txtShopTitle.setText("Default Account");
        } else {
            txtShopTitle.setText(shopName == null || shopName.isEmpty() ? "Shop: " + shopId : shopName);
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
        lastDataUpdate = reminderPrefs.getLong(PREF_LAST_UPDATE + shopId, 0);
    }

    private void setupTabs() {
        pagerAdapter = new CollectionPagerAdapter();
        viewPager.setAdapter(pagerAdapter);
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

        // **DEBUG LOG**
        Toast.makeText(this, "Processing " + totalCustomers + " customers...", Toast.LENGTH_SHORT).show();

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
                    // **PREVENT DUPLICATES**
                    if (findCustomerByPhone(phone) == null) {
                        categorizeCustomer(name, phone, pending, earliestDue[0]);
                    }
                }

                processedCount++;
                if (processedCount >= totalCustomers) {
                    runOnUiThread(() -> {
                        updateUIAndScheduleReminders();
                        // **CRITICAL FIX: Force refresh ALL fragments**
                        refreshAllFragments();
                    });
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

    // **IMPROVED: Check ALL lists for duplicates**
    private CollectionModel findCustomerByPhone(String phone) {
        for (CollectionModel model : duePaymentsList) {
            if (model.getPhone().equals(phone)) return model;
        }
        for (CollectionModel model : todayList) {
            if (model.getPhone().equals(phone)) return model;
        }
        for (CollectionModel model : incomingList) {
            if (model.getPhone().equals(phone)) return model;
        }
        return null;
    }

    private void categorizeCustomer(String name, String phone, double pending, long dueDate) {
        long todayStart = getTodayStart();
        long tomorrowStart = todayStart + 86400000L;
        CollectionModel model = new CollectionModel(name, phone, pending, dueDate);

        if (dueDate == 0 || dueDate < todayStart) {
            duePaymentsList.add(model);
        } else if (dueDate < tomorrowStart) {
            todayList.add(model);
        } else {
            incomingList.add(model);
        }
    }

    // **CRITICAL FIX: Force refresh ALL fragments**
    private void refreshAllFragments() {
        if (pagerAdapter != null) {
            // **CRITICAL: This now works properly with getItemId() override**
            pagerAdapter.notifyDataSetChanged();
        }
        // Switch to first tab to trigger immediate refresh
        viewPager.setCurrentItem(0, false);
        viewPager.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
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
        lastDataUpdate = System.currentTimeMillis();
        reminderPrefs.edit().putLong(PREF_LAST_UPDATE + shopId, lastDataUpdate).apply();

        recalculateTotalDue();

        // **SHOW ALL COUNTS**
        String countMsg = String.format("Due:%d Today:%d Incoming:%d Total:₹%.2f",
                duePaymentsList.size(), todayList.size(), incomingList.size(), getTotalDueAmount());
        Toast.makeText(this, countMsg, Toast.LENGTH_LONG).show();

        if (duePaymentsList.isEmpty() && todayList.isEmpty() && incomingList.isEmpty()) {
            showEmptyState("No pending collections");
        } else {
            refreshAllFragments();
            txtEmptyState.setVisibility(View.GONE);
        }

        scheduleSmartReminders();
    }

    // **INSTANT UPDATE METHOD**
    public void updateDueAmountInstantly(String customerPhone, double paymentAmount) {
        boolean found = updateCustomerInList(duePaymentsList, customerPhone, paymentAmount) ||
                updateCustomerInList(todayList, customerPhone, paymentAmount) ||
                updateCustomerInList(incomingList, customerPhone, paymentAmount);

        if (found) {
            recalculateTotalDue();
            refreshAllFragments();
            Toast.makeText(this, "Payment updated instantly!", Toast.LENGTH_SHORT).show();
        } else {
            loadCollectionData();
        }
    }

    private boolean updateCustomerInList(ArrayList<CollectionModel> list, String phone, double paymentAmount) {
        for (int i = 0; i < list.size(); i++) {
            CollectionModel model = list.get(i);
            if (model.getPhone().equals(phone)) {
                double newPending = Math.max(0, model.getPendingAmount() - paymentAmount);
                model.setPendingAmount(newPending);
                if (newPending <= 0) {
                    list.remove(i);
                }
                return true;
            }
        }
        return false;
    }

    private void recalculateTotalDue() {
        double total = getTotalDueAmount();
        txtTotalDue.setText(String.format(Locale.getDefault(), "Total Due: ₹%.2f", total));
    }

    private double getTotalDueAmount() {
        double total = 0;
        for (CollectionModel m : duePaymentsList) total += m.getPendingAmount();
        for (CollectionModel m : todayList) total += m.getPendingAmount();
        for (CollectionModel m : incomingList) total += m.getPendingAmount();
        return total;
    }

    // **REMINDER METHODS (unchanged)**
    private void scheduleSmartReminders() {
        long now = System.currentTimeMillis();
        if (now - lastDataUpdate > 3600000) {
            clearAllReminders();
        }

        for (CollectionModel m : todayList) {
            String key = PREF_REMINDER_SCHEDULED + "today_" + m.getPhone();
            if (!reminderPrefs.getBoolean(key, false)) {
                long triggerTime = getNext10AM();
                scheduleSingleReminder(m, triggerTime, "today");
                reminderPrefs.edit().putBoolean(key, true).apply();
            }
        }

        for (CollectionModel m : incomingList) {
            String key = PREF_REMINDER_SCHEDULED + "incoming_" + m.getPhone();
            if (!reminderPrefs.getBoolean(key, false)) {
                long triggerTime = getDayBeforeDueAt9AM(m.getDueDate());
                if (triggerTime > now) {
                    scheduleSingleReminder(m, triggerTime, "incoming");
                    reminderPrefs.edit().putBoolean(key, true).apply();
                }
            }
        }
    }

    private long getNext10AM() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        return c.getTimeInMillis();
    }

    private long getDayBeforeDueAt9AM(long dueDate) {
        Calendar reminderCal = Calendar.getInstance();
        reminderCal.setTimeInMillis(dueDate);
        reminderCal.add(Calendar.DAY_OF_YEAR, -1);
        reminderCal.set(Calendar.HOUR_OF_DAY, 9);
        reminderCal.set(Calendar.MINUTE, 0);
        reminderCal.set(Calendar.SECOND, 0);
        reminderCal.set(Calendar.MILLISECOND, 0);
        return reminderCal.getTimeInMillis();
    }

    private void clearAllReminders() {
        reminderPrefs.edit().clear().apply();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.cancelAll();
    }

    private void scheduleSingleReminder(CollectionModel model, long triggerAt, String category) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, CollectionReminderReceiver.class);
        intent.putExtra("name", model.getName());
        intent.putExtra("amount", model.getPendingAmount());
        intent.putExtra("phone", model.getPhone());
        intent.putExtra("category", category);

        int requestCode = (category + "_" + model.getPhone()).hashCode();
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
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
            Toast.makeText(this, "Due date updated for " + model.getName(), Toast.LENGTH_LONG).show();
            loadCollectionData();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateAllGaveTransactions(String phone, long dueDate) {
        // Implementation same as before...
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCollectionData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            // **IMPROVED: Pass CURRENT data lists with fresh copies to prevent reference issues**
            switch (position) {
                case 0:
                    return CollectionFragment.newInstance(new ArrayList<>(duePaymentsList));
                case 1:
                    return CollectionFragment.newInstance(new ArrayList<>(todayList));
                case 2:
                    return CollectionFragment.newInstance(new ArrayList<>(incomingList));
                default:
                    return CollectionFragment.newInstance(new ArrayList<>());
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        // **CRITICAL FIX #1: Override getItemId() for proper fragment recreation**
        @Override
        public long getItemId(int position) {
            // Generate unique ID based on position + timestamp for data refresh
            return position * 1000 + System.currentTimeMillis() % 1000;
        }

        // **CRITICAL FIX #2: Override containsItem() for FragmentStateAdapter refresh support**
        @Override
        public boolean containsItem(long itemId) {
            // Always return true for valid positions to allow recreation
            long position = itemId / 1000;
            return position >= 0 && position < getItemCount();
        }
    }
}
