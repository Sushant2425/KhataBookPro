package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Customer;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_SEND_SMS = 555;
    private Transaction pendingTransactionForSms;

    private TextInputEditText etDate, etAmount, etNote;
    private AutoCompleteTextView autoCustomer;
    private TextInputLayout layoutCustomer;
    private MaterialButtonToggleGroup toggleButtonGroup;
    private MaterialButton btnSave;
    private MaterialCheckBox checkboxSendMessage;

    private PrefManager prefManager;
    private DatabaseReference transactionsRef;
    private List<Customer> customerList = new ArrayList<>();
    private List<String> customerNames = new ArrayList<>();
    private ArrayAdapter<String> customerAdapter;

    private Transaction editTransaction;

    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();
        initFirebase();
        loadCustomers();
        handleIntent();

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void initViews() {
        etDate = findViewById(R.id.et_date);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        autoCustomer = findViewById(R.id.auto_customer);
        layoutCustomer = findViewById(R.id.layout_customer);
        toggleButtonGroup = findViewById(R.id.toggle_button_group);
        btnSave = findViewById(R.id.btn_save);
        checkboxSendMessage = findViewById(R.id.checkbox_send_message);
        checkboxSendMessage.setChecked(true);

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setText(dateFormat.format(calendar.getTime()));

        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerNames);
        autoCustomer.setAdapter(customerAdapter);
        autoCustomer.setThreshold(1);

        autoCustomer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                // Logic to get phone from name can be improved or changed if needed
            }
        });

        autoCustomer.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            // Logic to get phone from name
        });
    }

    private void initFirebase() {
        prefManager = new PrefManager(this);
        String userEmail = prefManager.getUserEmail();
        String userNode = userEmail.replace(".", ",");
        transactionsRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(userNode)
                .child("transactions");
    }

    private void loadCustomers() {
        String userEmail = prefManager.getUserEmail();
        String userNode = userEmail.replace(".", ",");
        FirebaseDatabase.getInstance().getReference("Khatabook")
                .child(userNode)
                .child("customers")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        customerList.clear();
                        customerNames.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Customer c = ds.getValue(Customer.class);
                            if (c != null) {
                                customerList.add(c);
                                customerNames.add(c.getName() + " (" + c.getPhone() + ")");
                            }
                        }
                        customerAdapter.notifyDataSetChanged();
                        // If editing, set the customer after the list is loaded
                        if (editTransaction != null) {
                            autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddTransactionActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void handleIntent() {
        editTransaction = (Transaction) getIntent().getSerializableExtra("EDIT_TRANSACTION");
        if (editTransaction != null) {
            getSupportActionBar().setTitle("Edit Transaction");
            btnSave.setText("Update");

            etAmount.setText(String.valueOf(editTransaction.getAmount()));
            etNote.setText(editTransaction.getNote());
            etDate.setText(editTransaction.getDate());

            autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
            autoCustomer.setEnabled(false); // Don't allow changing customer when editing

            if ("gave".equals(editTransaction.getType())) {
                toggleButtonGroup.check(R.id.btn_gave);
            } else {
                toggleButtonGroup.check(R.id.btn_got);
            }
        } else {
            getSupportActionBar().setTitle("Add Transaction");
        }
    }


    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String date = etDate.getText().toString();
        String customerInfo = autoCustomer.getText().toString();


        if (amountStr.isEmpty()) {
            etAmount.setError("Enter amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }
        
        String selectedPhone;
        String selectedName;

        if (editTransaction != null) {
            selectedPhone = editTransaction.getCustomerPhone();
            selectedName = editTransaction.getCustomerName();
        } else {
             if (customerInfo.isEmpty() || !customerInfo.contains("(")) {
                layoutCustomer.setError("Select a customer");
                return;
            }
            selectedName = customerInfo.substring(0, customerInfo.lastIndexOf(" (")).trim();
            selectedPhone = customerInfo.substring(customerInfo.lastIndexOf(" (") + 2, customerInfo.length() - 1);
        }


        boolean isGave = toggleButtonGroup.getCheckedButtonId() == R.id.btn_gave;

        Transaction transaction = new Transaction();
        String idToSave = editTransaction != null ? editTransaction.getId() : transactionsRef.child(selectedPhone).push().getKey();
        
        transaction.setId(idToSave);
        transaction.setCustomerPhone(selectedPhone);
        transaction.setCustomerName(selectedName);
        transaction.setAmount(amount);
        transaction.setType(isGave ? "gave" : "got");
        transaction.setNote(note);
        transaction.setDate(date);
        transaction.setTimestamp(System.currentTimeMillis());

        transactionsRef.child(selectedPhone).child(idToSave).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    finishWithSuccess(editTransaction != null ? "Updated" : "Saved");
                    if (checkboxSendMessage.isChecked()) {
                        checkSmsPermissionAndSend(transaction);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    private void checkSmsPermissionAndSend(Transaction transaction){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            pendingTransactionForSms = transaction;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
        } else {
            sendSms(transaction);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (pendingTransactionForSms != null){
                    sendSms(pendingTransactionForSms);
                    pendingTransactionForSms = null;
                }
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSms(Transaction transaction){
        String message = "Hello " + transaction.getCustomerName() + ",\n"
                + "Transaction update: you " + (transaction.getType().equals("gave") ? "received" : "gave") + " ₹"
                + String.format(Locale.getDefault(), "%.2f", transaction.getAmount())
                + " on " + transaction.getDate() + ".\nNote: "
                + (transaction.getNote().isEmpty() ? "No notes." : transaction.getNote())
                + "\nThank you for using MyKhataPro.";
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(transaction.getCustomerPhone(), null, message, null, null);
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(this, "Failed to send SMS: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void finishWithSuccess(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}