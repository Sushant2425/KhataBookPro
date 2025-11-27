//package com.sandhyasofttechh.mykhatapro.activities;
//
//import android.Manifest;
//import android.app.DatePickerDialog;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.provider.ContactsContract;
//import android.provider.MediaStore;
//import android.telephony.SmsManager;
//import android.text.TextUtils;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.MenuItem;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.os.Bundle;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.button.MaterialButtonToggleGroup;
//import com.google.android.material.checkbox.MaterialCheckBox;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.model.Customer;
//import com.sandhyasofttechh.mykhatapro.model.Transaction;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import android.widget.Toast;
//import android.widget.LinearLayout;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//import java.util.Date;
//import java.util.concurrent.TimeUnit;
//import java.text.ParseException;
//
//import android.content.Context;
//import android.telephony.SmsManager;
//import android.util.Log;
//
//public class AddTransactionActivity extends AppCompatActivity {
//
//    private TextInputEditText etDate, etAmount, etNote;
//    private AutoCompleteTextView autoCustomer;
//    private TextInputLayout layoutCustomer;
//    private MaterialButtonToggleGroup toggleButtonGroup;
//    private MaterialButton btnSave, btnAddCustomer;
//    private MaterialCheckBox checkboxSendMessage;
//    private Button btnAttachFile;
//    private androidx.constraintlayout.widget.ConstraintLayout containerFields;
//    private PrefManager prefManager;
//    private DatabaseReference transactionsRef, customersRef;
//    private List<Customer> customerList = new ArrayList<>();
//    private List<String> customerNames = new ArrayList<>();
//    private ArrayAdapter<String> customerAdapter;
//    private Transaction editTransaction;
//    private final Calendar calendar = Calendar.getInstance();
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//
//    private static final int SMS_PERMISSION_CODE = 101;
//    private static final int PICK_IMAGE_REQUEST_CODE = 201;
//    private static final int PICK_FILE_REQUEST_CODE = 202;
//    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 300;
//    private static final int REQUEST_CODE_PICK_CONTACT = 301;
//
//    private Uri attachedFileUri = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_transaction);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
//
//        initViews();
//        initFirebase();
//        loadCustomers();
//        handleIntent();
//
//        checkSmsPermission();
//
//        autoCustomer.setOnItemClickListener((parent, view, position, id) -> { /* nothing extra here */ });
//        autoCustomer.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { customerAdapter.getFilter().filter(s); }
//            @Override public void afterTextChanged(Editable s) { }
//        });
//
//        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
//            containerFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
//        });
//
//        etDate.setOnClickListener(v -> showDatePicker());
//
//        layoutCustomer.setEndIconOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
//            } else {
//                openContactPicker();
//            }
//        });
//
//        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerNames);
//        autoCustomer.setAdapter(customerAdapter);
//        autoCustomer.setThreshold(1);
//
//        btnAttachFile.setOnClickListener(v -> showFilePickerOptions());
//        btnSave.setOnClickListener(v -> saveTransaction());
//        btnAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
//    }
//
//    private void initViews() {
//        etDate = findViewById(R.id.et_date);
//        etAmount = findViewById(R.id.et_amount);
//        etNote = findViewById(R.id.et_note);
//        autoCustomer = findViewById(R.id.auto_customer);
//        layoutCustomer = findViewById(R.id.layout_customer);
//        toggleButtonGroup = findViewById(R.id.toggle_button_group);
//        btnSave = findViewById(R.id.btn_save);
//        checkboxSendMessage = findViewById(R.id.checkbox_send_message);
//        btnAttachFile = findViewById(R.id.btn_attach_file);
//        containerFields = findViewById(R.id.container_fields);
//        btnAddCustomer = findViewById(R.id.btn_add_customer);
//
//        etDate.setText(dateFormat.format(calendar.getTime()));
//    }
//
//    private void initFirebase() {
//        prefManager = new PrefManager(this);
//        String userEmail = prefManager.getUserEmail();
//        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
//        transactionsRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(userNode)
//                .child("transactions");
//        customersRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(userNode)
//                .child("customers");
//    }
//
//    private void loadCustomers() {
//        customersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                customerList.clear();
//                customerNames.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Customer c = ds.getValue(Customer.class);
//                    if (c != null) {
//                        customerList.add(c);
//                        customerNames.add(c.getName() + " (" + c.getPhone() + ")");
//                    }
//                }
//                customerAdapter.notifyDataSetChanged();
//
//                if (editTransaction != null) {
//                    autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
//                }
//            }
//
//            @Override public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(AddTransactionActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // ---- ADD CUSTOMER FUNCTIONALITY ----
//    private void showAddCustomerDialog() {
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_customer, null);
//        TextInputEditText etName = dialogView.findViewById(R.id.et_dialog_customer_name);
//        TextInputEditText etPhone = dialogView.findViewById(R.id.et_dialog_customer_phone);
//
//        new AlertDialog.Builder(this)
//                .setTitle("Add Customer")
//                .setView(dialogView)
//                .setPositiveButton("Save", (dialog, which) -> {
//                    String name = etName.getText().toString().trim();
//                    String phone = etPhone.getText().toString().trim();
//                    if (name.isEmpty() || phone.isEmpty()) {
//                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    addCustomerToFirebase(name, phone);
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void addCustomerToFirebase(String name, String phone) {
//        Customer newCustomer = new Customer();
//        newCustomer.setName(name);
//        newCustomer.setPhone(phone);
//        newCustomer.setEmail("");
//        customersRef.child(phone).setValue(newCustomer)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show();
//                    customerList.add(newCustomer);
//                    customerNames.add(name + " (" + phone + ")");
//                    customerAdapter.notifyDataSetChanged();
//                    autoCustomer.setText(name + " (" + phone + ")");
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add customer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//    // ---- END ADD CUSTOMER FUNCTIONALITY ----
//
//    private void showDatePicker() {
//        new DatePickerDialog(this,
//                (view, year, month, day) -> {
//                    calendar.set(year, month, day);
//                    etDate.setText(dateFormat.format(calendar.getTime()));
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//        ).show();
//    }
//
//    private void handleIntent() {
//        editTransaction = (Transaction) getIntent().getSerializableExtra("EDIT_TRANSACTION");
//        String prefillCustomerPhone = getIntent().getStringExtra("edit_customer_phone");
//        String prefillCustomerName = getIntent().getStringExtra("edit_customer_name");
//
//        if (editTransaction != null) {
//            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Transaction");
//            btnSave.setText("Update");
//            etDate.setText(editTransaction.getDate());
//            etAmount.setText(String.valueOf(editTransaction.getAmount()));
//            etNote.setText(editTransaction.getNote());
//            autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
//            autoCustomer.setEnabled(false);
//            if ("gave".equalsIgnoreCase(editTransaction.getType())) {
//                toggleButtonGroup.check(R.id.btn_gave);
//            } else {
//                toggleButtonGroup.check(R.id.btn_got);
//            }
//            containerFields.setVisibility(View.VISIBLE);
//        } else if (prefillCustomerPhone != null && prefillCustomerName != null) {
//            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Transaction");
//            btnSave.setText("Save Transaction");
//            autoCustomer.setText(prefillCustomerName + " (" + prefillCustomerPhone + ")");
//            containerFields.setVisibility(View.GONE);
//        } else {
//            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Transaction");
//            btnSave.setText("Save Transaction");
//            containerFields.setVisibility(View.GONE);
//            autoCustomer.setEnabled(true);
//        }
//    }
//
//    private void saveTransaction() {
//        String amountStr = etAmount.getText().toString().trim();
//        String note = etNote.getText().toString().trim();
//        String date = etDate.getText().toString();
//        String customerInfo = autoCustomer.getText().toString();
//        boolean sendSms = checkboxSendMessage.isChecked();
//
//        if (toggleButtonGroup.getCheckedButtonId() == -1) {
//            Toast.makeText(this, "Please select 'You Give' or 'You Get'", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (TextUtils.isEmpty(amountStr)) {
//            etAmount.setError("Enter amount");
//            return;
//        }
//        double amount;
//        try {
//            amount = Double.parseDouble(amountStr);
//        } catch (NumberFormatException e) {
//            etAmount.setError("Invalid amount");
//            return;
//        }
//        String selectedPhone;
//        String selectedName;
//        if (editTransaction != null) {
//            selectedPhone = editTransaction.getCustomerPhone();
//            selectedName = editTransaction.getCustomerName();
//        } else {
//            if (TextUtils.isEmpty(customerInfo) || !customerInfo.contains("(")) {
//                layoutCustomer.setError("Select a customer");
//                return;
//            }
//            selectedName = customerInfo.substring(0, customerInfo.lastIndexOf(" (")).trim();
//            selectedPhone = customerInfo.substring(customerInfo.lastIndexOf(" (") + 2, customerInfo.length() - 1);
//        }
//        boolean isGave = toggleButtonGroup.getCheckedButtonId() == R.id.btn_gave;
//        Transaction transaction = new Transaction();
//        String idToSave = editTransaction != null ? editTransaction.getId() : transactionsRef.child(selectedPhone).push().getKey();
//        transaction.setId(idToSave);
//        transaction.setCustomerPhone(selectedPhone);
//        transaction.setCustomerName(selectedName);
//        transaction.setAmount(amount);
//        transaction.setType(isGave ? "gave" : "got");
//        transaction.setNote(note);
//        transaction.setDate(date);
//        transaction.setTimestamp(System.currentTimeMillis());
//
//        transactionsRef.child(selectedPhone).child(idToSave).setValue(transaction)
//                .addOnSuccessListener(aVoid -> {
//                    // After saving transaction, if sendSms is checked, generate + upload statement + send SMS with link
//                    if (sendSms) {
//                        // Start background task to generate PDF (bank-style), upload and send SMS with link
//                        new GenerateStatementAndSendSmsTask(this, selectedPhone, selectedName, amount, isGave, date).execute();
//                    } else {
//                        Intent intent = new Intent(this, TransactionSuccessActivity.class);
//                        intent.putExtra(TransactionSuccessActivity.EXTRA_AMOUNT, amount);
//                        intent.putExtra(TransactionSuccessActivity.EXTRA_CUSTOMER, selectedName);
//                        startActivity(intent);
//                        finish();
//                    }
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
//    }
//
//    private void checkSmsPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == SMS_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "SMS permission denied. SMS will not be sent.", Toast.LENGTH_SHORT).show();
//            }
//        } else if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openContactPicker();
//            } else {
//                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void openContactPicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
//        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK && data != null) {
//            if (requestCode == REQUEST_CODE_PICK_CONTACT) {
//                Uri contactUri = data.getData();
//                try (Cursor cursor = getContentResolver().query(contactUri,
//                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
//                        null, null, null)) {
//                    if (cursor != null && cursor.moveToFirst()) {
//                        String name = cursor.getString(0);
//                        String number = cursor.getString(1);
//                        number = number.replaceAll("[^0-9+]", "");
//                        String finalNumber = number;
//                        new AlertDialog.Builder(this)
//                                .setTitle("Add Customer")
//                                .setMessage("Add " + name + " (" + number + ") as a customer?")
//                                .setPositiveButton("Yes", (dialog, which) -> addCustomerToFirebase(name, finalNumber))
//                                .setNegativeButton("No", null)
//                                .show();
//                    }
//                } catch (Exception e) {
//                    Toast.makeText(this, "Error reading contact", Toast.LENGTH_SHORT).show();
//                }
//            } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
//                // handle camera response if needed
//            } else if (requestCode == PICK_FILE_REQUEST_CODE) {
//                // handle gallery file pick if needed
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
//
//    /**
//     * Background task: fetch transactions for customer, generate bank-style PDF, upload to Firebase, then send SMS with link
//     */
//    private static class GenerateStatementAndSendSmsTask extends android.os.AsyncTask<Void, Void, String> {
//
//        private final Context context;
//        private final String phone;
//        private final String name;
//        private final double amount;
//        private final boolean isDebit; // isGave
//        private final String date;
//        private ProgressDialog progress;
//
//        GenerateStatementAndSendSmsTask(Context ctx, String phone, String name, double amount, boolean isDebit, String date) {
//            this.context = ctx;
//            this.phone = phone;
//            this.name = name;
//            this.amount = amount;
//            this.isDebit = isDebit;
//            this.date = date;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            progress = new ProgressDialog(context);
//            progress.setMessage("Preparing statement and sending SMS...");
//            progress.setCancelable(false);
//            progress.show();
//        }
//
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            try {
//                // 1) Read all transactions for this customer synchronously
//                final List<Transaction> txList = new ArrayList<>();
//                final Object lock = new Object();
//                final boolean[] done = {false};
//
//                PrefManager pm = new PrefManager(context);
//                String userEmail = pm.getUserEmail();
//                String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
//
//                DatabaseReference txnRef = FirebaseDatabase.getInstance()
//                        .getReference("Khatabook")
//                        .child(userNode)
//                        .child("transactions")
//                        .child(phone);
//
//                txnRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot s : snapshot.getChildren()) {
//                            Transaction t = s.getValue(Transaction.class);
//                            if (t != null) txList.add(t);
//                        }
//                        synchronized (lock) {
//                            done[0] = true;
//                            lock.notifyAll();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        synchronized (lock) {
//                            done[0] = true;
//                            lock.notifyAll();
//                        }
//                    }
//                });
//
//                // wait up to 10s for Firebase read
//                synchronized (lock) {
//                    long waitTime = 0;
//                    while (!done[0] && waitTime < 10000) {
//                        lock.wait(300);
//                        waitTime += 300;
//                    }
//                }
//
//                // 2) Generate bank-style PDF
//                File pdfFile = createBankStylePdf(context, name, phone, txList);
//                if (pdfFile == null) return null;
//
//                // 3) Upload PDF to Firebase Storage
//                final String[] downloadUrl = {null};
//                final boolean[] uploadDone = {false};
//
//                String fileName = "statement_" + phone.replaceAll("[^\\d]", "") + "_" + System.currentTimeMillis() + ".pdf";
//
//                StorageReference storageRef = FirebaseStorage.getInstance()
//                        .getReference()
//                        .child("Statements/" + fileName);
//
//                storageRef.putFile(Uri.fromFile(pdfFile))
//                        .addOnSuccessListener(taskSnapshot -> {
//                            storageRef.getDownloadUrl()
//                                    .addOnSuccessListener(uri -> {
//
//                                        String firebaseUrl = uri.toString();
//
//                                        // ⭐ DIRECT PDF LINK — no Drive viewer
//                                        // Firebase requires ?alt=media for direct file access
//                                        if (!firebaseUrl.contains("alt=media")) {
//                                            if (firebaseUrl.contains("?")) {
//                                                firebaseUrl = firebaseUrl + "&alt=media";
//                                            } else {
//                                                firebaseUrl = firebaseUrl + "?alt=media";
//                                            }
//                                        }
//
//                                        // store final direct link
//                                        downloadUrl[0] = firebaseUrl;
//
//                                        uploadDone[0] = true;
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        uploadDone[0] = true;
//                                    });
//                        })
//                        .addOnFailureListener(e -> uploadDone[0] = true);
//
//                // wait for upload max 30 sec
//                int waited = 0;
//                while (!uploadDone[0] && waited < 30000) {
//                    Thread.sleep(300);
//                    waited += 300;
//                }
//
//                return downloadUrl[0];
//
//            } catch (Exception e) {
//                Log.e("GenSendStmtTask", "Error", e);
//                return null;
//            }
//        }
//
//        @Override
//
//        protected void onPostExecute(String link) {
//            if (progress != null && progress.isShowing()) progress.dismiss();
//
//            String txnType = isDebit ? "debit" : "credit";
//            String message;
//
//            if (link != null && !link.isEmpty()) {
//                message = "Dear " + name + ",\n" +
//                        "₹" + String.format(Locale.getDefault(), "%,.2f", amount) + " " + txnType +
//                        " recorded on " + date + ".\n" +
//                        "View full statement:\n" + link + "\n\n" +
//                        "- MyKhata Pro";
//            } else {
//                message = "Dear " + name + ",\n" +
//                        "₹" + String.format(Locale.getDefault(), "%,.2f", amount) + " " + txnType +
//                        " recorded on " + date + ".\n" +
//                        "View full statement:\n" + link + "\n\n" +
//                        "- MyKhata Pro";
//            }
//
//            try {
//                SmsManager smsManager = SmsManager.getDefault();
//                ArrayList<String> parts = smsManager.divideMessage(message);
//
//                // ये दो लाइन ऐड कर — सिर्फ यही चाहिए था तुझे!
//                ArrayList<String> formattedParts = new ArrayList<>();
//                for (String part : parts) {
//                    if (part.contains(link)) {
//                        // सिर्फ लिंक वाली लाइन को नीला + क्लिकेबल बनाने के लिए
//                        formattedParts.add(part.replace(link, "<u>" + link + "</u>"));
//                    } else {
//                        formattedParts.add(part);
//                    }
//                }
//
//                // नया तरीका — Android SMS में underline + blue link दिखेगा 100%
//                smsManager.sendMultipartTextMessage(
//                        phone,
//                        null,
//                        formattedParts,
//                        null,
//                        null
//                );
//
//                Toast.makeText(context, "SMS sent to " + name, Toast.LENGTH_LONG).show();
//
//            } catch (Exception e) {
//                Log.e("SendSMS", "Error", e);
//                Toast.makeText(context, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//
//            // Success screen
//            Intent intent = new Intent(context, TransactionSuccessActivity.class);
//            intent.putExtra(TransactionSuccessActivity.EXTRA_AMOUNT, amount);
//            intent.putExtra(TransactionSuccessActivity.EXTRA_CUSTOMER, name);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//
//            if (context instanceof AddTransactionActivity) {
//                ((AddTransactionActivity) context).finish();
//            }
//        }
//        private File createBankStylePdf(Context context, String customerName, String customerPhone, List<Transaction> transactions) {
//            try {
//                File folder = new File(context.getExternalFilesDir(null), "MyKhataPro/Statements");
//                if (!folder.exists()) folder.mkdirs();
//
//                File pdfFile = new File(folder, "statement_" + System.currentTimeMillis() + ".pdf");
//
//                android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
//                int pageWidth = 595;
//                int pageHeight = 842;
//                int pageNumber = 1;
//
//                int marginLeft = 40;
//                int marginRight = 40;
//                int tableWidth = pageWidth - marginLeft - marginRight;
//
//                // Column positions
//                int col1X = marginLeft;
//                int col2X = marginLeft + 115;
//                int col3X = marginLeft + 245;
//                int col4X = marginLeft + 375;
//                int tableEndX = pageWidth - marginRight;
//
//                int rowHeight = 30;
//
//                // ============ TESTED COLOR PALETTE ============
//                final int COLOR_PRIMARY = android.graphics.Color.parseColor("#1976D2");
//                final int COLOR_PRIMARY_LIGHT = android.graphics.Color.parseColor("#E3F2FD");
//                final int COLOR_DEBIT = android.graphics.Color.parseColor("#E53935");
//                final int COLOR_CREDIT = android.graphics.Color.parseColor("#43A047");
//                final int COLOR_DEBIT_BG = android.graphics.Color.parseColor("#FFEBEE");
//                final int COLOR_CREDIT_BG = android.graphics.Color.parseColor("#E8F5E9");
//                final int COLOR_TEXT_PRIMARY = android.graphics.Color.parseColor("#212121");
//                final int COLOR_TEXT_SECONDARY = android.graphics.Color.parseColor("#757575");
//                final int COLOR_BORDER = android.graphics.Color.parseColor("#BDBDBD");
//                final int COLOR_BG_LIGHT = android.graphics.Color.parseColor("#FAFAFA");
//                final int COLOR_BG_INFO = android.graphics.Color.parseColor("#F5F5F5");
//
//                // ============ PAINT OBJECTS ============
//
//                android.graphics.Paint borderPaint = new android.graphics.Paint();
//                borderPaint.setColor(COLOR_BORDER);
//                borderPaint.setStrokeWidth(1f);
//                borderPaint.setStyle(android.graphics.Paint.Style.STROKE);
//                borderPaint.setAntiAlias(true);
//
//                android.graphics.Paint primaryBgPaint = new android.graphics.Paint();
//                primaryBgPaint.setColor(COLOR_PRIMARY);
//                primaryBgPaint.setStyle(android.graphics.Paint.Style.FILL);
//                primaryBgPaint.setAntiAlias(true);
//
//                android.graphics.Paint lightBgPaint = new android.graphics.Paint();
//                lightBgPaint.setColor(COLOR_BG_LIGHT);
//                lightBgPaint.setStyle(android.graphics.Paint.Style.FILL);
//                lightBgPaint.setAntiAlias(true);
//
//                android.graphics.Paint infoBgPaint = new android.graphics.Paint();
//                infoBgPaint.setColor(COLOR_BG_INFO);
//                infoBgPaint.setStyle(android.graphics.Paint.Style.FILL);
//                infoBgPaint.setAntiAlias(true);
//
//                android.graphics.Paint debitBgPaint = new android.graphics.Paint();
//                debitBgPaint.setColor(COLOR_DEBIT_BG);
//                debitBgPaint.setStyle(android.graphics.Paint.Style.FILL);
//                debitBgPaint.setAntiAlias(true);
//
//                android.graphics.Paint creditBgPaint = new android.graphics.Paint();
//                creditBgPaint.setColor(COLOR_CREDIT_BG);
//                creditBgPaint.setStyle(android.graphics.Paint.Style.FILL);
//                creditBgPaint.setAntiAlias(true);
//
//                android.graphics.Paint summaryBgPaint = new android.graphics.Paint();
//                summaryBgPaint.setColor(COLOR_PRIMARY_LIGHT);
//                summaryBgPaint.setStyle(android.graphics.Paint.Style.FILL);
//                summaryBgPaint.setAntiAlias(true);
//
//                // Text Paints
//                android.graphics.Paint whiteTitlePaint = new android.graphics.Paint();
//                whiteTitlePaint.setColor(android.graphics.Color.WHITE);
//                whiteTitlePaint.setTextSize(20);
//                whiteTitlePaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                whiteTitlePaint.setAntiAlias(true);
//
//                android.graphics.Paint whiteSubtitlePaint = new android.graphics.Paint();
//                whiteSubtitlePaint.setColor(android.graphics.Color.WHITE);
//                whiteSubtitlePaint.setTextSize(10);
//                whiteSubtitlePaint.setAntiAlias(true);
//
//                android.graphics.Paint whiteSmallPaint = new android.graphics.Paint();
//                whiteSmallPaint.setColor(android.graphics.Color.WHITE);
//                whiteSmallPaint.setTextSize(9);
//                whiteSmallPaint.setAntiAlias(true);
//
//                android.graphics.Paint headerTextPaint = new android.graphics.Paint();
//                headerTextPaint.setColor(android.graphics.Color.WHITE);
//                headerTextPaint.setTextSize(10);
//                headerTextPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                headerTextPaint.setAntiAlias(true);
//
//                android.graphics.Paint titlePaint = new android.graphics.Paint();
//                titlePaint.setColor(COLOR_TEXT_PRIMARY);
//                titlePaint.setTextSize(16);
//                titlePaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                titlePaint.setAntiAlias(true);
//
//                android.graphics.Paint labelPaint = new android.graphics.Paint();
//                labelPaint.setColor(COLOR_TEXT_SECONDARY);
//                labelPaint.setTextSize(9);
//                labelPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                labelPaint.setAntiAlias(true);
//
//                android.graphics.Paint valuePaint = new android.graphics.Paint();
//                valuePaint.setColor(COLOR_TEXT_PRIMARY);
//                valuePaint.setTextSize(10);
//                valuePaint.setAntiAlias(true);
//
//                android.graphics.Paint normalTextPaint = new android.graphics.Paint();
//                normalTextPaint.setColor(COLOR_TEXT_SECONDARY);
//                normalTextPaint.setTextSize(10);
//                normalTextPaint.setAntiAlias(true);
//
//                android.graphics.Paint debitPaint = new android.graphics.Paint();
//                debitPaint.setColor(COLOR_DEBIT);
//                debitPaint.setTextSize(10);
//                debitPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                debitPaint.setAntiAlias(true);
//
//                android.graphics.Paint creditPaint = new android.graphics.Paint();
//                creditPaint.setColor(COLOR_CREDIT);
//                creditPaint.setTextSize(10);
//                creditPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                creditPaint.setAntiAlias(true);
//
//                android.graphics.Paint footerPaint = new android.graphics.Paint();
//                footerPaint.setColor(COLOR_TEXT_SECONDARY);
//                footerPaint.setTextSize(8);
//                footerPaint.setAntiAlias(true);
//
//                // ============ START FIRST PAGE ============
//                android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
//                android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);
//                android.graphics.Canvas canvas = page.getCanvas();
//
//                int y = 0;
//
//                // TOP HEADER BAR
//                canvas.drawRect(0, 0, pageWidth, 50, primaryBgPaint);
//                canvas.drawText("MyKhata Pro", marginLeft, 28, whiteTitlePaint);
//                canvas.drawText("Professional Account Management", marginLeft, 42, whiteSubtitlePaint);
//
//                // Reference number
//                String refNumber = "REF: ST" + System.currentTimeMillis();
//                canvas.drawText(refNumber, pageWidth - marginRight - 110, 30, whiteSmallPaint);
//
//                y = 70;
//
//                // STATEMENT TITLE (NO UNDERLINE)
//                canvas.drawText("ACCOUNT STATEMENT", marginLeft, y, titlePaint);
//
//                y += 15;
//
//                // Generation date
//                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
//                String generatedDate = "Generated: " + dateTimeFormat.format(new Date());
//                canvas.drawText(generatedDate, marginLeft, y, footerPaint);
//
//                y += 22;
//
//                // CUSTOMER INFORMATION BOX - FIXED HEIGHT
//                int infoBoxHeight = 80;
//                canvas.drawRect(marginLeft, y, tableEndX, y + infoBoxHeight, infoBgPaint);
//                canvas.drawRect(marginLeft, y, tableEndX, y + infoBoxHeight, borderPaint);
//
//                // Info box header
//                canvas.drawRect(marginLeft, y, tableEndX, y + 26, primaryBgPaint);
//                canvas.drawText("CUSTOMER INFORMATION", marginLeft + 8, y + 17, headerTextPaint);
//
//                // Horizontal divider after header
//                canvas.drawLine(marginLeft, y + 26, tableEndX, y + 26, borderPaint);
//
//                // Vertical divider in middle
//                int midX = marginLeft + (tableWidth / 2);
//                canvas.drawLine(midX, y + 26, midX, y + infoBoxHeight, borderPaint);
//
//                // Left column content
//                int contentStartY = y + 26;
//                canvas.drawText("Customer Name:", marginLeft + 8, contentStartY + 15, labelPaint);
//                canvas.drawText(customerName, marginLeft + 8, contentStartY + 27, valuePaint);
//
//                canvas.drawText("Statement Date:", marginLeft + 8, contentStartY + 43, labelPaint);
//                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//                String statementDate = dateFormat.format(new Date());
//                canvas.drawText(statementDate, marginLeft + 8, contentStartY + 55, valuePaint);
//
//                // Right column content
//                canvas.drawText("Phone Number:", midX + 8, contentStartY + 15, labelPaint);
//                canvas.drawText(customerPhone, midX + 8, contentStartY + 27, valuePaint);
//
//                canvas.drawText("Account ID:", midX + 8, contentStartY + 43, labelPaint);
//                String accountId = "ACC-" + customerPhone.substring(Math.max(0, customerPhone.length() - 4));
//                canvas.drawText(accountId, midX + 8, contentStartY + 55, valuePaint);
//
//                y += infoBoxHeight + 20;
//
//                // TRANSACTION SECTION HEADER (NO UNDERLINE)
//                canvas.drawText("TRANSACTION DETAILS", marginLeft, y, titlePaint);
//                y += 18;
//
//                // TABLE HEADER
//                canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, primaryBgPaint);
//                canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
//
//                // Vertical lines in header
//                canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
//                canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
//                canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);
//
//                canvas.drawText("DATE", col1X + 8, y + 19, headerTextPaint);
//                canvas.drawText("DEBIT (You Gave)", col2X + 8, y + 19, headerTextPaint);
//                canvas.drawText("CREDIT (You Got)", col3X + 8, y + 19, headerTextPaint);
//                canvas.drawText("BALANCE", col4X + 8, y + 19, headerTextPaint);
//
//                y += rowHeight;
//
//                // CALCULATE TRANSACTIONS
//                double runningBalance = 0;
//                List<TransactionRow> rows = new ArrayList<>();
//
//                for (Transaction t : transactions) {
//                    String dateStr = t.getDate() != null ? t.getDate() : "-";
//                    String type = t.getType() != null ? t.getType().toUpperCase() : "";
//                    double amount = t.getAmount();
//                    double debit = 0, credit = 0;
//
//                    if (type.equals("GAVE") || type.equals("PAYMENT") || type.equals("DEBIT")) {
//                        debit = amount;
//                        runningBalance -= amount;
//                    } else {
//                        credit = amount;
//                        runningBalance += amount;
//                    }
//
//                    rows.add(new TransactionRow(dateStr, debit, credit, runningBalance));
//                }
//
//                // DRAW TRANSACTION ROWS
//                int rowIndex = 0;
//                for (TransactionRow row : rows) {
//                    // Check if we need a new page
//                    if (y > pageHeight - 180) {
//                        // Draw footer on current page
//                        drawPageFooter(canvas, pageWidth, pageHeight, pageNumber, marginLeft, footerPaint, borderPaint);
//
//                        document.finishPage(page);
//                        pageNumber++;
//                        pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
//                        page = document.startPage(pageInfo);
//                        canvas = page.getCanvas();
//
//                        // New page header
//                        y = 0;
//                        canvas.drawRect(0, 0, pageWidth, 40, primaryBgPaint);
//                        canvas.drawText("MyKhata Pro - Statement", marginLeft, 25, whiteSubtitlePaint);
//                        canvas.drawText("Page " + pageNumber, pageWidth - marginRight - 50, 25, whiteSmallPaint);
//
//                        y = 55;
//
//                        // Repeat table header
//                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, primaryBgPaint);
//                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
//                        canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
//                        canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
//                        canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);
//                        canvas.drawText("DATE", col1X + 8, y + 19, headerTextPaint);
//                        canvas.drawText("DEBIT (You Gave)", col2X + 8, y + 19, headerTextPaint);
//                        canvas.drawText("CREDIT (You Got)", col3X + 8, y + 19, headerTextPaint);
//                        canvas.drawText("BALANCE", col4X + 8, y + 19, headerTextPaint);
//
//                        y += rowHeight;
//                        rowIndex = 0;
//                    }
//
//                    // Alternating background
//                    if (rowIndex % 2 == 0) {
//                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, lightBgPaint);
//                    }
//
//                    // Color indicator on left edge
//                    if (row.debit > 0) {
//                        canvas.drawRect(marginLeft, y, marginLeft + 3, y + rowHeight, debitBgPaint);
//                    } else if (row.credit > 0) {
//                        canvas.drawRect(marginLeft, y, marginLeft + 3, y + rowHeight, creditBgPaint);
//                    }
//
//                    // Draw cell borders
//                    canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
//                    canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
//                    canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
//                    canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);
//
//                    // Date
//                    canvas.drawText(row.date, col1X + 8, y + 19, normalTextPaint);
//
//                    // Debit
//                    if (row.debit > 0) {
//                        canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", row.debit),
//                                col2X + 8, y + 19, debitPaint);
//                    } else {
//                        canvas.drawText("-", col2X + 8, y + 19, normalTextPaint);
//                    }
//
//                    // Credit
//                    if (row.credit > 0) {
//                        canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", row.credit),
//                                col3X + 8, y + 19, creditPaint);
//                    } else {
//                        canvas.drawText("-", col3X + 8, y + 19, normalTextPaint);
//                    }
//
//                    // Balance
//                    android.graphics.Paint balancePaint = new android.graphics.Paint();
//                    balancePaint.setTextSize(10);
//                    balancePaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                    balancePaint.setColor(row.balance >= 0 ? COLOR_CREDIT : COLOR_DEBIT);
//                    balancePaint.setAntiAlias(true);
//
//                    String balanceText = (row.balance >= 0 ? "+" : "-") + " ₹ " +
//                            String.format(Locale.getDefault(), "%,.2f", Math.abs(row.balance));
//                    canvas.drawText(balanceText, col4X + 8, y + 19, balancePaint);
//
//                    y += rowHeight;
//                    rowIndex++;
//                }
//
//                // SUMMARY SECTION - FIXED WITH PROPER COLUMNS
//                y += 20;
//                int summaryHeight = 95;
//
//                canvas.drawRect(marginLeft, y, tableEndX, y + summaryHeight, summaryBgPaint);
//                canvas.drawRect(marginLeft, y, tableEndX, y + summaryHeight, borderPaint);
//
//                // Summary header
//                canvas.drawRect(marginLeft, y, tableEndX, y + 26, primaryBgPaint);
//                canvas.drawText("STATEMENT SUMMARY", marginLeft + 8, y + 17, headerTextPaint);
//
//                canvas.drawLine(marginLeft, y + 26, tableEndX, y + 26, borderPaint);
//
//                // Vertical divider for label and value columns
//                int summaryDividerX = marginLeft + 280;
//                canvas.drawLine(summaryDividerX, y + 26, summaryDividerX, y + summaryHeight, borderPaint);
//
//                // Horizontal dividers
//                canvas.drawLine(marginLeft, y + 50, tableEndX, y + 50, borderPaint);
//                canvas.drawLine(marginLeft, y + 74, tableEndX, y + 74, borderPaint);
//
//                // Calculate totals
//                double totalDebit = 0;
//                double totalCredit = 0;
//                for (TransactionRow r : rows) {
//                    totalDebit += r.debit;
//                    totalCredit += r.credit;
//                }
//
//                int summaryContentY = y + 26;
//
//                // Total Debit Row
//                canvas.drawText("Total Debit (You Gave):", marginLeft + 8, summaryContentY + 15, labelPaint);
//                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", totalDebit),
//                        summaryDividerX + 8, summaryContentY + 15, debitPaint);
//
//                // Total Credit Row
//                canvas.drawText("Total Credit (You Got):", marginLeft + 8, summaryContentY + 39, labelPaint);
//                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", totalCredit),
//                        summaryDividerX + 8, summaryContentY + 39, creditPaint);
//
//                // NET BALANCE Row
//                android.graphics.Paint netLabelPaint = new android.graphics.Paint();
//                netLabelPaint.setTextSize(11);
//                netLabelPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                netLabelPaint.setColor(COLOR_TEXT_PRIMARY);
//                netLabelPaint.setAntiAlias(true);
//
//                canvas.drawText("NET BALANCE:", marginLeft + 8, summaryContentY + 63, netLabelPaint);
//
//                double netBalance = rows.size() > 0 ? rows.get(rows.size() - 1).balance : 0;
//
//                android.graphics.Paint netAmountPaint = new android.graphics.Paint();
//                netAmountPaint.setTextSize(12);
//                netAmountPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
//                netAmountPaint.setColor(netBalance >= 0 ? COLOR_CREDIT : COLOR_DEBIT);
//                netAmountPaint.setAntiAlias(true);
//
//                String netText = (netBalance >= 0 ? "+" : "-") + " ₹ " +
//                        String.format(Locale.getDefault(), "%,.2f", Math.abs(netBalance));
//                canvas.drawText(netText, summaryDividerX + 8, summaryContentY + 63, netAmountPaint);
//
//                // FOOTER
//                drawPageFooter(canvas, pageWidth, pageHeight, pageNumber, marginLeft, footerPaint, borderPaint);
//
//                document.finishPage(page);
//
//                // Write to file
//                FileOutputStream fos = new FileOutputStream(pdfFile);
//                document.writeTo(fos);
//                document.close();
//                fos.close();
//
//                return pdfFile;
//
//            } catch (Exception e)
//
//
//            {
//                Log.e("CreatePDF", "Error creating PDF", e);
//                return null;
//            }
//        }
//
//        // HELPER METHOD: Draw page footer
//        private void drawPageFooter(android.graphics.Canvas canvas, int pageWidth, int pageHeight,
//                                    int pageNumber, int marginLeft, android.graphics.Paint footerPaint,
//                                    android.graphics.Paint borderPaint) {
//            int footerY = pageHeight - 25;
//
//            // Divider line
//            canvas.drawLine(marginLeft, footerY - 8, pageWidth - marginLeft, footerY - 8, borderPaint);
//
//            // Footer text
//            canvas.drawText("This is a computer-generated statement from MyKhata Pro",
//                    marginLeft, footerY, footerPaint);
//            canvas.drawText("Page " + pageNumber, pageWidth - marginLeft - 40, footerY, footerPaint);
//        }
//
//
//
//        // Helper class for transaction rows (add this if not already present)
//        static class TransactionRow {
//            String date;
//            double debit;
//            double credit;
//            double balance;
//
//            TransactionRow(String date, double debit, double credit, double balance) {
//                this.date = date;
//                this.debit = debit;
//                this.credit = credit;
//                this.balance = balance;
//            }
//        }}
//
//    private void showFilePickerOptions() {
//        String[] options = {"Camera", "Gallery"};
//        new AlertDialog.Builder(this)
//                .setTitle("Select option")
//                .setItems(options, (dialog, which) -> {
//                    if (which == 0) openCamera();
//                    else if (which == 1) openGallery();
//                })
//                .show();
//    }
//
//    private void openCamera() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST_CODE);
//        }
//    }
//
//    private void openGallery() {
//        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(pickPhoto, PICK_FILE_REQUEST_CODE);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}




package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Customer;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import android.widget.LinearLayout;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.text.ParseException;

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

    // listeners to remove if needed
    private ValueEventListener customersListener;

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

        autoCustomer.setOnItemClickListener((parent, view, position, id) -> { /* nothing extra here */ });
        autoCustomer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { if (customerAdapter!=null) customerAdapter.getFilter().filter(s); }
            @Override public void afterTextChanged(Editable s) { }
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
    }

//    private void initFirebase() {
//        prefManager = new PrefManager(this);
//        String userEmail = prefManager.getUserEmail();
//        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";
//        String shopId = prefManager.getCurrentShopId();
//
//        // Use shop-specific nodes. If no shop selected, fallback to old behavior (but prefer to require a shop)
//        if (shopId == null || shopId.isEmpty()) {
//            // fallback - keep old nodes but warn developer/user
//            transactionsRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(userNode)
//                    .child("transactions");
//            customersRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(userNode)
//                    .child("customers");
//        } else {
//            transactionsRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(userNode)
//                    .child("shops")
//                    .child(shopId)
//                    .child("transactions");
//
//            customersRef = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(userNode)
//                    .child("shops")
//                    .child(shopId)
//                    .child("customers");
//        }
//    }



    private void initFirebase() {
        prefManager = new PrefManager(this);
        String userEmail = prefManager.getUserEmail();
        String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";

        String shopId = prefManager.getCurrentShopId(); // null OR "" = no shop selected

        if (shopId == null || shopId.trim().isEmpty()) {

            // ✅ NO SHOP → Store everything directly under EMAIL
            transactionsRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("transactions");

            customersRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("customers");

            Log.d("FIREBASE_PATH", "NO SHOP → Using root path under email");

        } else {

            // ✅ SHOP SELECTED → Store inside shop
            transactionsRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("shops")
                    .child(shopId)
                    .child("transactions");

            customersRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(userNode)
                    .child("shops")
                    .child(shopId)
                    .child("customers");

            Log.d("FIREBASE_PATH", "SHOP SELECTED → Using shop path: " + shopId);
        }
    }





    private void loadCustomers() {
        // remove previous listener if any
        try {
            if (customersListener != null && customersRef != null) customersRef.removeEventListener(customersListener);
        } catch (Exception ignored){}

        customersListener = new ValueEventListener() {
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
                if (customerAdapter != null) customerAdapter.notifyDataSetChanged();

                if (editTransaction != null) {
                    autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTransactionActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
            }
        };

        if (customersRef != null) customersRef.addValueEventListener(customersListener);
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

        if (customersRef == null) {
            Toast.makeText(this, "Unable to add customer: no shop selected", Toast.LENGTH_SHORT).show();
            return;
        }

        customersRef.child(phone).setValue(newCustomer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show();
                    customerList.add(newCustomer);
                    customerNames.add(name + " (" + phone + ")");
                    if (customerAdapter != null) customerAdapter.notifyDataSetChanged();
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
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Transaction");
            btnSave.setText("Update");
            etDate.setText(editTransaction.getDate());
            etAmount.setText(String.valueOf(editTransaction.getAmount()));
            etNote.setText(editTransaction.getNote());
            autoCustomer.setText(editTransaction.getCustomerName() + " (" + editTransaction.getCustomerPhone() + ")");
            autoCustomer.setEnabled(false);
            if ("gave".equalsIgnoreCase(editTransaction.getType())) {
                toggleButtonGroup.check(R.id.btn_gave);
            } else {
                toggleButtonGroup.check(R.id.btn_got);
            }
            containerFields.setVisibility(View.VISIBLE);
        } else if (prefillCustomerPhone != null && prefillCustomerName != null) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Transaction");
            btnSave.setText("Save Transaction");
            autoCustomer.setText(prefillCustomerName + " (" + prefillCustomerPhone + ")");
            containerFields.setVisibility(View.GONE);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Transaction");
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
            layoutCustomer.setError(null);
            selectedName = customerInfo.substring(0, customerInfo.lastIndexOf(" (")).trim();
            selectedPhone = customerInfo.substring(customerInfo.lastIndexOf(" (") + 2, customerInfo.length() - 1);
        }

        boolean isGave = toggleButtonGroup.getCheckedButtonId() == R.id.btn_gave;

        if (transactionsRef == null) {
            Toast.makeText(this, "Unable to save transaction: no shop selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String idToSave = (editTransaction != null && editTransaction.getId() != null) ? editTransaction.getId()
                : transactionsRef.child(selectedPhone).push().getKey();

        if (idToSave == null || idToSave.trim().isEmpty()) {
            idToSave = String.valueOf(System.currentTimeMillis());
        }

        Transaction transaction = new Transaction();
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
                        new GenerateStatementAndSendSmsTask(this, selectedPhone, selectedName, amount, isGave, date, prefManager.getCurrentShopId()).execute();
                    } else {
                        Intent intent = new Intent(this, TransactionSuccessActivity.class);
                        intent.putExtra(TransactionSuccessActivity.EXTRA_AMOUNT, amount);
                        intent.putExtra(TransactionSuccessActivity.EXTRA_CUSTOMER, selectedName);
                        startActivity(intent);
                        finish();
                    }
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
                        // Remove all non-digit and non-plus characters
                        number = number.replaceAll("[^0-9+]", "");

                        // Remove country code prefix
                        if (number.startsWith("+91")) {
                            number = number.substring(3);
                        } else if (number.startsWith("91") && number.length() > 10) {
                            number = number.substring(2);
                        }

                        // Optionally, remove spaces/dashes
                        number = number.replaceAll("\\s+", "").replaceAll("-", "");

                        String finalNumber = number;
                        new AlertDialog.Builder(this)
                                .setTitle("Add Customer")
                                .setMessage("Add " + name + " (" + finalNumber + ") as a customer?")
                                .setPositiveButton("Yes", (dialog, which) -> addCustomerToFirebase(name, finalNumber))
                                .setNegativeButton("No", null)
                                .show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error reading contact", Toast.LENGTH_SHORT).show();
                }


        } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                // handle camera response if needed
            } else if (requestCode == PICK_FILE_REQUEST_CODE) {
                // handle gallery file pick if needed
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Background task: fetch transactions for customer, generate bank-style PDF, upload to Firebase, then send SMS with link
     */
    private static class GenerateStatementAndSendSmsTask extends android.os.AsyncTask<Void, Void, String> {

        private final Context context;
        private final String phone;
        private final String name;
        private final double amount;
        private final boolean isDebit; // isGave
        private final String date;
        private final String shopId;
        private ProgressDialog progress;

        GenerateStatementAndSendSmsTask(Context ctx, String phone, String name, double amount, boolean isDebit, String date, String shopId) {
            this.context = ctx;
            this.phone = phone;
            this.name = name;
            this.amount = amount;
            this.isDebit = isDebit;
            this.date = date;
            this.shopId = shopId;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            progress.setMessage("Preparing statement and sending SMS...");
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // 1) Read all transactions for this customer synchronously (shop-specific)
                final List<Transaction> txList = new ArrayList<>();
                final Object lock = new Object();
                final boolean[] done = {false};

                PrefManager pm = new PrefManager(context);
                String userEmail = pm.getUserEmail();
                String userNode = userEmail != null ? userEmail.replace(".", ",") : "unknown_user";

                DatabaseReference txnRef;
                if (shopId == null || shopId.isEmpty()) {
                    txnRef = FirebaseDatabase.getInstance().getReference("Khatabook").child(userNode).child("transactions").child(phone);
                } else {
                    txnRef = FirebaseDatabase.getInstance().getReference("Khatabook")
                            .child(userNode).child("shops").child(shopId).child("transactions").child(phone);
                }

                txnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Transaction t = s.getValue(Transaction.class);
                            if (t != null) txList.add(t);
                        }
                        synchronized (lock) {
                            done[0] = true;
                            lock.notifyAll();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        synchronized (lock) {
                            done[0] = true;
                            lock.notifyAll();
                        }
                    }
                });

                // wait up to 10s for Firebase read
                synchronized (lock) {
                    long waitTime = 0;
                    while (!done[0] && waitTime < 10000) {
                        lock.wait(300);
                        waitTime += 300;
                    }
                }

                // 2) Generate bank-style PDF
                File pdfFile = createBankStylePdf(context, name, phone, txList);
                if (pdfFile == null) return null;

                // 3) Upload PDF to Firebase Storage
                final String[] downloadUrl = {null};
                final boolean[] uploadDone = {false};

                String fileName = "statement_" + phone.replaceAll("[^\\d]", "") + "_" + System.currentTimeMillis() + ".pdf";

                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference()
                        .child("Statements/" + fileName);

                storageRef.putFile(Uri.fromFile(pdfFile))
                        .addOnSuccessListener(taskSnapshot -> {
                            storageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        String firebaseUrl = uri.toString();
                                        if (!firebaseUrl.contains("alt=media")) {
                                            if (firebaseUrl.contains("?")) {
                                                firebaseUrl = firebaseUrl + "&alt=media";
                                            } else {
                                                firebaseUrl = firebaseUrl + "?alt=media";
                                            }
                                        }
                                        downloadUrl[0] = firebaseUrl;
                                        uploadDone[0] = true;
                                    })
                                    .addOnFailureListener(e -> uploadDone[0] = true);
                        })
                        .addOnFailureListener(e -> uploadDone[0] = true);

                // wait for upload max 30 sec
                int waited = 0;
                while (!uploadDone[0] && waited < 30000) {
                    Thread.sleep(300);
                    waited += 300;
                }

                return downloadUrl[0];

            } catch (Exception e) {
                Log.e("GenSendStmtTask", "Error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String link) {
            if (progress != null && progress.isShowing()) progress.dismiss();

            String txnType = isDebit ? "debit" : "credit";
            String message;

            if (link != null && !link.isEmpty()) {
                message = "Dear " + name + ",\n" +
                        "₹" + String.format(Locale.getDefault(), "%,.2f", amount) + " " + txnType +
                        " recorded on " + date + ".\n" +
                        "View full statement:\n" + link + "\n\n" +
                        "- MyKhata Pro";
            } else {
                message = "Dear " + name + ",\n" +
                        "₹" + String.format(Locale.getDefault(), "%,.2f", amount) + " " + txnType +
                        " recorded on " + date + ".\n\n" +
                        "- MyKhata Pro";
            }

            try {
                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> parts = smsManager.divideMessage(message);

                // send multipart SMS
                smsManager.sendMultipartTextMessage(
                        phone,
                        null,
                        parts,
                        null,
                        null
                );

                Toast.makeText(context, "SMS sent to " + name, Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e("SendSMS", "Error", e);
                Toast.makeText(context, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Success screen
            Intent intent = new Intent(context, TransactionSuccessActivity.class);
            intent.putExtra(TransactionSuccessActivity.EXTRA_AMOUNT, amount);
            intent.putExtra(TransactionSuccessActivity.EXTRA_CUSTOMER, name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            if (context instanceof AddTransactionActivity) {
                ((AddTransactionActivity) context).finish();
            }
        }

        // createBankStylePdf + helper methods (unchanged from your original version)
        private File createBankStylePdf(Context context, String customerName, String customerPhone, List<Transaction> transactions) {
            try {
                File folder = new File(context.getExternalFilesDir(null), "MyKhataPro/Statements");
                if (!folder.exists()) folder.mkdirs();

                File pdfFile = new File(folder, "statement_" + System.currentTimeMillis() + ".pdf");

                android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
                int pageWidth = 595;
                int pageHeight = 842;
                int pageNumber = 1;

                int marginLeft = 40;
                int marginRight = 40;
                int tableWidth = pageWidth - marginLeft - marginRight;

                int col1X = marginLeft;
                int col2X = marginLeft + 115;
                int col3X = marginLeft + 245;
                int col4X = marginLeft + 375;
                int tableEndX = pageWidth - marginRight;

                int rowHeight = 30;

                // Colors & paints (same as before)...
                final int COLOR_PRIMARY = android.graphics.Color.parseColor("#1976D2");
                final int COLOR_PRIMARY_LIGHT = android.graphics.Color.parseColor("#E3F2FD");
                final int COLOR_DEBIT = android.graphics.Color.parseColor("#E53935");
                final int COLOR_CREDIT = android.graphics.Color.parseColor("#43A047");
                final int COLOR_DEBIT_BG = android.graphics.Color.parseColor("#FFEBEE");
                final int COLOR_CREDIT_BG = android.graphics.Color.parseColor("#E8F5E9");
                final int COLOR_TEXT_PRIMARY = android.graphics.Color.parseColor("#212121");
                final int COLOR_TEXT_SECONDARY = android.graphics.Color.parseColor("#757575");
                final int COLOR_BORDER = android.graphics.Color.parseColor("#BDBDBD");
                final int COLOR_BG_LIGHT = android.graphics.Color.parseColor("#FAFAFA");
                final int COLOR_BG_INFO = android.graphics.Color.parseColor("#F5F5F5");

                android.graphics.Paint borderPaint = new android.graphics.Paint();
                borderPaint.setColor(COLOR_BORDER);
                borderPaint.setStrokeWidth(1f);
                borderPaint.setStyle(android.graphics.Paint.Style.STROKE);
                borderPaint.setAntiAlias(true);

                android.graphics.Paint primaryBgPaint = new android.graphics.Paint();
                primaryBgPaint.setColor(COLOR_PRIMARY);
                primaryBgPaint.setStyle(android.graphics.Paint.Style.FILL);
                primaryBgPaint.setAntiAlias(true);

                android.graphics.Paint lightBgPaint = new android.graphics.Paint();
                lightBgPaint.setColor(COLOR_BG_LIGHT);
                lightBgPaint.setStyle(android.graphics.Paint.Style.FILL);
                lightBgPaint.setAntiAlias(true);

                android.graphics.Paint infoBgPaint = new android.graphics.Paint();
                infoBgPaint.setColor(COLOR_BG_INFO);
                infoBgPaint.setStyle(android.graphics.Paint.Style.FILL);
                infoBgPaint.setAntiAlias(true);

                android.graphics.Paint debitBgPaint = new android.graphics.Paint();
                debitBgPaint.setColor(COLOR_DEBIT_BG);
                debitBgPaint.setStyle(android.graphics.Paint.Style.FILL);
                debitBgPaint.setAntiAlias(true);

                android.graphics.Paint creditBgPaint = new android.graphics.Paint();
                creditBgPaint.setColor(COLOR_CREDIT_BG);
                creditBgPaint.setStyle(android.graphics.Paint.Style.FILL);
                creditBgPaint.setAntiAlias(true);

                android.graphics.Paint summaryBgPaint = new android.graphics.Paint();
                summaryBgPaint.setColor(COLOR_PRIMARY_LIGHT);
                summaryBgPaint.setStyle(android.graphics.Paint.Style.FILL);
                summaryBgPaint.setAntiAlias(true);

                android.graphics.Paint whiteTitlePaint = new android.graphics.Paint();
                whiteTitlePaint.setColor(android.graphics.Color.WHITE);
                whiteTitlePaint.setTextSize(20);
                whiteTitlePaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                whiteTitlePaint.setAntiAlias(true);

                android.graphics.Paint whiteSubtitlePaint = new android.graphics.Paint();
                whiteSubtitlePaint.setColor(android.graphics.Color.WHITE);
                whiteSubtitlePaint.setTextSize(10);
                whiteSubtitlePaint.setAntiAlias(true);

                android.graphics.Paint whiteSmallPaint = new android.graphics.Paint();
                whiteSmallPaint.setColor(android.graphics.Color.WHITE);
                whiteSmallPaint.setTextSize(9);
                whiteSmallPaint.setAntiAlias(true);

                android.graphics.Paint headerTextPaint = new android.graphics.Paint();
                headerTextPaint.setColor(android.graphics.Color.WHITE);
                headerTextPaint.setTextSize(10);
                headerTextPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                headerTextPaint.setAntiAlias(true);

                android.graphics.Paint titlePaint = new android.graphics.Paint();
                titlePaint.setColor(COLOR_TEXT_PRIMARY);
                titlePaint.setTextSize(16);
                titlePaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                titlePaint.setAntiAlias(true);

                android.graphics.Paint labelPaint = new android.graphics.Paint();
                labelPaint.setColor(COLOR_TEXT_SECONDARY);
                labelPaint.setTextSize(9);
                labelPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                labelPaint.setAntiAlias(true);

                android.graphics.Paint valuePaint = new android.graphics.Paint();
                valuePaint.setColor(COLOR_TEXT_PRIMARY);
                valuePaint.setTextSize(10);
                valuePaint.setAntiAlias(true);

                android.graphics.Paint normalTextPaint = new android.graphics.Paint();
                normalTextPaint.setColor(COLOR_TEXT_SECONDARY);
                normalTextPaint.setTextSize(10);
                normalTextPaint.setAntiAlias(true);

                android.graphics.Paint debitPaint = new android.graphics.Paint();
                debitPaint.setColor(COLOR_DEBIT);
                debitPaint.setTextSize(10);
                debitPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                debitPaint.setAntiAlias(true);

                android.graphics.Paint creditPaint = new android.graphics.Paint();
                creditPaint.setColor(COLOR_CREDIT);
                creditPaint.setTextSize(10);
                creditPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                creditPaint.setAntiAlias(true);

                android.graphics.Paint footerPaint = new android.graphics.Paint();
                footerPaint.setColor(COLOR_TEXT_SECONDARY);
                footerPaint.setTextSize(8);
                footerPaint.setAntiAlias(true);

                // ============ START FIRST PAGE ============
                android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);
                android.graphics.Canvas canvas = page.getCanvas();

                int y = 0;

                // TOP HEADER BAR
                canvas.drawRect(0, 0, pageWidth, 50, primaryBgPaint);
                canvas.drawText("MyKhata Pro", marginLeft, 28, whiteTitlePaint);
                canvas.drawText("Professional Account Management", marginLeft, 42, whiteSubtitlePaint);

                // Reference number
                String refNumber = "REF: ST" + System.currentTimeMillis();
                canvas.drawText(refNumber, pageWidth - marginRight - 110, 30, whiteSmallPaint);

                y = 70;

                // STATEMENT TITLE (NO UNDERLINE)
                canvas.drawText("ACCOUNT STATEMENT", marginLeft, y, titlePaint);

                y += 15;

                // Generation date
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                String generatedDate = "Generated: " + dateTimeFormat.format(new Date());
                canvas.drawText(generatedDate, marginLeft, y, footerPaint);

                y += 22;

                // CUSTOMER INFORMATION BOX - FIXED HEIGHT
                int infoBoxHeight = 80;
                canvas.drawRect(marginLeft, y, tableEndX, y + infoBoxHeight, infoBgPaint);
                canvas.drawRect(marginLeft, y, tableEndX, y + infoBoxHeight, borderPaint);

                // Info box header
                canvas.drawRect(marginLeft, y, tableEndX, y + 26, primaryBgPaint);
                canvas.drawText("CUSTOMER INFORMATION", marginLeft + 8, y + 17, headerTextPaint);

                // Horizontal divider after header
                canvas.drawLine(marginLeft, y + 26, tableEndX, y + 26, borderPaint);

                // Vertical divider in middle
                int midX = marginLeft + (tableWidth / 2);
                canvas.drawLine(midX, y + 26, midX, y + infoBoxHeight, borderPaint);

                // Left column content
                int contentStartY = y + 26;
                canvas.drawText("Customer Name:", marginLeft + 8, contentStartY + 15, labelPaint);
                canvas.drawText(customerName, marginLeft + 8, contentStartY + 27, valuePaint);

                canvas.drawText("Statement Date:", marginLeft + 8, contentStartY + 43, labelPaint);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String statementDate = dateFormat.format(new Date());
                canvas.drawText(statementDate, marginLeft + 8, contentStartY + 55, valuePaint);

                // Right column content
                canvas.drawText("Phone Number:", midX + 8, contentStartY + 15, labelPaint);
                canvas.drawText(customerPhone, midX + 8, contentStartY + 27, valuePaint);

                canvas.drawText("Account ID:", midX + 8, contentStartY + 43, labelPaint);
                String accountId = "ACC-" + customerPhone.substring(Math.max(0, customerPhone.length() - 4));
                canvas.drawText(accountId, midX + 8, contentStartY + 55, valuePaint);

                y += infoBoxHeight + 20;

                // TRANSACTION SECTION HEADER (NO UNDERLINE)
                canvas.drawText("TRANSACTION DETAILS", marginLeft, y, titlePaint);
                y += 18;

                // TABLE HEADER
                canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, primaryBgPaint);
                canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);

                // Vertical lines in header
                canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
                canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
                canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);

                canvas.drawText("DATE", col1X + 8, y + 19, headerTextPaint);
                canvas.drawText("DEBIT (You Gave)", col2X + 8, y + 19, headerTextPaint);
                canvas.drawText("CREDIT (You Got)", col3X + 8, y + 19, headerTextPaint);
                canvas.drawText("BALANCE", col4X + 8, y + 19, headerTextPaint);

                y += rowHeight;

                // CALCULATE TRANSACTIONS
                double runningBalance = 0;
                List<TransactionRow> rows = new ArrayList<>();

                for (Transaction t : transactions) {
                    String dateStr = t.getDate() != null ? t.getDate() : "-";
                    String type = t.getType() != null ? t.getType().toUpperCase() : "";
                    double amount = t.getAmount();
                    double debit = 0, credit = 0;

                    if (type.equals("GAVE") || type.equals("PAYMENT") || type.equals("DEBIT")) {
                        debit = amount;
                        runningBalance -= amount;
                    } else {
                        credit = amount;
                        runningBalance += amount;
                    }

                    rows.add(new TransactionRow(dateStr, debit, credit, runningBalance));
                }

                // DRAW TRANSACTION ROWS
                int rowIndex = 0;
                for (TransactionRow row : rows) {
                    // Check if we need a new page
                    if (y > pageHeight - 180) {
                        // Draw footer on current page
                        drawPageFooter(canvas, pageWidth, pageHeight, pageNumber, marginLeft, footerPaint, borderPaint);

                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();

                        // New page header
                        y = 0;
                        canvas.drawRect(0, 0, pageWidth, 40, primaryBgPaint);
                        canvas.drawText("MyKhata Pro - Statement", marginLeft, 25, whiteSubtitlePaint);
                        canvas.drawText("Page " + pageNumber, pageWidth - marginRight - 50, 25, whiteSmallPaint);

                        y = 55;

                        // Repeat table header
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, primaryBgPaint);
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
                        canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
                        canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
                        canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);
                        canvas.drawText("DATE", col1X + 8, y + 19, headerTextPaint);
                        canvas.drawText("DEBIT (You Gave)", col2X + 8, y + 19, headerTextPaint);
                        canvas.drawText("CREDIT (You Got)", col3X + 8, y + 19, headerTextPaint);
                        canvas.drawText("BALANCE", col4X + 8, y + 19, headerTextPaint);

                        y += rowHeight;
                        rowIndex = 0;
                    }

                    // Alternating background
                    if (rowIndex % 2 == 0) {
                        canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, lightBgPaint);
                    }

                    // Color indicator on left edge
                    if (row.debit > 0) {
                        canvas.drawRect(marginLeft, y, marginLeft + 3, y + rowHeight, debitBgPaint);
                    } else if (row.credit > 0) {
                        canvas.drawRect(marginLeft, y, marginLeft + 3, y + rowHeight, creditBgPaint);
                    }

                    // Draw cell borders
                    canvas.drawRect(marginLeft, y, tableEndX, y + rowHeight, borderPaint);
                    canvas.drawLine(col2X, y, col2X, y + rowHeight, borderPaint);
                    canvas.drawLine(col3X, y, col3X, y + rowHeight, borderPaint);
                    canvas.drawLine(col4X, y, col4X, y + rowHeight, borderPaint);

                    // Date
                    canvas.drawText(row.date, col1X + 8, y + 19, normalTextPaint);

                    // Debit
                    if (row.debit > 0) {
                        canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", row.debit),
                                col2X + 8, y + 19, debitPaint);
                    } else {
                        canvas.drawText("-", col2X + 8, y + 19, normalTextPaint);
                    }

                    // Credit
                    if (row.credit > 0) {
                        canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", row.credit),
                                col3X + 8, y + 19, creditPaint);
                    } else {
                        canvas.drawText("-", col3X + 8, y + 19, normalTextPaint);
                    }

                    // Balance
                    android.graphics.Paint balancePaint = new android.graphics.Paint();
                    balancePaint.setTextSize(10);
                    balancePaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                    balancePaint.setColor(row.balance >= 0 ? COLOR_CREDIT : COLOR_DEBIT);
                    balancePaint.setAntiAlias(true);

                    String balanceText = (row.balance >= 0 ? "+" : "-") + " ₹ " +
                            String.format(Locale.getDefault(), "%,.2f", Math.abs(row.balance));
                    canvas.drawText(balanceText, col4X + 8, y + 19, balancePaint);

                    y += rowHeight;
                    rowIndex++;
                }

                // SUMMARY SECTION - FIXED WITH PROPER COLUMNS
                y += 20;
                int summaryHeight = 95;

                canvas.drawRect(marginLeft, y, tableEndX, y + summaryHeight, summaryBgPaint);
                canvas.drawRect(marginLeft, y, tableEndX, y + summaryHeight, borderPaint);

                // Summary header
                canvas.drawRect(marginLeft, y, tableEndX, y + 26, primaryBgPaint);
                canvas.drawText("STATEMENT SUMMARY", marginLeft + 8, y + 17, headerTextPaint);

                canvas.drawLine(marginLeft, y + 26, tableEndX, y + 26, borderPaint);

                // Vertical divider for label and value columns
                int summaryDividerX = marginLeft + 280;
                canvas.drawLine(summaryDividerX, y + 26, summaryDividerX, y + summaryHeight, borderPaint);

                // Horizontal dividers
                canvas.drawLine(marginLeft, y + 50, tableEndX, y + 50, borderPaint);
                canvas.drawLine(marginLeft, y + 74, tableEndX, y + 74, borderPaint);

                // Calculate totals
                double totalDebit = 0;
                double totalCredit = 0;
                for (TransactionRow r : rows) {
                    totalDebit += r.debit;
                    totalCredit += r.credit;
                }

                int summaryContentY = y + 26;

                // Total Debit Row
                canvas.drawText("Total Debit (You Gave):", marginLeft + 8, summaryContentY + 15, labelPaint);
                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", totalDebit),
                        summaryDividerX + 8, summaryContentY + 15, debitPaint);

                // Total Credit Row
                canvas.drawText("Total Credit (You Got):", marginLeft + 8, summaryContentY + 39, labelPaint);
                canvas.drawText("₹ " + String.format(Locale.getDefault(), "%,.2f", totalCredit),
                        summaryDividerX + 8, summaryContentY + 39, creditPaint);

                // NET BALANCE Row
                android.graphics.Paint netLabelPaint = new android.graphics.Paint();
                netLabelPaint.setTextSize(11);
                netLabelPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                netLabelPaint.setColor(COLOR_TEXT_PRIMARY);
                netLabelPaint.setAntiAlias(true);

                canvas.drawText("NET BALANCE:", marginLeft + 8, summaryContentY + 63, netLabelPaint);

                double netBalance = rows.size() > 0 ? rows.get(rows.size() - 1).balance : 0;

                android.graphics.Paint netAmountPaint = new android.graphics.Paint();
                netAmountPaint.setTextSize(12);
                netAmountPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                netAmountPaint.setColor(netBalance >= 0 ? COLOR_CREDIT : COLOR_DEBIT);
                netAmountPaint.setAntiAlias(true);

                String netText = (netBalance >= 0 ? "+" : "-") + " ₹ " +
                        String.format(Locale.getDefault(), "%,.2f", Math.abs(netBalance));
                canvas.drawText(netText, summaryDividerX + 8, summaryContentY + 63, netAmountPaint);

                // FOOTER
                drawPageFooter(canvas, pageWidth, pageHeight, pageNumber, marginLeft, footerPaint, borderPaint);

                document.finishPage(page);

                // Write to file
                FileOutputStream fos = new FileOutputStream(pdfFile);
                document.writeTo(fos);
                document.close();
                fos.close();

                return pdfFile;

            } catch (Exception e) {
                Log.e("CreatePDF", "Error creating PDF", e);
                return null;
            }
        }

        // HELPER METHOD: Draw page footer (same as earlier)
        private void drawPageFooter(android.graphics.Canvas canvas, int pageWidth, int pageHeight,
                                    int pageNumber, int marginLeft, android.graphics.Paint footerPaint,
                                    android.graphics.Paint borderPaint) {
            int footerY = pageHeight - 25;
            canvas.drawLine(marginLeft, footerY - 8, pageWidth - marginLeft, footerY - 8, borderPaint);
            canvas.drawText("This is a computer-generated statement from MyKhata Pro",
                    marginLeft, footerY, footerPaint);
            canvas.drawText("Page " + pageNumber, pageWidth - marginLeft - 40, footerY, footerPaint);
        }

        // Helper class for transaction rows
        static class TransactionRow {
            String date;
            double debit;
            double credit;
            double balance;

            TransactionRow(String date, double debit, double credit, double balance) {
                this.date = date;
                this.debit = debit;
                this.credit = credit;
                this.balance = balance;
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (customersListener != null && customersRef != null) customersRef.removeEventListener(customersListener);
        } catch (Exception ignored) {}
    }
}
