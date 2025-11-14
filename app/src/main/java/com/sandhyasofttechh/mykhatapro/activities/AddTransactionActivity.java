package com.sandhyasofttechh.mykhatapro.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etDate, etAmount, etNote;
    private AutoCompleteTextView autoCustomer;
    private TextInputLayout layoutCustomer;
    private RadioGroup rgType;
    private MaterialButton btnSave;

    private PrefManager prefManager;
    private DatabaseReference transactionsRef;
    private List<Customer> customerList = new ArrayList<>();
    private List<String> customerNames = new ArrayList<>();
    private ArrayAdapter<String> customerAdapter;

    private String editTransactionId = null;
    private String selectedCustomerPhone = null;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Transaction");
        }

        initViews();
        initFirebase();
        loadCustomers();

        // Handle edit mode
        handleIntent();

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void initViews() {
        etDate = findViewById(R.id.et_date);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        autoCustomer = findViewById(R.id.auto_customer);
        layoutCustomer = findViewById(R.id.layout_customer);
        rgType = findViewById(R.id.rg_type);
        btnSave = findViewById(R.id.btn_save);

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setText(dateFormat.format(calendar.getTime()));

        // Customer dropdown
        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerNames);
        autoCustomer.setAdapter(customerAdapter);
        autoCustomer.setThreshold(1);

        autoCustomer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                selectedCustomerPhone = getPhoneFromName(s.toString());
            }
        });

        autoCustomer.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            selectedCustomerPhone = getPhoneFromName(name);
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
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddTransactionActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            etDate.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void handleIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editTransactionId = extras.getString("edit_transaction_id");
            if (editTransactionId != null) {
                getSupportActionBar().setTitle("Edit Transaction");
                etAmount.setText(extras.getString("edit_amount"));
                etNote.setText(extras.getString("edit_note"));
                etDate.setText(extras.getString("edit_date"));
                selectedCustomerPhone = extras.getString("edit_customer_phone");
                autoCustomer.setText(extras.getString("edit_customer_name"));

                int type = extras.getString("edit_type", "gave").equals("gave") ? R.id.rb_gave : R.id.rb_got;
                rgType.check(type);
            }
        }
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String date = etDate.getText().toString();

        if (amountStr.isEmpty()) {
            etAmount.setError("Enter amount");
            return;
        }
        if (selectedCustomerPhone == null) {
            layoutCustomer.setError("Select a customer");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        boolean isGave = rgType.getCheckedRadioButtonId() == R.id.rb_gave;
        String customerName = getNameFromPhone(selectedCustomerPhone);

        Transaction transaction = new Transaction();
        // The 'id' field is no longer needed for the database structure, but we can keep it in the model
        transaction.setId(selectedCustomerPhone); 
        transaction.setCustomerPhone(selectedCustomerPhone);
        transaction.setCustomerName(customerName);
        transaction.setAmount(amount);
        transaction.setType(isGave ? "gave" : "got");
        transaction.setNote(note);
        transaction.setDate(date);
        transaction.setTimestamp(System.currentTimeMillis());
        
        // This will save the transaction object directly under the phone number.
        // It will overwrite any existing transaction for this customer.
        transactionsRef.child(selectedCustomerPhone).setValue(transaction)
                .addOnSuccessListener(a -> finishWithSuccess("Saved"))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String getPhoneFromName(String fullName) {
        for (Customer c : customerList) {
            String display = c.getName() + " (" + c.getPhone() + ")";
            if (display.equals(fullName)) {
                return c.getPhone();
            }
        }
        return null;
    }
    
    private String getNameFromPhone(String phone) {
        for (Customer c : customerList) {
            if (c.getPhone().equals(phone)) {
                return c.getName();
            }
        }
        return null;
    }

    private void finishWithSuccess(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}