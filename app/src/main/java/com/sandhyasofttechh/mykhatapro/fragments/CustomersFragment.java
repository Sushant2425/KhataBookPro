//package com.sandhyasofttechh.mykhatapro.fragments;
//
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.ValueEventListener;
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.activities.AddCustomerActivity;
//import com.sandhyasofttechh.mykhatapro.adapter.CustomerAdapter;
//import com.sandhyasofttechh.mykhatapro.model.Customer;
//import com.sandhyasofttechh.mykhatapro.model.CustomerService;
//import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CustomersFragment extends Fragment implements CustomerAdapter.CustomerActionListener {
//
//    private RecyclerView recyclerView;
//    private FloatingActionButton fabAddCustomer;
//    private TextInputEditText searchEditText;
//    private TextView tvEmpty;
//
//    private CustomerAdapter adapter;
//    private CustomerService customerService;
//    private PrefManager prefManager;
//
//    private final List<Customer> allCustomers = new ArrayList<>();
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_customers, container, false);
//
//        // Initialize views
//        recyclerView = view.findViewById(R.id.recycler_customers);
//        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
//        searchEditText = view.findViewById(R.id.search_view_customers);
//        tvEmpty = view.findViewById(R.id.tv_empty);
//
//        // Setup RecyclerView
//        adapter = new CustomerAdapter(this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(adapter);
//
//        // Initialize services
//        prefManager = new PrefManager(requireContext());
//        String userEmail = prefManager.getUserEmail();
//        customerService = new CustomerService(userEmail);
//
//        // Load data
//        loadCustomers();
//
//        // FAB click
//        fabAddCustomer.setOnClickListener(v ->
//                startActivity(new Intent(getActivity(), AddCustomerActivity.class)));
//
//        // Search setup
//        setupSearch();
//
//        return view;
//    }
//
//    private void loadCustomers() {
//        customerService.getCustomers(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                allCustomers.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Customer customer = ds.getValue(Customer.class);
//                    if (customer != null) {
//                        allCustomers.add(customer);
//                    }
//                }
//
//                // Update adapter
//                adapter.setCustomers(new ArrayList<>(allCustomers));
//
//                // Update empty state
//                updateEmptyState();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(),
//                        "Failed to load customers: " + error.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void setupSearch() {
//        searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String query = s.toString().trim();
//                adapter.filter(query);
//                updateEmptyStateAfterFilter();
//            }
//        });
//
//        // Optional: Clear focus on start
//        searchEditText.clearFocus();
//    }
//
//    private void updateEmptyState() {
//        if (allCustomers.isEmpty()) {
//            tvEmpty.setVisibility(View.VISIBLE);
//            recyclerView.setVisibility(View.GONE);
//        } else {
//            tvEmpty.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void updateEmptyStateAfterFilter() {
//        boolean isEmpty = adapter.getItemCount() == 0;
//        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
//        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
//    }
//
//    // === Customer Actions ===
//    @Override
//    public void onEditClicked(Customer customer) {
//        Intent intent = new Intent(getActivity(), AddCustomerActivity.class);
//        intent.putExtra("edit_customer_name", customer.getName());
//        intent.putExtra("edit_customer_phone", customer.getPhone());
//        intent.putExtra("edit_customer_email", customer.getEmail());
//        intent.putExtra("edit_customer_address", customer.getAddress());
//        startActivity(intent);
//    }
//
//    @Override
//    public void onDeleteClicked(Customer customer) {
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Delete Customer")
//                .setMessage("Are you sure you want to delete " + customer.getName() + "?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    customerService.deleteCustomer(customer.getPhone());
//                    Toast.makeText(getContext(), "Customer deleted", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//}


package com.sandhyasofttechh.mykhatapro.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.AddCustomerActivity;
import com.sandhyasofttechh.mykhatapro.activities.CustomerDetailsActivity;
import com.sandhyasofttechh.mykhatapro.adapter.CustomerAdapter;
import com.sandhyasofttechh.mykhatapro.model.Customer;
import com.sandhyasofttechh.mykhatapro.model.Reminder;
import com.sandhyasofttechh.mykhatapro.model.CustomerService;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class CustomersFragment extends Fragment implements CustomerAdapter.CustomerActionListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddCustomer;
    private TextInputEditText searchEditText;
    private TextView tvEmpty;

    private CustomerAdapter adapter;
    private CustomerService customerService;
    private PrefManager prefManager;

    private final List<Customer> allCustomers = new ArrayList<>();
    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customers, container, false);

        recyclerView = view.findViewById(R.id.recycler_customers);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        searchEditText = view.findViewById(R.id.search_view_customers);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new CustomerAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        prefManager = new PrefManager(requireContext());
        String userEmail = prefManager.getUserEmail();
        customerService = new CustomerService(userEmail);

        loadCustomers();

        fabAddCustomer.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddCustomerActivity.class)));

        setupSearch();

        return view;
    }
    @Override
    public void onItemClicked(Customer customer) {
        Intent intent = new Intent(getActivity(), CustomerDetailsActivity.class);
        intent.putExtra("customer", customer);
        startActivity(intent);
    }

    private void loadCustomers() {
        customerService.getCustomers(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCustomers.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Customer customer = ds.getValue(Customer.class);
                    if (customer != null) {
                        customer.setCustomerId(ds.getKey());

                        DatabaseReference transRef = FirebaseDatabase.getInstance().getReference("Khatabook")
                                .child(prefManager.getUserEmail().replace(".", ","))
                                .child("transactions");

                        transRef.orderByChild("customerPhone").equalTo(customer.getPhone())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot transSnap) {
                                        double totalDue = 0;

                                        for (DataSnapshot t : transSnap.getChildren()) {
                                            String type = t.child("type").getValue(String.class);
                                            Double amount = t.child("amount").getValue(Double.class);
                                            if (amount == null) continue;

                                            if ("gave".equals(type)) {
                                                totalDue += amount;
                                            } else if ("got".equals(type)) {
                                                totalDue -= amount;
                                            }
                                        }

                                        customer.setPendingAmount(totalDue > 0 ? totalDue : 0);

                                        if (!allCustomers.contains(customer)) {
                                            allCustomers.add(customer);
                                        }
                                        adapter.setCustomers(new ArrayList<>(allCustomers));
                                        updateEmptyState();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.filter(s.toString().trim());
                updateEmptyStateAfterFilter();
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = allCustomers.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyStateAfterFilter() {
        boolean isEmpty = adapter.getItemCount() == 0;
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEditClicked(Customer customer) {
        Intent intent = new Intent(getActivity(), AddCustomerActivity.class);
        intent.putExtra("edit_customer_id", customer.getCustomerId());
        intent.putExtra("edit_customer_name", customer.getName());
        intent.putExtra("edit_customer_phone", customer.getPhone());
        intent.putExtra("edit_customer_email", customer.getEmail());
        intent.putExtra("edit_customer_address", customer.getAddress());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(Customer customer) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Customer")
                .setMessage("Delete " + customer.getName() + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    customerService.deleteCustomer(customer.getCustomerId());
                    Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // NEW: WhatsApp Click
    @Override
    public void onWhatsAppClicked(Customer customer) {
        String phone = customer.getPhone().replaceAll("[^0-9]", ""); // Clean number
        if (!phone.startsWith("+91") && phone.length() == 10) {
            phone = "+91" + phone;
        }

        String message = String.format(
                "Hi %s,\n\nYou have ₹%.2f pending with us.\nPlease pay at your earliest.\n\nThank you!\n- MyKhata Pro",
                customer.getName(), customer.getPendingAmount()
        );

        try {
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.whatsapp");
            startActivity(intent);
            logReminder(customer, "WhatsApp");
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSmsClicked(Customer customer) {
        String phone = customer.getPhone().replaceAll("[^0-9]", "");
        if (!phone.startsWith("+91") && phone.length() == 10) {
            phone = "+91" + phone;
        }

        String message = String.format(
                "Hi %s,\nYou have ₹%.2f pending with us.\nPlease pay soon.\n- MyKhata Pro",
                customer.getName(), customer.getPendingAmount()
        );

        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phone));
            smsIntent.putExtra("sms_body", message);
            startActivity(smsIntent);
            logReminder(customer, "SMS");
        } catch (Exception e) {
            Toast.makeText(getContext(), "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }


    private void logReminder(Customer customer, String method) {
        Reminder reminder = new Reminder(method, System.currentTimeMillis());
        customerService.logReminder(customer.getCustomerId(), reminder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "SMS permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}