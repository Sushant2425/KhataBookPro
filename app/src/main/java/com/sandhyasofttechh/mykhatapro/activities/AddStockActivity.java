package com.sandhyasofttechh.mykhatapro.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.StockPagerAdapter;

public class AddStockActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ExtendedFloatingActionButton fabAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        initViews();
        setupToolbar();
        setupViewPager();
        setupFab();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabStock);
        viewPager = findViewById(R.id.viewPagerStock);
        fabAddProduct = findViewById(R.id.fabAddProduct);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddStock);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        StockPagerAdapter adapter = new StockPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Products" : "Services")
        ).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Show FAB only on PRODUCTS tab
                fabAddProduct.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupFab() {
        fabAddProduct.setOnClickListener(v ->
                startActivity(new Intent(AddStockActivity.this, AddProductActivity.class))
        );
    }
}
