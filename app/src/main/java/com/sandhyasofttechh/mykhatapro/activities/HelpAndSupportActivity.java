package com.sandhyasofttechh.mykhatapro.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

public class HelpAndSupportActivity extends AppCompatActivity {

    // Views for contact info
    private ImageView ivCall, ivWhatsApp, ivInstagram, ivEmail;
    private TextView tvPhone, tvWhatsApp, tvInstagram, tvEmail;

    // Support form views
    private EditText etName, etEmail, etMessage;
    private Button btnSend;
    private PrefManager prefManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_support);

        prefManager = new PrefManager(this);

        initViews();
        setupClickListeners();

        prefillUserEmail(); // Prefill email if available

    }
    private void prefillUserEmail() {
        String userEmail = prefManager.getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            etEmail.setText(userEmail);
        }
    }

    private void sendSupportEmail() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Please enter your name");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (message.length() < 5) {  // Updated validation here
            etMessage.setError("Message must be at least 5 characters long");
            etMessage.requestFocus();
            return;
        }

        String supportEmail = tvEmail.getText().toString().trim();

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + supportEmail));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from " + name);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Name: " + name + "\nEmail: " + email + "\n\nMessage:\n" + message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send support email"));
            clearForm();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No email client found.", Toast.LENGTH_SHORT).show();
        }
    }
    private void initViews() {
        ivCall = findViewById(R.id.iv_call);
        ivWhatsApp = findViewById(R.id.iv_whatsapp);
        ivInstagram = findViewById(R.id.iv_instagram);
        ivEmail = findViewById(R.id.iv_email);

        tvPhone = findViewById(R.id.tv_phone);
        tvWhatsApp = findViewById(R.id.tv_whatsapp);
        tvInstagram = findViewById(R.id.tv_instagram);
        tvEmail = findViewById(R.id.tv_email);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setupClickListeners() {
        // Phone call
        View.OnClickListener callListener = v -> {
            String phone = tvPhone.getText().toString().trim();
            String uri = "tel:" + phone;
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
            startActivity(intent);
        };
        ivCall.setOnClickListener(callListener);
        tvPhone.setOnClickListener(callListener);

        // WhatsApp chat
        View.OnClickListener whatsappListener = v -> {
            String whatsappNumber = tvWhatsApp.getText().toString().replace("+", "").trim();
            openWhatsApp(whatsappNumber);
        };
        ivWhatsApp.setOnClickListener(whatsappListener);
        tvWhatsApp.setOnClickListener(whatsappListener);

        // Instagram profile
        View.OnClickListener instagramListener = v -> {
            String instaHandle = tvInstagram.getText().toString().trim();
            openInstagramProfile(instaHandle);
        };
        ivInstagram.setOnClickListener(instagramListener);
        tvInstagram.setOnClickListener(instagramListener);

        // Email compose
        View.OnClickListener emailListener = v -> {
            String email = tvEmail.getText().toString().trim();
            composeEmail(email);
        };
        ivEmail.setOnClickListener(emailListener);
        tvEmail.setOnClickListener(emailListener);

        // Send message button in the form
        btnSend.setOnClickListener(v -> sendSupportEmail());
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "WhatsApp not installed on your device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openInstagramProfile(String handle) {
        Uri uri = Uri.parse("http://instagram.com/_u/" + handle);
        Intent instagramIntent = new Intent(Intent.ACTION_VIEW, uri);
        instagramIntent.setPackage("com.instagram.android");

        try {
            startActivity(instagramIntent);
        } catch (ActivityNotFoundException e) {
            // Instagram app not installed, open in browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/" + handle));
            startActivity(webIntent);
        }
    }

    private void composeEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from App");

        try {
            startActivity(Intent.createChooser(intent, "Choose email client"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No email client installed on your device.", Toast.LENGTH_SHORT).show();
        }
    }

//    private void sendSupportEmail() {
//        String name = etName.getText().toString().trim();
//        String email = etEmail.getText().toString().trim();
//        String message = etMessage.getText().toString().trim();
//
//        if (TextUtils.isEmpty(name)) {
//            etName.setError("Please enter your name");
//            etName.requestFocus();
//            return;
//        }
//
//        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            etEmail.setError("Please enter a valid email");
//            etEmail.requestFocus();
//            return;
//        }
//
//        if (TextUtils.isEmpty(message)) {
//            etMessage.setError("Please enter your message");
//            etMessage.requestFocus();
//            return;
//        }
//
//        String supportEmail = tvEmail.getText().toString().trim();
//
//        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
//        emailIntent.setData(Uri.parse("mailto:" + supportEmail));
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from " + name);
//        emailIntent.putExtra(Intent.EXTRA_TEXT,
//                "Name: " + name + "\nEmail: " + email + "\n\nMessage:\n" + message);
//
//        try {
//            startActivity(Intent.createChooser(emailIntent, "Send support email"));
//            clearForm();
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, "No email client found.", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etMessage.setText("");
        etName.requestFocus();
        prefillUserEmail();


    }
}
