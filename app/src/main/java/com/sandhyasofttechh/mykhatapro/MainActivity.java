package com.sandhyasofttechh.mykhatapro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.activities.AddTransactionActivity;
import com.sandhyasofttechh.mykhatapro.fragments.*;
import com.sandhyasofttechh.mykhatapro.register.LoginActivity;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import androidx.appcompat.widget.Toolbar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabAddTransaction;

    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_CUSTOMERS = "customers";
    private static final String TAG_REPORTS = "reports";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_ABOUT = "about";

    private PrefManager prefManager;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // Header views
    private CircleImageView ivUserPhoto;
    private TextView tvUserName;
    private TextView tvUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = new PrefManager(this);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupDrawer();
        setupBottomNav();
        setupFAB();
        setupDrawerHeader();
        loadUserProfile();

        if (savedInstanceState == null) {
            loadFragmentSafe(new DashboardFragment(), TAG_DASHBOARD);
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNav = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MyKhata Pro");
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupDrawerHeader() {
        View header = navigationView.getHeaderView(0);
        ivUserPhoto = header.findViewById(R.id.iv_user_photo);
        tvUserName = header.findViewById(R.id.tv_user_name);
        tvUserEmail = header.findViewById(R.id.tv_user_email);

        // Set default values
        tvUserName.setText("Loading...");
        tvUserEmail.setText("");
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            if (userEmail != null) {
                // Replace '.' with ',' for Firebase key
                String firebaseKey = userEmail.replace(".", ",");

                // Reference to user's profile in Firebase
                databaseReference = FirebaseDatabase.getInstance()
                        .getReference("Khatabook")
                        .child(firebaseKey)
                        .child("profile");

                // Listen for profile data
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get profile data
                            String businessName = snapshot.child("businessName").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);
                            String logoUrl = snapshot.child("logoUrl").getValue(String.class);
                            String name = snapshot.child("name").getValue(String.class);

                            // Update header views
                            updateDrawerHeader(businessName, email, logoUrl, name);

                            // Save to PrefManager for offline access
                            if (businessName != null) {
                                prefManager.saveBusinessName(businessName);
                            }
                            if (logoUrl != null) {
                                prefManager.saveLogoUrl(logoUrl);
                            }
                        } else {
                            // Profile doesn't exist, show email
                            tvUserName.setText(userEmail);
                            tvUserEmail.setText(userEmail);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error - show cached data if available
                        String cachedBusinessName = prefManager.getBusinessName();
                        String cachedEmail = prefManager.getUserEmail();
                        String cachedLogoUrl = prefManager.getLogoUrl();

                        if (cachedBusinessName != null) {
                            tvUserName.setText(cachedBusinessName);
                            tvUserEmail.setText(cachedEmail != null ? cachedEmail : "");

                            if (cachedLogoUrl != null && !cachedLogoUrl.isEmpty()) {
                                loadProfileImage(cachedLogoUrl);
                            }
                        } else {
                            tvUserName.setText("Error loading profile");
                            tvUserEmail.setText("");
                        }
                    }
                });
            }
        } else {
            // No user logged in
            tvUserName.setText("Guest");
            tvUserEmail.setText("Please login");
        }
    }

    private void updateDrawerHeader(String businessName, String email, String logoUrl, String name) {
        // Priority: Business Name > Name > Email
        if (businessName != null && !businessName.isEmpty()) {
            tvUserName.setText(businessName);
        } else if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        } else {
            tvUserName.setText("MyKhata User");
        }

        // Set email
        if (email != null && !email.isEmpty()) {
            tvUserEmail.setText(email);
            tvUserEmail.setVisibility(View.VISIBLE);
        } else {
            tvUserEmail.setVisibility(View.GONE);
        }

        // Load profile image with Glide
        if (logoUrl != null && !logoUrl.isEmpty()) {
            loadProfileImage(logoUrl);
        }
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(ivUserPhoto);
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragmentSafe(new DashboardFragment(), TAG_DASHBOARD);
            } else if (id == R.id.nav_customers) {
                loadFragmentSafe(new CustomersFragment(), TAG_CUSTOMERS);
            } else if (id == R.id.nav_reports) {
                loadFragmentSafe(new ReportsFragment(), TAG_REPORTS);
            } else if (id == R.id.nav_settings) {
                loadFragmentSafe(new SettingsFragment(), TAG_SETTINGS);
            } else if (id == R.id.placeholder) {
                // Ignore placeholder click
                return false;
            }
            navigationView.setCheckedItem(id);
            return true;
        });
    }

    private void setupFAB() {
        fabAddTransaction.setOnClickListener(v -> {
            // TODO: Create and navigate to AddTransactionActivity
             Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
             startActivity(intent);

            // Temporary: Show Toast until AddTransactionActivity is created
//            Toast.makeText(this, "Add Transaction - Coming Soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            loadFragmentSafe(new DashboardFragment(), TAG_DASHBOARD);
            bottomNav.setSelectedItemId(id);
        } else if (id == R.id.nav_customers) {
            loadFragmentSafe(new CustomersFragment(), TAG_CUSTOMERS);
            bottomNav.setSelectedItemId(id);

        } else if (id == R.id.nav_reports) {
            loadFragmentSafe(new ReportsFragment(), TAG_REPORTS);
            bottomNav.setSelectedItemId(id);
        } else if (id == R.id.nav_settings) {
            loadFragmentSafe(new SettingsFragment(), TAG_SETTINGS);
            bottomNav.setSelectedItemId(id);
        }
        // Drawer-only items
        else if (id == R.id.nav_profile) {
            loadFragmentSafe(new ProfileFragment(), TAG_PROFILE);
        }
//        else if (id == R.id.nav_share) {
//            shareApp();
//        } else if (id == R.id.nav_rate) {
//            rateApp();
//        }
        else if (id == R.id.nav_about) {
            // loadFragmentSafe(new AboutFragment(), TAG_ABOUT);
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragmentSafe(Fragment fragment, String tag) {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        if (existing == null) existing = fragment;

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment_container, existing, tag);
        tx.commit();

        setToolbarTitle(tag);
    }

    private void setToolbarTitle(String tag) {
        if (getSupportActionBar() != null) {
            switch (tag) {
                case TAG_DASHBOARD:
                    getSupportActionBar().setTitle("Dashboard");
                    break;
                case TAG_CUSTOMERS:
                    getSupportActionBar().setTitle("Customers");
                    break;
                case TAG_REPORTS:
                    getSupportActionBar().setTitle("Reports");
                    break;
                case TAG_SETTINGS:
                    getSupportActionBar().setTitle("Settings");
                    break;
                case TAG_PROFILE:
                    getSupportActionBar().setTitle("Profile");
                    break;
                case TAG_ABOUT:
                    getSupportActionBar().setTitle("About");
                    break;
                default:
                    getSupportActionBar().setTitle("MyKhata Pro");
                    break;
            }
        }
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Download MyKhata Pro: https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void rateApp() {
        String appId = getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId));
        try {
            startActivity(rateIntent);
        } catch (Exception e) {
            // If Play Store not available, open in browser
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId)));
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void performLogout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear all saved data from PrefManager
        prefManager.clear();

        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to prevent memory leaks
        if (databaseReference != null) {
            databaseReference.removeEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {}
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}