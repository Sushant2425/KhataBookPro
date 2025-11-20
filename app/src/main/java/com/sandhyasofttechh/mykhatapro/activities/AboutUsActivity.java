package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.sandhyasofttechh.mykhatapro.R;

public class AboutUsActivity extends AppCompatActivity {

    private TextView tvLastBackupTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvLastBackupTime = findViewById(R.id.tv_last_backup_time);

        showRealTimeNow(); // This will show current date & time immediately
    }

    private void showRealTimeNow() {
        long currentTime = System.currentTimeMillis();

        String date = DateFormat.format("dd MMM yyyy", currentTime).toString();
        String time = DateFormat.format("hh:mm a", currentTime).toString();

        tvLastBackupTime.setText(date + "\n" + time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRealTimeNow(); // Always shows latest time when you open the screen
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}