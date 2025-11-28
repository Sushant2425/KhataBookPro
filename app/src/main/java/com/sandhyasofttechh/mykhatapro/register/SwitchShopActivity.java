package com.sandhyasofttechh.mykhatapro.register;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.sandhyasofttechh.mykhatapro.MainActivity;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class SwitchShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout btnCreateShop, btnWithoutShop;

    private PrefManager prefManager;
    private DatabaseReference shopsRef;

    private List<ShopItem> shopList = new ArrayList<>();
    private ShopAdapter adapter;

    private String emailKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_shop);

        recyclerView = findViewById(R.id.rvShops);
        btnCreateShop = findViewById(R.id.btnCreateShop);
        btnWithoutShop = findViewById(R.id.btnWithoutShop);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());

        prefManager = new PrefManager(this);
        String email = prefManager.getUserEmail();
        emailKey = email.replace(".", ",");

        shopsRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("shops");

        setupRecyclerView();
        loadShops();

        btnCreateShop.setOnClickListener(v -> showCreateShopDialog());

        btnWithoutShop.setOnClickListener(v -> {
            // CLEAR current shop settings
            prefManager.setCurrentShopId("");
            prefManager.setCurrentShopName("");

            Toast.makeText(this, "Using Without Shop", Toast.LENGTH_SHORT).show();

            openMain();
        });
    }


    // ------------------------------------------------------------
    // Recycler Setup
    // ------------------------------------------------------------
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShopAdapter(shopList, shop -> {
            prefManager.setCurrentShopId(shop.id);
            prefManager.setCurrentShopName(shop.name);

            Toast.makeText(this, "Switched to: " + shop.name, Toast.LENGTH_SHORT).show();
            openMain();
        });
        recyclerView.setAdapter(adapter);
    }

    // ------------------------------------------------------------
    // Load Shops From Firebase
    // ------------------------------------------------------------
    private void loadShops() {
        shopsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shopList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String id = ds.getKey();
                    String name = ds.child("name").getValue(String.class);

                    if (name == null) name = id;

                    shopList.add(new ShopItem(id, name));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ------------------------------------------------------------
    // Create New Shop
    // ------------------------------------------------------------
    private void showCreateShopDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter shop name");

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Create New Shop");
        dialog.setView(input);

        dialog.setPositiveButton("Create", (d, w) -> {
            String shopName = input.getText().toString().trim();

            if (shopName.isEmpty()) {
                Toast.makeText(this, "Shop name required", Toast.LENGTH_SHORT).show();
                return;
            }

            createShop(shopName);
        });

        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }

    private void createShop(String shopName) {
        String shopId = shopsRef.push().getKey();

        if (shopId == null) {
            Toast.makeText(this, "Error creating shop", Toast.LENGTH_SHORT).show();
            return;
        }

        shopsRef.child(shopId).child("name").setValue(shopName)
                .addOnSuccessListener(a -> {
                    prefManager.setCurrentShopId(shopId);
                    prefManager.setCurrentShopName(shopName);

                    Toast.makeText(this, "Shop created successfully!", Toast.LENGTH_SHORT).show();
                    openMain();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ------------------------------------------------------------
    // Navigate Back to Main Dashboard
    // ------------------------------------------------------------
    private void openMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    // ------------------------------------------------------------
    // Shop Item Model
    // ------------------------------------------------------------
    public static class ShopItem {
        public String id;
        public String name;

        public ShopItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
