package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.ChangePasswordActivity;
import com.sandhyasofttechh.mykhatapro.activities.RecycleBinActivity;
import com.sandhyasofttechh.mykhatapro.activities.AboutUsActivity;
import com.sandhyasofttechh.mykhatapro.activities.HelpAndSupportActivity;
import com.sandhyasofttechh.mykhatapro.activities.AppLockActivity;

public class SettingsFragment extends Fragment {

    private TextView optChangePassword, optSmsSettings, optPaymentSettings;
    private TextView optRecycleBin, optAppLock, optHelpSupport, optAboutApp, optAboutUs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        onClicks();

        return view;
    }

    private void initViews(View view) {

        optChangePassword = view.findViewById(R.id.opt_change_password);
        optSmsSettings = view.findViewById(R.id.opt_sms_settings);
        optPaymentSettings = view.findViewById(R.id.opt_payment_settings);

        optRecycleBin = view.findViewById(R.id.opt_recycle_bin);
        optAppLock = view.findViewById(R.id.opt_app_lock);
        optHelpSupport = view.findViewById(R.id.opt_help_support);
        optAboutApp = view.findViewById(R.id.opt_about_app);
        optAboutUs = view.findViewById(R.id.opt_about_us);
    }

    private void onClicks() {

        // Change Password
        optChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        // Recycle Bin
        optRecycleBin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RecycleBinActivity.class));
        });

        // About Us
        optAboutUs.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AboutUsActivity.class));
        });

        // Help & Support
        optHelpSupport.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), HelpAndSupportActivity.class));
        });

        // App Lock (If available)
        optAppLock.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AppLockActivity.class));
        });

        // Optional: About App (if you have AboutAppActivity)
        optAboutApp.setOnClickListener(v -> {
            // startActivity(new Intent(requireContext(), AboutAppActivity.class));
        });

        // Optional SMS Settings
        optSmsSettings.setOnClickListener(v -> {
            // startActivity(new Intent(requireContext(), SmsSettingsActivity.class));
        });

        // Optional Payment Settings
        optPaymentSettings.setOnClickListener(v -> {
            // startActivity(new Intent(requireContext(), PaymentSettingsActivity.class));
        });
    }
}
