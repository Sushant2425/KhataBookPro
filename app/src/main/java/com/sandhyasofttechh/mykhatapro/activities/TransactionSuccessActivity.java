package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sandhyasofttechh.mykhatapro.R;

public class TransactionSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CUSTOMER = "extra_customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_success);

        ImageView imgSuccess = findViewById(R.id.img_success);
        TextView tvMessage = findViewById(R.id.tv_message);
        Button btnAddAnother = findViewById(R.id.btn_add_another);
        Button btnViewHistory = findViewById(R.id.btn_view_history);
        Button btnClose = findViewById(R.id.btn_close);

        // Animate the success icon (fade-in effect)
        imgSuccess.setAlpha(0f);
        imgSuccess.animate().alpha(1f).setDuration(800).start();

        // Get transaction info
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        String customer = getIntent().getStringExtra(EXTRA_CUSTOMER);
        tvMessage.setText("₹" + amount + " successfully for " + customer);

        // Button to add another transaction
        btnAddAnother.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Button to view transaction history
        btnViewHistory.setOnClickListener(v -> {
            // TODO: triggered your transaction list activity
            // startActivity(new Intent(this, TransactionHistoryActivity.class));
            finish();
        });

        // Close button
        btnClose.setOnClickListener(v -> finish());
    }
}
