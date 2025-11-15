package com.sandhyasofttechh.mykhatapro.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class EditTransactionActivity extends AppCompatActivity {

    private Transaction transaction;

    private LinearLayout headerView;
    private TextView tvTransactionTypeHeader, tvCustomerNameHeader;
    private TextInputEditText etAmount, etNote, etDate;
    private MaterialButton btnSaveChanges;

    private String initialAmount;
    private String initialNote;
    private String initialDate;

    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transaction = (Transaction) getIntent().getSerializableExtra("EDIT_TRANSACTION");
        if (transaction == null) {
            Toast.makeText(this, "Error: Transaction data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        populateData();
        setupClickListeners();
    }

    private void initViews() {
        headerView = findViewById(R.id.header_view_edit);
        tvTransactionTypeHeader = findViewById(R.id.tv_transaction_type_header_edit);
        tvCustomerNameHeader = findViewById(R.id.tv_customer_name_header_edit);
        etAmount = findViewById(R.id.et_amount_edit);
        etNote = findViewById(R.id.et_note_edit);
        etDate = findViewById(R.id.et_date_edit);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
    }

    private void populateData() {
        initialAmount = String.format(Locale.getDefault(), "%.2f", transaction.getAmount());
        initialNote = transaction.getNote();
        initialDate = transaction.getDate();

        etAmount.setText(initialAmount);
        etNote.setText(initialNote);
        etDate.setText(initialDate);

        if ("gave".equals(transaction.getType())) {
            headerView.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
            tvTransactionTypeHeader.setText("You Gave");
            tvCustomerNameHeader.setText("to " + transaction.getCustomerName());
        } else {
            headerView.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            tvTransactionTypeHeader.setText("You Got");
            tvCustomerNameHeader.setText("from " + transaction.getCustomerName());
        }

        try {
            calendar.setTime(Objects.requireNonNull(dateFormat.parse(transaction.getDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        etAmount.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void setupClickListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    etDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean hasChanges() {
        String currentAmount = Objects.requireNonNull(etAmount.getText()).toString();
        String currentNote = Objects.requireNonNull(etNote.getText()).toString();
        String currentDate = Objects.requireNonNull(etDate.getText()).toString();
        return !currentAmount.equals(initialAmount) || !currentNote.equals(initialNote) || !currentDate.equals(initialDate);
    }

    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Discard changes?")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Keep Editing", null)
                .show();
    }

    private void saveChanges() {
        String amountStr = Objects.requireNonNull(etAmount.getText()).toString().trim();
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount cannot be empty");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }

        transaction.setAmount(amount);
        transaction.setNote(Objects.requireNonNull(etNote.getText()).toString().trim());
        transaction.setDate(Objects.requireNonNull(etDate.getText()).toString());
        transaction.setTimestamp(System.currentTimeMillis());

        PrefManager prefManager = new PrefManager(this);
        String userNode = prefManager.getUserEmail().replace(".", ",");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Khatabook")
                .child(userNode)
                .child("transactions")
                .child(transaction.getCustomerPhone())
                .child(transaction.getId());

        dbRef.setValue(transaction).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (hasChanges()) {
                showDiscardChangesDialog();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasChanges()) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }
}