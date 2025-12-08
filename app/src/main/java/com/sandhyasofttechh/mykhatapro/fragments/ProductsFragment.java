package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.ProductReportActivity;
import com.sandhyasofttechh.mykhatapro.adapter.ProductAdapter;
import com.sandhyasofttechh.mykhatapro.model.Product;
import com.sandhyasofttechh.mykhatapro.model.StockHistory;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductsFragment extends Fragment {

    private RecyclerView rvProducts;
    private SearchView searchView;
    private Button btnFilter;

    private MaterialButtonToggleGroup toggleGroupFilter;
    private MaterialButton btnAll, btnLowStock;

    private TextView tvTotalStockValue, tvLowStockCount;

    private MaterialCardView cardViewReport, cardLowStock, cardTotalValue;

    private DatabaseReference productsRef;
    private DatabaseReference historyRef;
    private ValueEventListener productsListener;

    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();
    private ProductAdapter adapter;

    private String searchFilter = "";
    private FilterMode mode = FilterMode.ALL;
    private String emailKey = "";

    private enum FilterMode {
        ALL,
        LOW_STOCK
    }

    public ProductsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_products, container, false);

        initViews(view);
        initFirebase();
        initRecycler();
        initSearch();
        initFilterButtons();
        initHeaderActions();
        loadProducts();

        return view;
    }

    private void initViews(View v) {
        rvProducts = v.findViewById(R.id.rvProducts);
        searchView = v.findViewById(R.id.searchView);
        btnFilter = v.findViewById(R.id.btnFilter);

        toggleGroupFilter = v.findViewById(R.id.toggleGroupFilter);
        btnAll = v.findViewById(R.id.btnAllItems);
        btnLowStock = v.findViewById(R.id.btnLowStock);

        tvTotalStockValue = v.findViewById(R.id.tvTotalStockValue);
        tvLowStockCount = v.findViewById(R.id.tvLowStockCount);

        cardViewReport = v.findViewById(R.id.cardViewReport);
        cardLowStock = v.findViewById(R.id.cardLowStock);
        cardTotalValue = v.findViewById(R.id.cardTotalValue);
    }

    private void initFirebase() {
        PrefManager pref = new PrefManager(requireContext());
        String email = pref.getUserEmail();
        String shop = pref.getCurrentShopId();

        emailKey = email.replace(".", ",");

        if (shop == null || shop.trim().isEmpty()) {
            shop = "defaultShop";
        }

        DatabaseReference base = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("shops")
                .child(shop);

        productsRef = base.child("products");
        historyRef = base.child("history");
    }

    private void initRecycler() {
        adapter = new ProductAdapter(requireContext(), filteredProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(adapter);

        adapter.setStockActionListener(new ProductAdapter.StockActionListener() {
            @Override
            public void onStockIn(Product product) {
                openStockInSheet(product);
            }

            @Override
            public void onStockOut(Product product) {
                openStockOutSheet(product);
            }
        });
    }

    private void initSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                searchFilter = query.toLowerCase(Locale.getDefault());
                applyFilters();
                return true;
            }

            @Override public boolean onQueryTextChange(String newText) {
                searchFilter = newText.toLowerCase(Locale.getDefault());
                applyFilters();
                return true;
            }
        });




            cardLowStock.setOnClickListener(v ->
                    toggleGroupFilter.check(R.id.btnLowStock)
            );


    }

    private void initFilterButtons() {
        toggleGroupFilter.check(R.id.btnAllItems);
        mode = FilterMode.ALL;

        toggleGroupFilter.addOnButtonCheckedListener((group, id, isChecked) -> {
            if (!isChecked) return;

            if (id == R.id.btnAllItems) mode = FilterMode.ALL;
            else if (id == R.id.btnLowStock) mode = FilterMode.LOW_STOCK;

            applyFilters();
        });
    }

    private void initHeaderActions() {
        cardViewReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProductReportActivity.class);
            startActivity(intent);
        });

        cardLowStock.setOnClickListener(v ->
                toggleGroupFilter.check(R.id.btnLowStock)
        );
    }

    private void loadProducts() {
        productsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Product p = new Product();

                    p.setProductId(getString(ds.child("productId").getValue()));
                    p.setName(getString(ds.child("name").getValue()));
                    p.setUnit(getString(ds.child("unit").getValue()));

                    p.setSalePrice(getString(ds.child("salePrice").getValue()));
                    p.setPurchasePrice(getString(ds.child("purchasePrice").getValue()));

                    p.setOpeningStock(getString(ds.child("openingStock").getValue()));
                    p.setCurrentStock(getString(ds.child("currentStock").getValue()));
                    p.setLowStockAlert(getString(ds.child("lowStockAlert").getValue()));

                    p.setHsnCode(getString(ds.child("hsn").getValue()));
                    p.setGstRate(getString(ds.child("gst").getValue()));

                    p.setImageUrl(getString(ds.child("imageUrl").getValue()));
                    p.setDateAdded(getString(ds.child("dateAdded").getValue()));

                    p.setTimestamp(getLong(ds.child("timestamp").getValue()));

                    if (p.getName() != null && !p.getName().isEmpty()) {
                        allProducts.add(p);
                    }
                }

                updateHeaderData();
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        };

        productsRef.addValueEventListener(productsListener);
    }

    private String getString(Object value) {
        if (value == null) return "";
        try {
            return String.valueOf(value);
        } catch (Exception e) {
            return "";
        }
    }

    private long getLong(Object value) {
        if (value == null) return 0L;
        try {
            if (value instanceof Long) return (Long) value;
            if (value instanceof Integer) return ((Integer) value).longValue();
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private void updateHeaderData() {
        double totalValue = 0;
        int low = 0;

        for (Product p : allProducts) {
            totalValue += (p.getOpeningStockDouble() * p.getPurchasePriceDouble());

            if (p.getOpeningStockDouble() <= p.getLowStockAlertDouble())
                low++;
        }

        tvLowStockCount.setText(String.valueOf(low));
        tvTotalStockValue.setText("₹" + totalValue);
    }

    private void applyFilters() {
        filteredProducts.clear();

        for (Product p : allProducts) {
            if (!searchFilter.isEmpty()) {
                if (!p.getName().toLowerCase().contains(searchFilter)
                        && !p.getUnit().toLowerCase().contains(searchFilter)) {
                    continue;
                }
            }

            if (mode == FilterMode.LOW_STOCK
                    && !(p.getOpeningStockDouble() <= p.getLowStockAlertDouble())) {
                continue;
            }

            filteredProducts.add(p);
        }

        adapter.notifyDataSetChanged();
    }

    //  ---------------- STOCK IN LOGIC -----------------
    private void openStockInSheet(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.bottomsheet_stock_in, null);
        dialog.setContentView(v);
        dialog.show();

        // Views
        EditText etQty = v.findViewById(R.id.etQty);
        EditText etPurchasePrice = v.findViewById(R.id.etPurchasePrice);
        EditText etNote = v.findViewById(R.id.etNote);
        EditText etDate = v.findViewById(R.id.etDate);
        TextView tvUnit = v.findViewById(R.id.tvUnit);
        Button btnSave = v.findViewById(R.id.btnSaveStockIn);

        // Set existing unit
        tvUnit.setText(product.getUnit());

        // Pre-fill purchase price
        if (product.getPurchasePrice() != null && !product.getPurchasePrice().isEmpty())
            etPurchasePrice.setText(product.getPurchasePrice());

        // Auto today date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));

        btnSave.setOnClickListener(btn -> {
            String qtyStr = etQty.getText().toString().trim();
            String priceStr = etPurchasePrice.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (qtyStr.isEmpty()) {
                etQty.setError("Enter quantity");
                return;
            }

            if (priceStr.isEmpty()) {
                etPurchasePrice.setError("Enter purchase price");
                return;
            }

            double qty = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);
            double oldStock = product.getOpeningStockDouble();
            double newStock = oldStock + qty;

            // Create stock history entry
            StockHistory history = new StockHistory();
            history.setProductId(product.getProductId());
            history.setProductName(product.getName());
            history.setType("IN");
            history.setQuantity(String.valueOf(qty));
            history.setPrice(String.valueOf(price));
            history.setDate(date);

            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            history.setTime(sdfTime.format(new Date()));

            history.setNote(note.isEmpty() ? "Stock Added" : note);
            history.setOldStock(String.valueOf(oldStock));
            history.setNewStock(String.valueOf(newStock));
            history.setTimestamp(System.currentTimeMillis());

            // Generate unique history ID
            String historyId = historyRef.child(product.getProductId()).push().getKey();

            if (historyId == null) {
                Toast.makeText(requireContext(), "Failed to generate history ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update stock and save history
            productsRef.child(product.getProductId())
                    .child("openingStock")
                    .setValue(String.valueOf(newStock))
                    .addOnSuccessListener(a -> {
                        // Save transaction history
                        historyRef.child(product.getProductId())
                                .child(historyId)
                                .setValue(history)
                                .addOnSuccessListener(h -> {
                                    Toast.makeText(requireContext(), "Stock In saved successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to save history", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to update stock", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    //  ---------------- STOCK OUT LOGIC -----------------
    private void openStockOutSheet(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.bottomsheet_stock_out, null);
        dialog.setContentView(v);
        dialog.show();

        // Views
        EditText etQty = v.findViewById(R.id.etQty);
        EditText etSalePrice = v.findViewById(R.id.etSalePrice);
        EditText etNote = v.findViewById(R.id.etNote);
        EditText etDate = v.findViewById(R.id.etDate);
        TextView tvUnit = v.findViewById(R.id.tvUnit);
        Button btnSave = v.findViewById(R.id.btnSaveStockOut);

        // Set existing unit
        tvUnit.setText(product.getUnit());

        // Pre-fill sale price
        if (product.getSalePrice() != null && !product.getSalePrice().isEmpty())
            etSalePrice.setText(product.getSalePrice());

        // Auto today date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));

        btnSave.setOnClickListener(btn -> {
            String qtyStr = etQty.getText().toString().trim();
            String priceStr = etSalePrice.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (qtyStr.isEmpty()) {
                etQty.setError("Enter quantity");
                return;
            }

            if (priceStr.isEmpty()) {
                etSalePrice.setError("Enter sale price");
                return;
            }

            double qty = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);
            double oldStock = product.getOpeningStockDouble();

            if (qty > oldStock) {
                Toast.makeText(requireContext(), "Not enough stock quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            double newStock = oldStock - qty;

            // Create stock history entry
            StockHistory history = new StockHistory();
            history.setProductId(product.getProductId());
            history.setProductName(product.getName());
            history.setType("OUT");
            history.setQuantity(String.valueOf(qty));
            history.setPrice(String.valueOf(price));
            history.setDate(date);

            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            history.setTime(sdfTime.format(new Date()));

            history.setNote(note.isEmpty() ? "Stock Reduced" : note);
            history.setOldStock(String.valueOf(oldStock));
            history.setNewStock(String.valueOf(newStock));
            history.setTimestamp(System.currentTimeMillis());

            // Generate unique history ID
            String historyId = historyRef.child(product.getProductId()).push().getKey();

            if (historyId == null) {
                Toast.makeText(requireContext(), "Failed to generate history ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update stock and save history
            productsRef.child(product.getProductId())
                    .child("openingStock")
                    .setValue(String.valueOf(newStock))
                    .addOnSuccessListener(a -> {
                        // Save transaction history
                        historyRef.child(product.getProductId())
                                .child(historyId)
                                .setValue(history)
                                .addOnSuccessListener(h -> {
                                    Toast.makeText(requireContext(), "Stock Out saved successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to save history", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to update stock", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (productsRef != null && productsListener != null) {
            productsRef.removeEventListener(productsListener);
        }
    }
}
