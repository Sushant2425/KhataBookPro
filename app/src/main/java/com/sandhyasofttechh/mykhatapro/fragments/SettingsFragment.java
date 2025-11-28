//package com.sandhyasofttechh.mykhatapro.fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.coordinatorlayout.widget.CoordinatorLayout;
//import androidx.core.widget.NestedScrollView;
//
//import com.bumptech.glide.Glide;
//import com.google.android.material.appbar.AppBarLayout;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.activities.ChangePasswordActivity;
//import com.sandhyasofttechh.mykhatapro.activities.FullScreenImageActivity;
//import com.sandhyasofttechh.mykhatapro.activities.HelpAndSupportActivity;
//import com.sandhyasofttechh.mykhatapro.activities.AppLockActivity;
//import com.sandhyasofttechh.mykhatapro.activities.AboutAppActivity;
//import com.sandhyasofttechh.mykhatapro.activities.AboutUsActivity;
//import com.sandhyasofttechh.mykhatapro.activities.RecycleBinActivity;
//import com.sandhyasofttechh.mykhatapro.activities.SmsSettingsActivity;
//import com.sandhyasofttechh.mykhatapro.register.LoginActivity;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import androidx.fragment.app.Fragment;
//import de.hdodenhof.circleimageview.CircleImageView;
//import android.widget.TextView;
//
//public class SettingsFragment extends Fragment {
//
//    // Views
//    private CircleImageView imgLogoBig, imgLogoSmall;
//    private TextView tvBusinessNameBig, tvBusinessNameSmall;
//    private LinearLayout collapsedTitleLayout;
//    private AppBarLayout appBarLayout;
//
//    // Options
//    private LinearLayout optChangePassword, optSmsSettings, optRecycleBin;
//    private LinearLayout optAppLock, optHelpSupport, optAboutApp, optAboutUs;
//
//    // Firebase
//    private DatabaseReference profileRef;
//    private ValueEventListener profileListener;
//    private String currentLogoUrl = "";
//
//    // Utils
//    private PrefManager prefManager;
//    private static final String TAG_PROFILE = "profile";
//
//    public SettingsFragment() {}
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//        prefManager = new PrefManager(requireContext());
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_settings, container, false);
//
//        initViews(view);
//        setupCollapsingHeader();
//        setClickListeners();
//        loadProfileDataSafely();
//
//        // Set toolbar title
//        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
//            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Settings");
//        }
//
//        return view;
//    }
//
//    private void initViews(View view) {
//        // Profile Views
//        imgLogoBig = view.findViewById(R.id.img_logo_big);
//        imgLogoSmall = view.findViewById(R.id.img_logo_small);
//        tvBusinessNameBig = view.findViewById(R.id.tv_business_name_big);
//        tvBusinessNameSmall = view.findViewById(R.id.tv_business_name_small);
//        collapsedTitleLayout = view.findViewById(R.id.collapsed_title_layout);
//        appBarLayout = view.findViewById(R.id.appbar);
//
//        // Options
//        optChangePassword = view.findViewById(R.id.opt_change_password);
//        optSmsSettings = view.findViewById(R.id.opt_sms_settings);
//        optRecycleBin = view.findViewById(R.id.opt_recycle_bin);
//        optAppLock = view.findViewById(R.id.opt_app_lock);
//        optHelpSupport = view.findViewById(R.id.opt_help_support);
//        optAboutApp = view.findViewById(R.id.opt_about_app);
//        optAboutUs = view.findViewById(R.id.opt_about_us);
//
//        // Click on big logo → full screen
//        imgLogoBig.setOnClickListener(v -> openFullScreenImage());
//    }
//
//    private void setupCollapsingHeader() {
//        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
//            float alpha = (float) -verticalOffset / (float) appBarLayout.getTotalScrollRange();
//
//            // Show small icon + name only when scrolled
//            collapsedTitleLayout.setAlpha(alpha);
//
//            // Optional: slight zoom effect on big logo
//            float scale = 1.0f + (0.2f * alpha);
//            imgLogoBig.setScaleX(scale);
//            imgLogoBig.setScaleY(scale);
//        });
//    }
//
//    private void setClickListeners() {
//        optChangePassword.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
//        optSmsSettings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SmsSettingsActivity.class)));
//        optRecycleBin.setOnClickListener(v -> startActivity(new Intent(requireContext(), RecycleBinActivity.class)));
//        optAppLock.setOnClickListener(v -> startActivity(new Intent(requireContext(), AppLockActivity.class)));
//        optHelpSupport.setOnClickListener(v -> startActivity(new Intent(requireContext(), HelpAndSupportActivity.class)));
//        optAboutApp.setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutAppActivity.class)));
//        optAboutUs.setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutUsActivity.class)));
//    }
//
//    private void loadProfileDataSafely() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null || getContext() == null) {
//            setDefaultData();
//            return;
//        }
//
//        String encodedEmail = user.getEmail().replace(".", ",");
//        profileRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(encodedEmail)
//                .child("profile");
//
//        profileListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!isAdded() || getContext() == null) return;
//
//                String businessName = snapshot.child("businessName").getValue(String.class);
//                String logoUrl = snapshot.child("logoUrl").getValue(String.class);
//
//                String name = businessName != null && !businessName.trim().isEmpty()
//                        ? businessName : "My Business";
//
//                currentLogoUrl = logoUrl != null ? logoUrl : "";
//
//                tvBusinessNameBig.setText(name);
//                tvBusinessNameSmall.setText(name);
//
//                int placeholder = R.drawable.img;
//
//                Glide.with(SettingsFragment.this)
//                        .load(logoUrl != null && !logoUrl.isEmpty() ? logoUrl : placeholder)
//                        .placeholder(placeholder)
//                        .error(placeholder)
//                        .circleCrop()
//                        .into(imgLogoBig);
//
//                Glide.with(SettingsFragment.this)
//                        .load(logoUrl != null && !logoUrl.isEmpty() ? logoUrl : placeholder)
//                        .placeholder(placeholder)
//                        .error(placeholder)
//                        .circleCrop()
//                        .into(imgLogoSmall);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                if (isAdded()) setDefaultData();
//            }
//        };
//
//        profileRef.addValueEventListener(profileListener);
//    }
//
//    private void setDefaultData() {
//        if (isAdded()) {
//            tvBusinessNameBig.setText("My Business");
//            tvBusinessNameSmall.setText("My Business");
//            imgLogoBig.setImageResource(R.drawable.img);
//            imgLogoSmall.setImageResource(R.drawable.img);
//        }
//    }
//
//    private void openFullScreenImage() {
//        Intent intent = new Intent(requireContext(), FullScreenImageActivity.class);
//        intent.putExtra("image_url", currentLogoUrl.isEmpty() ? "default" : currentLogoUrl);
//        intent.putExtra("business_name", tvBusinessNameBig.getText().toString());
//        startActivity(intent);
//        requireActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
//    }
//
//    // Three-dot Menu
//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_main, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_edit_profile || id == R.id.action_change_photo) {
//            // Replace with your ProfileFragment or EditActivity
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new ProfileFragment(), TAG_PROFILE)
//                    .addToBackStack(null)
//                    .commit();
//            return true;
//        }
//
//        if (id == R.id.action_logout) {
//            FirebaseAuth.getInstance().signOut();
//            prefManager.clearAll();
//            startActivity(new Intent(requireActivity(), LoginActivity.class));
//            requireActivity().finish();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (profileRef != null && profileListener != null) {
//            profileRef.removeEventListener(profileListener);
//        }
//    }
//}



package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sandhyasofttechh.mykhatapro.activities.BusinessCardActivity;
import com.sandhyasofttechh.mykhatapro.activities.CollectionActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.ChangePasswordActivity;
import com.sandhyasofttechh.mykhatapro.activities.FullScreenImageActivity;
import com.sandhyasofttechh.mykhatapro.activities.HelpAndSupportActivity;
import com.sandhyasofttechh.mykhatapro.activities.AppLockActivity;
import com.sandhyasofttechh.mykhatapro.activities.AboutAppActivity;
import com.sandhyasofttechh.mykhatapro.activities.AboutUsActivity;
import com.sandhyasofttechh.mykhatapro.activities.RecycleBinActivity;
import com.sandhyasofttechh.mykhatapro.activities.SmsSettingsActivity;
import com.sandhyasofttechh.mykhatapro.register.LoginActivity;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {

    private CircleImageView imgLogoBig, imgLogoSmall;
    private TextView tvBusinessNameBig, tvBusinessNameSmall;
    private LinearLayout collapsedTitleLayout;
    private AppBarLayout appBarLayout;

    // Options
    private LinearLayout optChangePassword, optSmsSettings, optRecycleBin;
    private LinearLayout optAppLock, optHelpSupport, optAboutApp, optAboutUs,collection,optbusinesscard;

    // Firebase
    private DatabaseReference profileRef;
    private ValueEventListener profileListener;

    private PrefManager prefManager;
    private String currentLogoUrl = "";

    public SettingsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        prefManager = new PrefManager(requireContext());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        setupCollapsingHeader();
        setupClickListeners();
        loadProfileData();

        // Toolbar title
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Settings");
        }

        return view;
    }

    private void initViews(View view) {
        imgLogoBig = view.findViewById(R.id.img_logo_big);
        imgLogoSmall = view.findViewById(R.id.img_logo_small);
        tvBusinessNameBig = view.findViewById(R.id.tv_business_name_big);
        tvBusinessNameSmall = view.findViewById(R.id.tv_business_name_small);
        collapsedTitleLayout = view.findViewById(R.id.collapsed_title_layout);
        appBarLayout = view.findViewById(R.id.appbar);

        optChangePassword = view.findViewById(R.id.opt_change_password);
        optSmsSettings = view.findViewById(R.id.opt_sms_settings);
        optRecycleBin = view.findViewById(R.id.opt_recycle_bin);
        optAppLock = view.findViewById(R.id.opt_app_lock);
        optHelpSupport = view.findViewById(R.id.opt_help_support);
        optAboutApp = view.findViewById(R.id.opt_about_app);
        optAboutUs = view.findViewById(R.id.opt_about_us);
        collection = view.findViewById(R.id.collection);
        optbusinesscard = view.findViewById(R.id.opt_business_card);



        imgLogoBig.setOnClickListener(v -> openFullScreenImage());

     collection.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CollectionActivity.class)));
    }

    // Collapsing toolbar animation
    private void setupCollapsingHeader() {
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float alpha = (float) -verticalOffset / (float) appBarLayout.getTotalScrollRange();
            collapsedTitleLayout.setAlpha(alpha);

            float scale = 1.0f + (0.2f * alpha);
            imgLogoBig.setScaleX(scale);
            imgLogoBig.setScaleY(scale);
        });
    }

    private void setupClickListeners() {
        optChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));

        optSmsSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SmsSettingsActivity.class)));

        optRecycleBin.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RecycleBinActivity.class)));

        optAppLock.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AppLockActivity.class)));

        optHelpSupport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HelpAndSupportActivity.class)));

        optAboutApp.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutAppActivity.class)));

        optAboutUs.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutUsActivity.class)));
        optbusinesscard.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BusinessCardActivity.class)));
    }

    // -----------------------------------------------------------
    // 🔥 MAIN METHOD — Multi-Shop Profile Loader
    // -----------------------------------------------------------
    private void loadProfileData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) return;

        String emailKey = user.getEmail().replace(".", ",");
        String shopId = prefManager.getCurrentShopId();

        // If shop exists → read shop profile
        if (shopId != null && !shopId.isEmpty()) {
            profileRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId)
                    .child("profile");
        } else {
            // No shop → read root profile
            profileRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("profile");
        }

        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                String businessName = snapshot.child("businessName").getValue(String.class);
                String logoUrl = snapshot.child("logoUrl").getValue(String.class);

                if (businessName == null || businessName.trim().isEmpty())
                    businessName = "My Business";

                currentLogoUrl = logoUrl != null ? logoUrl : "";

                tvBusinessNameBig.setText(businessName);
                tvBusinessNameSmall.setText(businessName);

                int placeholder = R.drawable.img;

                Glide.with(SettingsFragment.this)
                        .load(logoUrl != null && !logoUrl.isEmpty() ? logoUrl : placeholder)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .circleCrop()
                        .into(imgLogoBig);

                Glide.with(SettingsFragment.this)
                        .load(logoUrl != null && !logoUrl.isEmpty() ? logoUrl : placeholder)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .circleCrop()
                        .into(imgLogoSmall);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        profileRef.addValueEventListener(profileListener);
    }

    private void openFullScreenImage() {
        Intent intent = new Intent(requireContext(), FullScreenImageActivity.class);
        intent.putExtra("image_url", currentLogoUrl.isEmpty() ? "default" : currentLogoUrl);
        intent.putExtra("business_name", tvBusinessNameBig.getText().toString());
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    // Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_profile || id == R.id.action_change_photo) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), "profile")
                    .addToBackStack(null)
                    .commit();
            return true;
        }

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            prefManager.clearAll();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileRef != null && profileListener != null) {
            profileRef.removeEventListener(profileListener);
        }
    }
}
