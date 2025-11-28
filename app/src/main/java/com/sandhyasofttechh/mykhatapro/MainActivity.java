//package com.sandhyasofttechh.mykhatapro;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.navigation.NavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.sandhyasofttechh.mykhatapro.activities.AboutAppActivity;
//import com.sandhyasofttechh.mykhatapro.activities.AboutUsActivity;
//import com.sandhyasofttechh.mykhatapro.activities.AddTransactionActivity;
//import com.sandhyasofttechh.mykhatapro.fragments.*;
//import com.sandhyasofttechh.mykhatapro.register.LoginActivity;
//import com.sandhyasofttechh.mykhatapro.register.SwitchShopActivity;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import androidx.appcompat.widget.Toolbar;
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class MainActivity extends AppCompatActivity
//        implements NavigationView.OnNavigationItemSelectedListener {
//
//    private DrawerLayout drawerLayout;
//    private BottomNavigationView bottomNav;
//    private NavigationView navigationView;
//    private Toolbar toolbar;
//    private FloatingActionButton fabAddTransaction;
//
//    private PrefManager prefManager;
//    private FirebaseAuth mAuth;
//
//    private CircleImageView ivUserPhoto;
//    private TextView tvUserName, tvUserEmail;
//
//    private static final String TAG_DASHBOARD = "dashboard";
//    private static final String TAG_CUSTOMERS = "customers";
//    private static final String TAG_REPORTS = "reports";
//    private static final String TAG_SETTINGS = "settings";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        prefManager = new PrefManager(this);
//        mAuth = FirebaseAuth.getInstance();
//
//        initViews();
//        setupToolbar();
//        setupDrawer();
//        setupBottomNav();
//        setupFAB();
//        setupDrawerHeader();
//        loadUserProfile();
//        checkIfShopStillExists();
//        updateToolbarTitle();
//
//        updateToolbarTitle(); // set at startup
//
//        if (savedInstanceState == null) {
//            loadFragmentSafe(new DashboardFragment(), TAG_DASHBOARD);
//            bottomNav.setSelectedItemId(R.id.nav_dashboard);
//            navigationView.setCheckedItem(R.id.nav_dashboard);
//        }
//    }
//
//    private void initViews() {
//        drawerLayout = findViewById(R.id.drawer_layout);
//        bottomNav = findViewById(R.id.bottom_navigation);
//        navigationView = findViewById(R.id.nav_view);
//        toolbar = findViewById(R.id.toolbar);
//        fabAddTransaction = findViewById(R.id.fab_add_transaction);
//    }
//
//
//    private void checkIfShopStillExists() {
//        String shopId = prefManager.getCurrentShopId();
//        String email = prefManager.getUserEmail();
//        if (shopId == null || shopId.isEmpty() || email == null) return;
//
//        String emailKey = email.replace(".", ",");
//
//        DatabaseReference shopRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(emailKey)
//                .child("shops")
//                .child(shopId);
//
//        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                // ❗ If shop node does NOT exist → clear saved shop
//                if (!snapshot.exists()) {
//                    prefManager.setCurrentShopId("");
//                    prefManager.setCurrentShopName("");
//                    updateToolbarTitle(); // refresh toolbar
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });
//    }
//
//
//    private void setupToolbar() {
//        setSupportActionBar(toolbar);
//    }
//
//    private void updateToolbarTitle() {
//        String shopName = prefManager.getCurrentShopName();
//
//        if (shopName == null || shopName.trim().isEmpty()) {
//            setToolbarTitle("Dashboard");
//        } else {
//            setToolbarTitle(shopName);
//        }
//    }
//
//    public void setToolbarTitle(String title) {
//        if (getSupportActionBar() != null) {
//            if (title == null || title.trim().isEmpty()) {
//                getSupportActionBar().setTitle("Dashboard");
//            } else {
//                getSupportActionBar().setTitle(title);
//            }
//        }
//    }
//
//    private void setupDrawer() {
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawerLayout, toolbar,
//                R.string.navigation_drawer_open,
//                R.string.navigation_drawer_close
//        );
//
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//
//        navigationView.setNavigationItemSelectedListener(this);
//    }
//
//    private void setupDrawerHeader() {
//        View header = navigationView.getHeaderView(0);
//
//        ivUserPhoto = header.findViewById(R.id.iv_user_photo);
//        tvUserName = header.findViewById(R.id.tv_user_name);
//        tvUserEmail = header.findViewById(R.id.tv_user_email);
//
//        tvUserName.setText("Loading...");
//        tvUserEmail.setText("");
//    }
//
//    private void loadUserProfile() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user == null) return;
//
//        String email = user.getEmail();
//        if (email == null) return;
//
//        tvUserEmail.setText(email);
//
//        // Convert email to Firebase key
//        String emailKey = email.replace(".", ",");
//
//        // SHOP LOGIC
//        String shopId = prefManager.getCurrentShopId();
//        DatabaseReference ref;
//
//        if (shopId != null && !shopId.isEmpty()) {
//            // 🔥 Load SHOP profile
//            ref = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(emailKey)
//                    .child("shops")
//                    .child(shopId)
//                    .child("profile");
//        } else {
//            // 🔥 Load ROOT profile
//            ref = FirebaseDatabase.getInstance()
//                    .getReference("Khatabook")
//                    .child(emailKey)
//                    .child("profile");
//        }
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                String businessName = snapshot.child("businessName").getValue(String.class);
//                String name = snapshot.child("name").getValue(String.class);
//                String logoUrl = snapshot.child("logoUrl").getValue(String.class);
//
//                // Name Priority: BusinessName → Name → Email
//                if (businessName != null && !businessName.isEmpty()) {
//                    tvUserName.setText(businessName);
//                } else if (name != null && !name.isEmpty()) {
//                    tvUserName.setText(name);
//                } else {
//                    tvUserName.setText("MyKhata User");
//                }
//
//                // SAVE to PrefManager cache
//                if (businessName != null) prefManager.saveBusinessName(businessName);
//                if (logoUrl != null) prefManager.saveLogoUrl(logoUrl);
//
//                // Load profile picture
//                if (logoUrl != null && !logoUrl.isEmpty()) {
//                    Glide.with(MainActivity.this)
//                            .load(logoUrl)
//                            .placeholder(R.drawable.ic_person)
//                            .error(R.drawable.ic_person)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .circleCrop()
//                            .into(ivUserPhoto);
//
//                } else {
//                    ivUserPhoto.setImageResource(R.drawable.ic_person);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // On error → load cached image
//                String cachedLogo = prefManager.getLogoUrl();
//
//                if (cachedLogo != null && !cachedLogo.isEmpty()) {
//                    Glide.with(MainActivity.this)
//                            .load(cachedLogo)
//                            .circleCrop()
//                            .placeholder(R.drawable.ic_person)
//                            .into(ivUserPhoto);
//                }
//            }
//        });
//    }
//
//    private void setupBottomNav() {
//        bottomNav.setOnItemSelectedListener(item -> {
//
//            int id = item.getItemId();
//
//            if (id == R.id.nav_dashboard) {
//                loadFragmentSafe(new DashboardFragment(), TAG_DASHBOARD);
//
//            } else if (id == R.id.nav_customers) {
//                loadFragmentSafe(new CustomersFragment(), TAG_CUSTOMERS);
//
//            } else if (id == R.id.nav_reports) {
//                loadFragmentSafe(new ReportsFragment(), TAG_REPORTS);
//
//            } else if (id == R.id.nav_settings) {
//                loadFragmentSafe(new SettingsFragment(), TAG_SETTINGS);
//            }
//
//            updateToolbarTitle(); // Refresh title
//            navigationView.setCheckedItem(id);
//
//            return true;
//        });
//    }
//
//    private void setupFAB() {
//        fabAddTransaction.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, AddTransactionActivity.class))
//        );
//    }
//
//
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//        int id = item.getItemId();
//
//        if (id == R.id.nav_dashboard) {
//            loadFragmentSafe(new DashboardFragment(), TAG_DASHBOARD);
//            bottomNav.setSelectedItemId(id);
//
//        } else if (id == R.id.nav_customers) {
//            loadFragmentSafe(new CustomersFragment(), TAG_CUSTOMERS);
//            bottomNav.setSelectedItemId(id);
//
//        } else if (id == R.id.nav_reports) {
//            loadFragmentSafe(new ReportsFragment(), TAG_REPORTS);
//            bottomNav.setSelectedItemId(id);
//
//        } else if (id == R.id.nav_settings) {
//            loadFragmentSafe(new SettingsFragment(), TAG_SETTINGS);
//            bottomNav.setSelectedItemId(id);
//
//        }
//        // ------------------------
//        // ⭐ OPEN PROFILE FRAGMENT
//        // ------------------------
//        else if (id == R.id.nav_profile) {
//            loadFragmentSafe(new ProfileFragment(), "profile");
//        }
//        // ------------------------
//        // ⭐ OPEN ABOUT ACTIVITY
//        // ------------------------
//        else if (id == R.id.nav_about) {
//            startActivity(new Intent(MainActivity.this, AboutAppActivity.class));
//        }
//
//        else if (id == R.id.nav_switchshop) {
//            startActivity(new Intent(MainActivity.this, SwitchShopActivity.class));
//
//        } else if (id == R.id.nav_logout) {
//            showLogoutDialog();
//        }
//
//        drawerLayout.closeDrawer(GravityCompat.START);
//        updateToolbarTitle();
//        return true;
//    }
//
//    private void loadFragmentSafe(Fragment fragment, String tag) {
//        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
//        tx.replace(R.id.fragment_container, fragment, tag);
//        tx.commit();
//
//        updateToolbarTitle(); // Always update title after fragment change
//    }
//
//    private void showLogoutDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Logout")
//                .setMessage("Are you sure?")
//                .setPositiveButton("Yes", (dialog, which) -> performLogout())
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void performLogout() {
//        mAuth.signOut();
//        prefManager.clearAll();
//
//        Intent i = new Intent(MainActivity.this, LoginActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(i);
//        finish();
//    }
//
//    @Override
//    public void onBackPressed() {
//
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//            return;
//        }
//
//        super.onBackPressed();
//    }
//}
//




package com.sandhyasofttechh.mykhatapro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.sandhyasofttechh.mykhatapro.activities.AboutAppActivity;
import com.sandhyasofttechh.mykhatapro.activities.AddTransactionActivity;
import com.sandhyasofttechh.mykhatapro.fragments.*;
import com.sandhyasofttechh.mykhatapro.register.LoginActivity;
import com.sandhyasofttechh.mykhatapro.register.SwitchShopActivity;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabAddTransaction;

    private PrefManager prefManager;
    private FirebaseAuth mAuth;

    private CircleImageView ivUserPhoto;
    private TextView tvUserName, tvUserEmail;

    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_CUSTOMERS = "customers";
    private static final String TAG_REPORTS = "reports";
    private static final String TAG_SETTINGS = "settings";

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
        setupDrawerHeader(); // This now includes click listener
        loadUserProfile();
        checkIfShopStillExists();
        updateToolbarTitle();

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

    private void checkIfShopStillExists() {
        String shopId = prefManager.getCurrentShopId();
        String email = prefManager.getUserEmail();
        if (shopId == null || shopId.isEmpty() || email == null) return;

        String emailKey = email.replace(".", ",");

        DatabaseReference shopRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("shops")
                .child(shopId);

        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    prefManager.setCurrentShopId("");
                    prefManager.setCurrentShopName("");
                    updateToolbarTitle();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void updateToolbarTitle() {
        String shopName = prefManager.getCurrentShopName();
        setToolbarTitle(shopName != null && !shopName.trim().isEmpty() ? shopName : "Dashboard");
    }

    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title != null && !title.trim().isEmpty() ? title : "Dashboard");
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    // UPDATED: Now includes click on profile image
    private void setupDrawerHeader() {
        View header = navigationView.getHeaderView(0);

        ivUserPhoto = header.findViewById(R.id.iv_user_photo);
        tvUserName = header.findViewById(R.id.tv_user_name);
        tvUserEmail = header.findViewById(R.id.tv_user_email);

        tvUserName.setText("Loading...");
        tvUserEmail.setText("");

        // CLICK ON PROFILE IMAGE → OPEN PROFILE FRAGMENT
        ivUserPhoto.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            loadFragmentSafe(new ProfileFragment(), "profile");
            navigationView.setCheckedItem(R.id.nav_profile);
        });

        // Optional: Also make the whole header clickable
        header.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            loadFragmentSafe(new ProfileFragment(), "profile");
            navigationView.setCheckedItem(R.id.nav_profile);
        });
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        if (email == null) return;

        tvUserEmail.setText(email);

        String emailKey = email.replace(".", ",");
        String shopId = prefManager.getCurrentShopId();
        DatabaseReference ref;

        if (shopId != null && !shopId.isEmpty()) {
            ref = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId)
                    .child("profile");
        } else {
            ref = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("profile");
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String businessName = snapshot.child("businessName").getValue(String.class);
                String name = snapshot.child("name").getValue(String.class);
                String logoUrl = snapshot.child("logoUrl").getValue(String.class);

                if (businessName != null && !businessName.isEmpty()) {
                    tvUserName.setText(businessName);
                } else if (name != null && !name.isEmpty()) {
                    tvUserName.setText(name);
                } else {
                    tvUserName.setText("MyKhata User");
                }

                if (businessName != null) prefManager.saveBusinessName(businessName);
                if (logoUrl != null) prefManager.saveLogoUrl(logoUrl);

                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Glide.with(MainActivity.this)
                            .load(logoUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .into(ivUserPhoto);
                } else {
                    ivUserPhoto.setImageResource(R.drawable.ic_person);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String cachedLogo = prefManager.getLogoUrl();
                if (cachedLogo != null && !cachedLogo.isEmpty()) {
                    Glide.with(MainActivity.this)
                            .load(cachedLogo)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivUserPhoto);
                }
            }
        });
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
            }

            updateToolbarTitle();
            navigationView.setCheckedItem(id);
            return true;
        });
    }

    private void setupFAB() {
        fabAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddTransactionActivity.class))
        );
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
        } else if (id == R.id.nav_profile) {
            loadFragmentSafe(new ProfileFragment(), "profile");
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutAppActivity.class));
        } else if (id == R.id.nav_switchshop) {
            startActivity(new Intent(this, SwitchShopActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        updateToolbarTitle();
        return true;
    }

    private void loadFragmentSafe(Fragment fragment, String tag) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment_container, fragment, tag);
        tx.commit();
        updateToolbarTitle();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        prefManager.clearAll();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
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
}