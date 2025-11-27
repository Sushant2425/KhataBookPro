//package com.sandhyasofttechh.mykhatapro.fragments;
//
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
//import com.sandhyasofttechh.mykhatapro.activities.CustomerDetailsActivity;
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
//        recyclerView = view.findViewById(R.id.recycler_customers);
//        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
//        searchEditText = view.findViewById(R.id.search_view_customers);
//        tvEmpty = view.findViewById(R.id.tv_empty);
//
//        adapter = new CustomerAdapter(this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(adapter);
//
//        prefManager = new PrefManager(requireContext());
//        String userEmail = prefManager.getUserEmail();
//        customerService = new CustomerService(userEmail);
//
//        loadCustomers();
//
//        fabAddCustomer.setOnClickListener(v ->
//                startActivity(new Intent(getActivity(), AddCustomerActivity.class)));
//
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
//                adapter.setCustomers(new ArrayList<>(allCustomers));
//                updateEmptyState();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Error loading customers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void setupSearch() {
//        searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                adapter.filter(s.toString().trim());
//                updateEmptyStateAfterFilter();
//            }
//        });
//    }
//
//    private void updateEmptyState() {
//        boolean isEmpty = allCustomers.isEmpty();
//        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
//        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
//    }
//
//    private void updateEmptyStateAfterFilter() {
//        boolean isEmpty = adapter.getItemCount() == 0;
//        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
//        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
//    }
//
//    @Override
//    public void onItemClicked(Customer customer) {
//        Intent intent = new Intent(getActivity(), CustomerDetailsActivity.class);
//        intent.putExtra("CUSTOMER_PHONE", customer.getPhone());
//        intent.putExtra("CUSTOMER_NAME", customer.getName());
//        startActivity(intent);
//    }
//}




package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class CustomersFragment extends Fragment implements CustomerAdapter.CustomerActionListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddCustomer;
    private TextInputEditText searchEditText;
    private TextView tvEmpty;

    private CustomerAdapter adapter;
    private PrefManager prefManager;

    private DatabaseReference customersRef;
    private ValueEventListener customerListener;
    private TextView tvCustomerCount;  // Add as class member

    private final List<Customer> allCustomers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customers, container, false);

        recyclerView = view.findViewById(R.id.recycler_customers);
        fabAddCustomer = view.findViewById(R.id.fab_add_customer);
        searchEditText = view.findViewById(R.id.search_view_customers);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvCustomerCount = view.findViewById(R.id.tv_customer_count);

        prefManager = new PrefManager(requireContext());
        adapter = new CustomerAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        initFirebaseRef();
        loadCustomers();
        setupSearch();

        fabAddCustomer.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddCustomerActivity.class)));

        return view;
    }
    private void updateCustomerCount(int count) {
        tvCustomerCount.setText("Total Customers: " + count);
    }

    /**
     * SHOP–WISE / NO–SHOP Firebase customer reference
     */
    private void initFirebaseRef() {
        String email = prefManager.getUserEmail();
        if (email == null) return;

        String emailKey = email.replace(".", ",");
        String shopId = prefManager.getCurrentShopId();

        if (shopId == null || shopId.isEmpty()) {
            // ⭐ NO SHOP SELECTED – load from old path
            customersRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("customers");
        } else {
            // ⭐ SHOP SELECTED – load inside shop
            customersRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId)
                    .child("customers");
        }
    }



    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString().trim());
                updateEmptyStateAfterFilter();
                updateCustomerCount(adapter.getItemCount());

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
    public void onItemClicked(Customer customer) {
        Intent intent = new Intent(getActivity(), CustomerDetailsActivity.class);
        intent.putExtra("CUSTOMER_PHONE", customer.getPhone());
        intent.putExtra("CUSTOMER_NAME", customer.getName());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (customersRef != null && customerListener != null) {
            customersRef.removeEventListener(customerListener);
        }
    }
}
