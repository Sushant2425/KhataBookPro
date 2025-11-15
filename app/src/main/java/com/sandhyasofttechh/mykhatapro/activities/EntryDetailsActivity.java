package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.fragments.DeleteConfirmationBottomSheet;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EntryDetailsActivity extends AppCompatActivity implements DeleteConfirmationBottomSheet.DeleteConfirmationListener {

    private Transaction transaction;
    private String customerName;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);

        Toolbar toolbar = findViewById(R.id.toolbar_entry_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Entry Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transaction = (Transaction) getIntent().getSerializableExtra("TRANSACTION_DATA");
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");

        if (transaction == null || customerName == null) {
            Toast.makeText(this, "Error: Data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateViews();
        setupClickListeners();
    }

    private void populateViews() {
        TextView tvCustomerName = findViewById(R.id.tv_customer_name_entry);
        TextView tvAmount = findViewById(R.id.tv_amount_entry);
        TextView tvDate = findViewById(R.id.tv_date_entry);
        TextView tvDaysAgo = findViewById(R.id.tv_days_ago_entry);
        TextView tvNote = findViewById(R.id.tv_note_entry);

        tvCustomerName.setText(customerName);
        tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", transaction.getAmount()));
        tvDate.setText(transaction.getDate());
        tvDaysAgo.setText(getCustomRelativeTime(transaction.getDate())); // Use the improved method
        tvNote.setText(transaction.getNote());
    }

    private String getCustomRelativeTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            Date date = sdf.parse(dateString);
            if (date == null) return "";
            if (DateUtils.isToday(date.getTime())) return "(Today)";
            long now = System.currentTimeMillis();
            long diff = now - date.getTime();
            if(diff < 0) return "";
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days < 7) return "(" + days + (days == 1 ? " day ago)" : " days ago)");
            long weeks = days / 7;
            if (weeks < 5) return "(" + weeks + (weeks == 1 ? " week ago)" : " weeks ago)");
            Calendar start = Calendar.getInstance();
            start.setTime(date);
            Calendar end = Calendar.getInstance();
            int monthDiff = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 + (end.get(Calendar.MONTH) - start.get(Calendar.MONTH));
            if (monthDiff < 12) return "(" + monthDiff + (monthDiff == 1 ? " month ago)" : " months ago)");
            int yearDiff = monthDiff / 12;
            return "(" + yearDiff + (yearDiff == 1 ? " year ago)" : " years ago)");
        } catch (ParseException e) {
            Log.e("EntryDetailsActivity", "Date parsing error", e);
            return "";
        }
    }


    private void setupClickListeners() {
        Button btnEdit = findViewById(R.id.btn_edit_entry);
        Button btnDelete = findViewById(R.id.btn_delete_entry);
        Button btnShare = findViewById(R.id.btn_share_entry);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTransactionActivity.class);
            intent.putExtra("EDIT_TRANSACTION", transaction);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            DeleteConfirmationBottomSheet bottomSheet = new DeleteConfirmationBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = "Transaction Details:\n" +
                    "Customer: " + customerName + "\n" +
                    "Amount: ₹" + transaction.getAmount() + "\n" +
                    "Date: " + transaction.getDate() + "\n" +
                    "Note: " + transaction.getNote();
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, "Share via"));
        });
    }

    private void deleteTransaction() {
        PrefManager prefManager = new PrefManager(this);
        String userNode = prefManager.getUserEmail().replace(".", ",");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Khatabook")
                .child(userNode)
                .child("transactions")
                .child(transaction.getCustomerPhone())
                .child(transaction.getId());

        dbRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteConfirmed() {
        deleteTransaction();
    }
}