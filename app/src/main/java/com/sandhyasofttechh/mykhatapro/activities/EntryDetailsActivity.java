package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.fragments.DeleteConfirmationBottomSheet;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntryDetailsActivity extends AppCompatActivity implements DeleteConfirmationBottomSheet.DeleteConfirmationListener {


    private Transaction transaction;
    private String customerName;
    private final SimpleDateFormat sdfWithTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);

        Toolbar toolbar = findViewById(R.id.toolbar_entry_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transaction = (Transaction) getIntent().getSerializableExtra("TRANSACTION_DATA");
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");

        if (transaction == null || customerName == null) {
            Toast.makeText(this, "Error: Data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (TextUtils.isEmpty(transaction.getCustomerName())) {
            transaction.setCustomerName(customerName);
        }

        populateViews();
        setupClickListeners();
    }

    private void populateViews() {
        TextView tvCustomerName = findViewById(R.id.tv_customer_name_receipt);
        TextView tvDate = findViewById(R.id.tv_date_receipt);
        TextView tvNote = findViewById(R.id.tv_note_receipt);
        TextView tvAmount = findViewById(R.id.tv_amount_receipt);
        TextView tvType = findViewById(R.id.tv_type_receipt);
        TextView tvSmsMessage = findViewById(R.id.tv_sms_message);
        
        tvCustomerName.setText(transaction.getCustomerName());
        
        Date date = new Date(transaction.getTimestamp());
        String formattedDate = sdfWithTime.format(date);
        tvDate.setText(formattedDate);

        if (TextUtils.isEmpty(transaction.getNote())) {
            tvNote.setVisibility(View.GONE);
        } else {
            tvNote.setVisibility(View.VISIBLE);
            tvNote.setText("Note: " + transaction.getNote());
        }

        String formattedAmount = String.format(Locale.getDefault(), "₹%,.2f", transaction.getAmount());
        tvAmount.setText(formattedAmount);

        boolean isGave = "gave".equals(transaction.getType());
        if (isGave) {
            tvType.setText("YOU GAVE");
            tvType.setTextColor(ContextCompat.getColor(this, R.color.error));
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.error));
        } else {
            tvType.setText("YOU GOT");
            tvType.setTextColor(ContextCompat.getColor(this, R.color.green));
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.green));
        }

        // Updated SMS Message with more details
        String smsText = "Hello " + transaction.getCustomerName() + ",\n" +
                "A transaction of " + formattedAmount + " (" + (isGave ? "You Gave" : "You Got") + ") " +
                "was recorded on " + formattedDate + ".\n\n" +
                "Sent by " + getString(R.string.app_name) + ".";
        tvSmsMessage.setText(smsText);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_edit_entry_receipt).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTransactionActivity.class);
            intent.putExtra("EDIT_TRANSACTION", transaction);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_delete_entry).setOnClickListener(v -> {
            DeleteConfirmationBottomSheet bottomSheet = new DeleteConfirmationBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        findViewById(R.id.btn_share_entry).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = "Transaction Details:\n" +
                    "Customer: " + transaction.getCustomerName() + "\n" +
                    "Amount: ₹" + transaction.getAmount() + "\n" +
                    "Date: " + sdfWithTime.format(new Date(transaction.getTimestamp())) + "\n" +
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