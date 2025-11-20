package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.AboutAppActivity;
import com.sandhyasofttechh.mykhatapro.activities.ChangePasswordActivity;
import com.sandhyasofttechh.mykhatapro.activities.PaymentSettingsActivity;
import com.sandhyasofttechh.mykhatapro.activities.RecycleBinActivity;
import com.sandhyasofttechh.mykhatapro.activities.AboutUsActivity;
import com.sandhyasofttechh.mykhatapro.activities.HelpAndSupportActivity;
import com.sandhyasofttechh.mykhatapro.activities.AppLockActivity;
import com.sandhyasofttechh.mykhatapro.activities.SmsSettingsActivity;

public class SettingsFragment extends Fragment {

    private LinearLayout optChangePassword, optSmsSettings, optPaymentSettings;
    private LinearLayout optRecycleBin, optAppLock, optHelpSupport, optAboutApp, optAboutUs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        setClickListeners();

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

    private void setClickListeners() {
        optChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        optSmsSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SmsSettingsActivity.class));
        });

//        optPaymentSettings.setOnClickListener(v -> {
//            // Uncomment and implement PaymentSettingsActivity when available
//            startActivity(new Intent(requireContext(), PaymentSettingsActivity.class));
//        });

        optRecycleBin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RecycleBinActivity.class));
        });

        optAppLock.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AppLockActivity.class));
        });

        optHelpSupport.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), HelpAndSupportActivity.class));
        });

        optAboutApp.setOnClickListener(v -> {
            // Uncomment and implement AboutAppActivity when available
             startActivity(new Intent(requireContext(), AboutAppActivity.class));
        });

        optAboutUs.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AboutUsActivity.class));
        });
    }
}
