package com.sandhyasofttechh.mykhatapro.register;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.sandhyasofttechh.mykhatapro.R;

public class NoInternetActivity extends AppCompatActivity {

    private ImageView imgNoInternet;
    private TextView tvMessage;
    private Button btnRetry, btnOpenSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        imgNoInternet = findViewById(R.id.img_no_internet);
        tvMessage = findViewById(R.id.tv_message);
        btnRetry = findViewById(R.id.btn_retry);
        btnOpenSettings = findViewById(R.id.btn_open_settings);

        btnRetry.setOnClickListener(v -> {
            // Try to reopen SplashActivity
            startActivity(new Intent(NoInternetActivity.this, SplashActivity.class));
            finish();
        });

        btnOpenSettings.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });
    }
}
