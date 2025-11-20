package com.sandhyasofttechh.mykhatapro.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sandhyasofttechh.mykhatapro.R;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge (Modern Android)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        setContentView(R.layout.activity_about_app);

        // Setup Toolbar + Back Arrow
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Show App Version Automatically
        TextView tvVersion = findViewById(R.id.tv_version);
        try {
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);

            String versionName = packageInfo.versionName;
            long versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? packageInfo.getLongVersionCode()
                    : packageInfo.versionCode;

            tvVersion.setText("Version " + versionName + " (Build " + versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Version 1.0.0");
        }

        // Edge-to-Edge Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Click Listeners (Optional)
        findViewById(R.id.tv_privacy_policy).setOnClickListener(v ->
                openUrl("https://doc-hosting.flycricket.io/khatabookpro-privacy-policy/596f2641-e3fb-4275-944d-e260652b0a49/privacy"));

        findViewById(R.id.tv_terms).setOnClickListener(v ->
                openUrl("https://doc-hosting.flycricket.io/khatabookpro-terms-of-use/bdc61429-7508-4485-b74b-1931ac0b3931/terms"));
    }

    private void openUrl(String url) {
        if (TextUtils.isEmpty(url)) return;
        try {
            startActivity(android.content.Intent.createChooser(
                    new android.content.Intent(android.content.Intent.ACTION_VIEW)
                            .setData(android.net.Uri.parse(url)),
                    "Open with"));
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}