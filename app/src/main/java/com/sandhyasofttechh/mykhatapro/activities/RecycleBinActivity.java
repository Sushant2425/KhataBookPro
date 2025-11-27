//package com.sandhyasofttechh.mykhatapro.activities;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.SearchView;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.dialog.MaterialAlertDialogBuilder;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.adapter.RecycleBinAdapter;
//import com.sandhyasofttechh.mykhatapro.model.Transaction;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class RecycleBinActivity extends AppCompatActivity {
//
//    private MaterialToolbar toolbar;
//    private SearchView searchView;
//    private RecyclerView recyclerView;
//    private RecycleBinAdapter adapter;
//
//    private List<Transaction> allDeletedTransactions = new ArrayList<>();
//    private List<Transaction> filteredTransactions = new ArrayList<>();
//
//    private static final long AUTO_DELETE_DURATION = 45L * 24 * 60 * 60 * 1000; // 45 days in ms
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_recycle_bin);
//
//        toolbar = findViewById(R.id.toolbar_recycle_bin);
//        searchView = findViewById(R.id.search_view_recycle_bin);
//        recyclerView = findViewById(R.id.recycler_recycle_bin);
//
//        toolbar.setNavigationOnClickListener(v -> finish());
//
//        setupSearchViewVisuals();
//        setupRecyclerView();
//
//        loadDeletedTransactions();
//        setupSearch();
//    }
//
//    private void setupSearchViewVisuals() {
//        searchView.setIconifiedByDefault(false);
//        searchView.setQueryHint("Search transactions...");
//
//        int textId = androidx.appcompat.R.id.search_src_text;
//        EditText searchEditText = searchView.findViewById(textId);
//
//        searchEditText.setHint("Search transactions...");
//        searchEditText.setHintTextColor(Color.GRAY);
//        searchEditText.setTextColor(Color.BLACK);
//        searchEditText.setTextSize(16);
//
//        int plateId = androidx.appcompat.R.id.search_plate;
//        searchView.findViewById(plateId).setBackgroundColor(Color.TRANSPARENT);
//    }
//
//    private void setupRecyclerView() {
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        adapter = new RecycleBinAdapter(filteredTransactions,
//                new RecycleBinAdapter.OnTransactionActionListener() {
//
//                    @Override
//                    public void onUndoClicked(Transaction transaction) {
//                        confirmRestore(transaction);
//                    }
//
//                    @Override
//                    public void onDeleteClicked(Transaction transaction) {
//                        confirmPermanentDelete(transaction);
//                    }
//                });
//
//        recyclerView.setAdapter(adapter);
//    }
//
//    // -------------------- AUTO DELETE + LOAD --------------------
//    private void loadDeletedTransactions() {
//        PrefManager prefManager = new PrefManager(this);
//        String userNode = prefManager.getUserEmail().replace(".", ",");
//
//        DatabaseReference recycleBinRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(userNode)
//                .child("RecycleBin");
//
//        recycleBinRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                allDeletedTransactions.clear();
//
//                long currentTime = System.currentTimeMillis();
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Transaction transaction = ds.getValue(Transaction.class);
//
//                    if (transaction == null) continue;
//
//                    long deletedAt = transaction.getDeletedAt();
//
//                    // AUTO DELETE CHECK
//                    if (deletedAt > 0 && (currentTime - deletedAt > AUTO_DELETE_DURATION)) {
//                        // delete item from firebase permanently
//                        ds.getRef().removeValue();
//                        continue;
//                    }
//
//                    allDeletedTransactions.add(transaction);
//                }
//
//                filteredTransactions.clear();
//                filteredTransactions.addAll(allDeletedTransactions);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(RecycleBinActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void setupSearch() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override public boolean onQueryTextSubmit(String query) { return false; }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterTransactions(newText);
//                return true;
//            }
//        });
//    }
//
//    private void filterTransactions(String query) {
//        filteredTransactions.clear();
//
//        if (query.isEmpty()) {
//            filteredTransactions.addAll(allDeletedTransactions);
//        } else {
//            String lower = query.toLowerCase();
//
//            for (Transaction t : allDeletedTransactions) {
//                if ((t.getCustomerName() != null &&
//                        t.getCustomerName().toLowerCase().contains(lower))
//                        ||
//                        (t.getNote() != null &&
//                                t.getNote().toLowerCase().contains(lower))) {
//
//                    filteredTransactions.add(t);
//                }
//            }
//        }
//
//        adapter.notifyDataSetChanged();
//    }
//
//    // CONFIRMATION DIALOGS
//    private void confirmRestore(Transaction transaction) {
//        new MaterialAlertDialogBuilder(this)
//                .setTitle("Restore Transaction")
//                .setMessage("Are you sure you want to restore this transaction?")
//                .setPositiveButton("Restore", (dialog, which) -> restoreTransaction(transaction))
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void confirmPermanentDelete(Transaction transaction) {
//        new MaterialAlertDialogBuilder(this)
//                .setTitle("Delete Permanently")
//                .setMessage("This transaction will be deleted permanently.\nThis action cannot be undone.")
//                .setPositiveButton("Delete", (dialog, which) -> deletePermanently(transaction))
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    // RESTORE TRANSACTION
//    private void restoreTransaction(Transaction transaction) {
//        PrefManager prefManager = new PrefManager(this);
//        String userNode = prefManager.getUserEmail().replace(".", ",");
//
//        DatabaseReference transactionsRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(userNode)
//                .child("transactions")
//                .child(transaction.getCustomerPhone())
//                .child(transaction.getId());
//
//        DatabaseReference recycleBinRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(userNode)
//                .child("RecycleBin")
//                .child(transaction.getId());
//
//        transactionsRef.setValue(transaction).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                recycleBinRef.removeValue();
//                Toast.makeText(this, "Transaction restored.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // DELETE PERMANENTLY
//    private void deletePermanently(Transaction transaction) {
//        PrefManager prefManager = new PrefManager(this);
//        String userNode = prefManager.getUserEmail().replace(".", ",");
//
//        DatabaseReference recycleBinRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(userNode)
//                .child("RecycleBin")
//                .child(transaction.getId());
//
//        recycleBinRef.removeValue().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(this, "Deleted permanently.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}


package com.sandhyasofttechh.mykhatapro.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.RecycleBinAdapter;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class RecycleBinActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private SearchView searchView;
    private RecyclerView recyclerView;

    private RecycleBinAdapter adapter;

    private List<Transaction> allDeletedTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();

    private static final long AUTO_DELETE_DURATION = 45L * 24 * 60 * 60 * 1000; // 45 days in ms

    // MULTI SHOP PATH REFS
    private DatabaseReference recycleBinRef;
    private DatabaseReference transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        toolbar = findViewById(R.id.toolbar_recycle_bin);
        searchView = findViewById(R.id.search_view_recycle_bin);
        recyclerView = findViewById(R.id.recycler_recycle_bin);

        toolbar.setNavigationOnClickListener(v -> finish());

        setupPaths();
        setupSearchViewVisuals();
        setupRecyclerView();

        loadDeletedTransactions();
        setupSearch();
    }

    // ---------------------------------------------------------
    // ⭐ SETUP MULTI-SHOP FIREBASE PATHS
    // ---------------------------------------------------------
    private void setupPaths() {
        PrefManager pref = new PrefManager(this);

        String emailKey = pref.getUserEmail().replace(".", ",");
        String shopId = pref.getCurrentShopId();

        DatabaseReference baseRef;

        if (shopId == null || shopId.isEmpty()) {
            // NO SHOP SELECTED — OLD DEFAULT PATH
            baseRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey);
        } else {
            // SHOP SELECTED — NESTED PATH
            baseRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId);
        }

        recycleBinRef = baseRef.child("RecycleBin");
        transactionsRef = baseRef.child("transactions");
    }

    // ---------------------------------------------------------
    // UI Setup
    // ---------------------------------------------------------
    private void setupSearchViewVisuals() {
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search transactions...");

        int textId = androidx.appcompat.R.id.search_src_text;
        EditText searchEditText = searchView.findViewById(textId);

        searchEditText.setHint("Search transactions...");
        searchEditText.setHintTextColor(Color.GRAY);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setTextSize(16);

        int plateId = androidx.appcompat.R.id.search_plate;
        searchView.findViewById(plateId).setBackgroundColor(Color.TRANSPARENT);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecycleBinAdapter(
                filteredTransactions,
                new RecycleBinAdapter.OnTransactionActionListener() {
                    @Override
                    public void onUndoClicked(Transaction transaction) {
                        confirmRestore(transaction);
                    }

                    @Override
                    public void onDeleteClicked(Transaction transaction) {
                        confirmPermanentDelete(transaction);
                    }
                });

        recyclerView.setAdapter(adapter);
    }

    // ---------------------------------------------------------
    // LOAD RECYCLE BIN WITH AUTO DELETE
    // ---------------------------------------------------------
    private void loadDeletedTransactions() {

        recycleBinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allDeletedTransactions.clear();
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Transaction transaction = ds.getValue(Transaction.class);
                    if (transaction == null) continue;

                    long deletedAt = transaction.getDeletedAt();

                    // AUTO DELETE (45 days)
                    if (deletedAt > 0 && (currentTime - deletedAt > AUTO_DELETE_DURATION)) {
                        ds.getRef().removeValue(); // permanent delete
                        continue;
                    }

                    allDeletedTransactions.add(transaction);
                }

                filteredTransactions.clear();
                filteredTransactions.addAll(allDeletedTransactions);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecycleBinActivity.this,
                        "Failed: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTransactions(newText);
                return true;
            }
        });
    }

    private void filterTransactions(String query) {
        filteredTransactions.clear();

        if (query.isEmpty()) {
            filteredTransactions.addAll(allDeletedTransactions);
        } else {
            String q = query.toLowerCase();

            for (Transaction t : allDeletedTransactions) {

                if ((t.getCustomerName() != null &&
                        t.getCustomerName().toLowerCase().contains(q))
                        ||
                        (t.getNote() != null &&
                                t.getNote().toLowerCase().contains(q))) {

                    filteredTransactions.add(t);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ---------------------------------------------------------
    // CONFIRM RESTORE / DELETE
    // ---------------------------------------------------------
    private void confirmRestore(Transaction transaction) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Restore Transaction")
                .setMessage("Are you sure you want to restore this transaction?")
                .setPositiveButton("Restore", (dialog, which) -> restoreTransaction(transaction))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmPermanentDelete(Transaction transaction) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Permanently")
                .setMessage("This will permanently delete the transaction.\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePermanently(transaction))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------
    // RESTORE -> MOVE BACK TO transactions/
    // ---------------------------------------------------------
    private void restoreTransaction(Transaction transaction) {

        DatabaseReference activeRef = transactionsRef
                .child(transaction.getCustomerPhone())
                .child(transaction.getId());

        DatabaseReference recycleRef = recycleBinRef
                .child(transaction.getId());

        activeRef.setValue(transaction).addOnCompleteListener(t1 -> {
            if (t1.isSuccessful()) {
                recycleRef.removeValue();
                Toast.makeText(this, "Transaction restored.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // DELETE PERMANENTLY
    // ---------------------------------------------------------
    private void deletePermanently(Transaction transaction) {

        DatabaseReference recycleRef = recycleBinRef
                .child(transaction.getId());

        recycleRef.removeValue().addOnCompleteListener(t1 -> {
            if (t1.isSuccessful()) {
                Toast.makeText(this,
                        "Deleted permanently.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
