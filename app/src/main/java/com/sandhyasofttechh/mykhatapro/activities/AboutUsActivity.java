package com.sandhyasofttechh.mykhatapro.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sandhyasofttechh.mykhatapro.R;

public class AboutUsActivity extends AppCompatActivity {

    private TextView tvAppName, tvAppVersion, tvCompanyName, tvDescription, tvContactInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initViews();
        setAppInfo();
        setCompanyInfo();
    }

    private void initViews() {
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppVersion = findViewById(R.id.tv_app_version);
        tvCompanyName = findViewById(R.id.tv_company_name);
        tvDescription = findViewById(R.id.tv_description);
        tvContactInfo = findViewById(R.id.tv_contact_info);
    }

    private void setAppInfo() {
        tvAppName.setText(getString(R.string.app_name));

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            tvAppVersion.setText("Version: N/A");
        }
    }

    private void setCompanyInfo() {
        tvCompanyName.setText("Sandhya Soft Tech");
        tvDescription.setText("At Sandhya Soft Tech, we develop innovative, user-friendly Android applications focused on improving productivity and delivering seamless experiences. Our commitment to quality and continuous improvement drives everything we do.");
        tvContactInfo.setText("Contact Us:\nPhone: +91 9527537131\nEmail: sandhyacomputer1@gmail.com\nWebsite: www.sandhyasofttechh.com");
    }
}
