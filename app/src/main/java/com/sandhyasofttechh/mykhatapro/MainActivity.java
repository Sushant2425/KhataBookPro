package com.sandhyasofttechh.mykhatapro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.sandhyasofttechh.mykhatapro.fragments.*;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;

    private Toolbar toolbar;

    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_CUSTOMERS = "customers";
    private static final String TAG_REPORTS = "reports";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_ABOUT = "about";

    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = new PrefManager(this);

        initViews();
        setupToolbar();
        setupDrawer();
        setupBottomNav();
        setupDrawerHeader();

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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MyKhata Pro");
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
        CircleImageView ivPhoto = header.findViewById(R.id.iv_user_photo);
        TextView tvName = header.findViewById(R.id.tv_user_name);
        TextView tvEmail = header.findViewById(R.id.tv_user_email);

        // Load from PrefManager or Firebase
        tvName.setText(prefManager.getUserEmail());
        tvEmail.setText(prefManager.getUserEmail());
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
            navigationView.setCheckedItem(id);
            return true;
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
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_rate) {
            rateApp();
        } else if (id == R.id.nav_about) {
//            loadFragmentSafe(new AboutFragment(), TAG_ABOUT);
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
        startActivity(rateIntent);
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