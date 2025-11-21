package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.*;

public class SettingsFragment extends Fragment {

    private LinearLayout optChangePassword, optSmsSettings, optPaymentSettings;
    private LinearLayout optRecycleBin, optAppLock, optHelpSupport, optAboutApp, optAboutUs;

    private ImageView imgLogo;
    private TextView tvBusinessName;

    private DatabaseReference profileRef;
    private ValueEventListener profileListener;  // THIS IS THE FIX

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        setClickListeners();
        loadProfileDataSafely();

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

        imgLogo = view.findViewById(R.id.img_logo);
        tvBusinessName = view.findViewById(R.id.tv_business_name);
    }

    private void setClickListeners() {
        optChangePassword.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        optSmsSettings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SmsSettingsActivity.class)));
        optRecycleBin.setOnClickListener(v -> startActivity(new Intent(requireContext(), RecycleBinActivity.class)));
        optAppLock.setOnClickListener(v -> startActivity(new Intent(requireContext(), AppLockActivity.class)));
        optHelpSupport.setOnClickListener(v -> startActivity(new Intent(requireContext(), HelpAndSupportActivity.class)));
        optAboutApp.setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutAppActivity.class)));
        optAboutUs.setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutUsActivity.class)));
    }

    private void loadProfileDataSafely() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) {
            setDefaultData();
            return;
        }

        String encodedEmail = user.getEmail().replace(".", ",");
        profileRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(encodedEmail)
                .child("profile");

        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;

                String businessName = snapshot.child("businessName").getValue(String.class);
                String logoUrl = snapshot.child("logoUrl").getValue(String.class);

                tvBusinessName.setText(businessName != null && !businessName.trim().isEmpty()
                        ? businessName : "My Business");

                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Glide.with(SettingsFragment.this)
                            .load(logoUrl)
                            .placeholder(R.drawable.img)
                            .error(R.drawable.img)
                            .circleCrop()
                            .into(imgLogo);
                } else {
                    imgLogo.setImageResource(R.drawable.img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    tvBusinessName.setText("My Business");
                    imgLogo.setImageResource(R.drawable.img);
                }
            }
        };

        profileRef.addValueEventListener(profileListener);
    }

    private void setDefaultData() {
        if (isAdded()) {
            tvBusinessName.setText("My Business");
            imgLogo.setImageResource(R.drawable.img);
        }
    }

    // FIXED: Remove listener properly — no more ClassCastException
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileRef != null && profileListener != null) {
            profileRef.removeEventListener(profileListener);
        }
    }
}