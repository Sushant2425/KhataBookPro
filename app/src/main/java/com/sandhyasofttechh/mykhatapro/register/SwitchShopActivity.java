//package com.sandhyasofttechh.mykhatapro.register;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.*;
//import android.widget.*;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.*;
//import com.sandhyasofttechh.mykhatapro.MainActivity;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.model.Shop;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class SwitchShopActivity extends AppCompatActivity {
//
//    private RecyclerView rvShops;
//    private ShopsAdapter adapter;
//    private List<Shop> shopList = new ArrayList<>();
//    private DatabaseReference shopsRef;
//    private PrefManager prefManager;
//    private String emailKey;
//    private ImageButton btnCreateShop;
//    private ValueEventListener shopsListener;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_switch_shop);
//
//        prefManager = new PrefManager(this);
//        String userEmail = prefManager.getUserEmail();
//        emailKey = userEmail != null ? userEmail.replace(".", ",") : "";
//
//        btnCreateShop = findViewById(R.id.btnCreateShop);
//        rvShops = findViewById(R.id.rvShops);
//        rvShops.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new ShopsAdapter(this, shopList);
//        rvShops.setAdapter(adapter);
//
//        shopsRef = FirebaseDatabase.getInstance()
//                .getReference("Khatabook")
//                .child(emailKey)
//                .child("shops");
//
//        btnCreateShop.setOnClickListener(v -> showCreateShopDialog());
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        loadShops();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (shopsRef != null && shopsListener != null) shopsRef.removeEventListener(shopsListener);
//    }
//
//    private void loadShops() {
//        shopList.clear();
//        adapter.notifyDataSetChanged();
//
//        shopsListener = shopsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                shopList.clear();
//                for (DataSnapshot s : snapshot.getChildren()) {
//                    Shop shop = s.getValue(Shop.class);
//                    if (shop != null) {
//                        shopList.add(shop);
//                    } else {
//                        // fallback if fields saved directly
//                        String id = s.getKey();
//                        String name = s.child("shopName").getValue(String.class);
//                        String created = s.child("createdAt").getValue(String.class);
//                        shopList.add(new Shop(id, name != null ? name : "Shop", created != null ? created : ""));
//                    }
//                }
//                // Sort by createdAt or name
//                Collections.sort(shopList, (a, b) -> (a.getShopName() != null ? a.getShopName() : "")
//                        .compareToIgnoreCase(b.getShopName() != null ? b.getShopName() : ""));
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(SwitchShopActivity.this, "Failed to load shops: " + error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void showCreateShopDialog() {
//        final EditText input = new EditText(this);
//        input.setHint("Shop name");
//
//        new AlertDialog.Builder(this)
//                .setTitle("Create Shop")
//                .setMessage("Enter shop name")
//                .setView(input)
//                .setPositiveButton("Create", (dialog, which) -> {
//                    String name = input.getText().toString().trim();
//                    if (TextUtils.isEmpty(name)) {
//                        Toast.makeText(this, "Enter shop name", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    createShopInFirebase(name);
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void createShopInFirebase(String shopName) {
//        String shopId = shopsRef.push().getKey();
//        if (shopId == null) {
//            Toast.makeText(this, "Error creating shop id", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
//        Shop shop = new Shop(shopId, shopName, createdAt);
//        shopsRef.child(shopId).setValue(shop)
//                .addOnSuccessListener(aVoid -> {
//                    // auto switch to new shop
//                    prefManager.setCurrentShopId(shopId);
//                    prefManager.setCurrentShopName(shopName);
//                    // restart MainActivity to reflect shop selection
//                    Intent i = new Intent(SwitchShopActivity.this, MainActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(i);
//                    finish();
//                })
//                .addOnFailureListener(e -> Toast.makeText(SwitchShopActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
//    }
//
//    private void removeShop(String shopId) {
//        if (shopId == null) return;
//
//        shopsRef.child(shopId).removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    // if deleted shop was current, clear or switch to another
//                    String current = prefManager.getCurrentShopId();
//                    if (shopId.equals(current)) {
//                        prefManager.setCurrentShopId("");
//                        prefManager.setCurrentShopName("");
//                        // optionally set first shop if exists
//                        if (!shopList.isEmpty()) {
//                            Shop first = shopList.get(0);
//                            if (first != null && !first.getShopId().equals(shopId)) {
//                                prefManager.setCurrentShopId(first.getShopId());
//                                prefManager.setCurrentShopName(first.getShopName());
//                            }
//                        }
//                        Intent i = new Intent(SwitchShopActivity.this, MainActivity.class);
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(i);
//                        finish();
//                    } else {
//                        adapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(e -> Toast.makeText(SwitchShopActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
//    }
//
//    // RecyclerView adapter
//    private class ShopsAdapter extends RecyclerView.Adapter<ShopsAdapter.ShopVH> {
//
//        private final Context ctx;
//        private final List<Shop> list;
//
//        ShopsAdapter(Context ctx, List<Shop> list) {
//            this.ctx = ctx;
//            this.list = list;
//        }
//
//        @NonNull
//        @Override
//        public ShopVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(ctx).inflate(R.layout.item_account, parent, false);
//            return new ShopVH(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ShopVH holder, int position) {
//            Shop shop = list.get(position);
//            holder.tvName.setText(shop.getShopName() != null ? shop.getShopName() : "Shop");
//            holder.tvId.setText(shop.getShopId());
//
//            holder.itemView.setOnClickListener(v -> {
//                // switch to this shop
//                prefManager.setCurrentShopId(shop.getShopId());
//                prefManager.setCurrentShopName(shop.getShopName());
//                Toast.makeText(SwitchShopActivity.this, "Switched to " + shop.getShopName(), Toast.LENGTH_SHORT).show();
//                // restart MainActivity so fragments reload
//                Intent i = new Intent(SwitchShopActivity.this, MainActivity.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
//                finish();
//            });
//
//            holder.itemView.setOnLongClickListener(v -> {
//                new AlertDialog.Builder(SwitchShopActivity.this)
//                        .setTitle("Delete shop")
//                        .setMessage("Delete " + shop.getShopName() + " ? This will remove all data under this shop.")
//                        .setPositiveButton("Delete", (dialog, which) -> removeShop(shop.getShopId()))
//                        .setNegativeButton("Cancel", null)
//                        .show();
//                return true;
//            });
//        }
//
//        @Override
//        public int getItemCount() { return list != null ? list.size() : 0; }
//
//        class ShopVH extends RecyclerView.ViewHolder {
//            TextView tvName, tvId;
//            ShopVH(@NonNull View itemView) {
//                super(itemView);
//                tvName = itemView.findViewById(R.id.tvShopName);
//                tvId = itemView.findViewById(R.id.tvShopId);
//            }
//        }
//    }
//}


package com.sandhyasofttechh.mykhatapro.register;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
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
