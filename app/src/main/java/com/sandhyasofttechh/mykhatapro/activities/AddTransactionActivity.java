package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

    private TextInputEditText etDate, etAmount, etNote;
    private AutoCompleteTextView autoCustomer;
    private TextInputLayout layoutCustomer;
    private MaterialButtonToggleGroup toggleButtonGroup;
    private MaterialButton btnSave, btnAddCustomer;
    private MaterialCheckBox checkboxSendMessage;
    private Button btnAttachFile;
    private androidx.constraintlayout.widget.ConstraintLayout containerFields;
    private PrefManager prefManager;
    private DatabaseReference transactionsRef, customersRef;
    private List<Customer> customerList = new ArrayList<>();
    private List<String> customerNames = new ArrayList<>();
    private ArrayAdapter<String> customerAdapter;
    private Transaction editTransaction;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private static final int SMS_PERMISSION_CODE = 101;
    private static final int PICK_IMAGE_REQUEST_CODE = 201;
    private static final int PICK_FILE_REQUEST_CODE = 202;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 300;
    private static final int REQUEST_CODE_PICK_CONTACT = 301;

    private Uri attachedFileUri = null;

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

        checkSmsPermission();

        autoCustomer.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCustomer = (String) parent.getItemAtPosition(position);
            // येथे तुम्ही तसेच प्रोसेस करू शकता, उदा. निवडलेला ग्राहक स्टोअर करणे
        });
        autoCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                customerAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            containerFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        etDate.setOnClickListener(v -> showDatePicker());

        layoutCustomer.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                openContactPicker();
            }
        });
        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerNames);
        autoCustomer.setAdapter(customerAdapter);
        autoCustomer.setThreshold(1);


        btnAttachFile.setOnClickListener(v -> showFilePickerOptions());
        btnSave.setOnClickListener(v -> saveTransaction());
        btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
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
        btnAttachFile = findViewById(R.id.btn_attach_file);
        containerFields = findViewById(R.id.container_fields);
        btnAddCustomer = findViewById(R.id.btn_add_customer);

        etDate.setText(dateFormat.format(calendar.getTime()));

        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerNames);
        autoCustomer.setAdapter(customerAdapter);
        autoCustomer.setThreshold(1);

        autoCustomer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {}
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
        customersRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(userNode)
                .child("customers");
    }

    private void loadCustomers() {
        customersRef.addValueEventListener(new ValueEventListener() {
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

                if (editTransaction != null) {
                    autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTransactionActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---- ADD CUSTOMER FUNCTIONALITY ----
    private void showAddCustomerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_customer, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_dialog_customer_name);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_dialog_customer_phone);

        new AlertDialog.Builder(this)
                .setTitle("Add Customer")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addCustomerToFirebase(name, phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCustomerToFirebase(String name, String phone) {
        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setPhone(phone);
        newCustomer.setEmail("");
        customersRef.child(phone).setValue(newCustomer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show();
                    customerList.add(newCustomer);
                    customerNames.add(name + " (" + phone + ")");
                    customerAdapter.notifyDataSetChanged();
                    autoCustomer.setText(name + " (" + phone + ")");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add customer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    // ---- END ADD CUSTOMER FUNCTIONALITY ----

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    etDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void handleIntent() {
        editTransaction = (Transaction) getIntent().getSerializableExtra("EDIT_TRANSACTION");
        String prefillCustomerPhone = getIntent().getStringExtra("edit_customer_phone");
        String prefillCustomerName = getIntent().getStringExtra("edit_customer_name");

        if (editTransaction != null) {
            getSupportActionBar().setTitle("Edit Transaction");
            btnSave.setText("Update");
            etDate.setText(editTransaction.getDate());
            etAmount.setText(String.valueOf(editTransaction.getAmount()));
            etNote.setText(editTransaction.getNote());
            autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
            autoCustomer.setEnabled(false);
            if ("gave".equals(editTransaction.getType())) {
                toggleButtonGroup.check(R.id.btn_gave);
            } else {
                toggleButtonGroup.check(R.id.btn_got);
            }
            containerFields.setVisibility(View.VISIBLE);
        } else if (prefillCustomerPhone != null && prefillCustomerName != null) {
            getSupportActionBar().setTitle("Add Transaction");
            btnSave.setText("Save Transaction");
            autoCustomer.setText(prefillCustomerName + " (" + prefillCustomerPhone + ")");
            containerFields.setVisibility(View.GONE);
        } else {
            getSupportActionBar().setTitle("Add Transaction");
            btnSave.setText("Save Transaction");
            containerFields.setVisibility(View.GONE);
            autoCustomer.setEnabled(true);
        }
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String date = etDate.getText().toString();
        String customerInfo = autoCustomer.getText().toString();
        boolean sendSms = checkboxSendMessage.isChecked();

        if (toggleButtonGroup.getCheckedButtonId() == -1) {
            Toast.makeText(this, "Please select 'You Give' or 'You Get'", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
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
            if (TextUtils.isEmpty(customerInfo) || !customerInfo.contains("(")) {
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
                    if (sendSms) {
                        calculateBalanceAndSendSms(selectedPhone, selectedName, amount, isGave, date);
                    }
                    Intent intent = new Intent(this, TransactionSuccessActivity.class);
                    intent.putExtra(TransactionSuccessActivity.EXTRA_AMOUNT, amount);
                    intent.putExtra(TransactionSuccessActivity.EXTRA_CUSTOMER, selectedName);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. SMS will not be sent.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_PICK_CONTACT) {
                Uri contactUri = data.getData();
                try (Cursor cursor = getContentResolver().query(contactUri,
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                        null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String name = cursor.getString(0);
                        String number = cursor.getString(1);
                        number = number.replaceAll("[^0-9+]", "");
                        String finalNumber = number;
                        new AlertDialog.Builder(this)
                                .setTitle("Add Customer")
                                .setMessage("Add " + name + " (" + number + ") as a customer?")
                                .setPositiveButton("Yes", (dialog, which) -> addCustomerToFirebase(name, finalNumber))
                                .setNegativeButton("No", null)
                                .show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error reading contact", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void calculateBalanceAndSendSms(String phone, String name, double amount, boolean isGave, String date) {
        transactionsRef.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalGave = 0;
                double totalGot = 0;
                for (DataSnapshot txSnapshot : snapshot.getChildren()) {
                    Transaction tx = txSnapshot.getValue(Transaction.class);
                    if (tx != null) {
                        if ("gave".equals(tx.getType())) {
                            totalGave += tx.getAmount();
                        } else if ("got".equals(tx.getType())) {
                            totalGot += tx.getAmount();
                        }
                    }
                }
                double balance = totalGot - totalGave;
                String txnType = isGave ? "debit" : "credit";
                String msg = "Dear " + name + ", your account has been " + txnType + "ed by ₹" + amount +
                        " on " + date + ". Current balance: ₹" + balance + ". - MyKhataPro";
                sendSms(phone, msg);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTransactionActivity.this, "Failed to fetch transactions for balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSms(String phone, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(msg);
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showFilePickerOptions() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else if (which == 1) openGallery();
                })
                .show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST_CODE);
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, PICK_FILE_REQUEST_CODE);
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