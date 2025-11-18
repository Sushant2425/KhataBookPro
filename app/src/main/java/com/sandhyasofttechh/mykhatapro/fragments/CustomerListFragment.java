package com.sandhyasofttechh.mykhatapro.fragments;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.CustomerBalanceAdapter;
import com.sandhyasofttechh.mykhatapro.utils.PdfUtils;
import com.sandhyasofttechh.mykhatapro.utils.TableDrawer;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class CustomerListFragment extends Fragment {

    private static final String TAG = "CustomerListFragment";

    private RecyclerView rvCustomerList;
    private FloatingActionButton fabExportAll;
    private DatabaseReference customerRef, transRef;
    private List<CustomerSummary> customerSummaries = new ArrayList<>();
    private CustomerBalanceAdapter adapter;

    private int totalCustomers = 0;
    private int loadedCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_list, container, false);

        rvCustomerList = v.findViewById(R.id.rvCustomerList);
        fabExportAll = v.findViewById(R.id.fabExportAll);
        rvCustomerList.setLayoutManager(new LinearLayoutManager(getContext()));

        PrefManager prefManager = new PrefManager(requireContext());
        String userEmail = prefManager.getUserEmail();

        if (userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return v;
        }

        String emailKey = userEmail.replace(".", ",");

        String basePath = "Khatabook/" + emailKey;
        customerRef = FirebaseDatabase.getInstance().getReference(basePath + "/customers");
        transRef = FirebaseDatabase.getInstance().getReference(basePath + "/transactions");

        fabExportAll.setOnClickListener(view -> exportCustomerListPdf());

        loadCustomersWithBalances();

        return v;
    }

    private void loadCustomersWithBalances() {
        customerRef.get().addOnSuccessListener(customerSnap -> {
            customerSummaries.clear();
            totalCustomers = (int) customerSnap.getChildrenCount();
            loadedCount = 0;

            if (totalCustomers == 0) {
                Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show();
                updateAdapter();
                return;
            }

            for (DataSnapshot c : customerSnap.getChildren()) {
                String phone = c.child("phone").getValue(String.class);
                String name = c.child("name").getValue(String.class);

                if (phone == null || name == null) {
                    Log.w(TAG, "Invalid customer data: " + c.getKey());
                    continue;
                }

                fetchBalanceForCustomer(name, phone);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load customers: " + e.getMessage(), Toast.LENGTH_LONG).show();
            updateAdapter();
        });
    }

    private void fetchBalanceForCustomer(String name, String phone) {
        transRef.child(phone).get().addOnSuccessListener(tsnap -> {
            double gave = 0, received = 0;

            for (DataSnapshot t : tsnap.getChildren()) {
                Double amt = t.child("amount").getValue(Double.class);
                String type = t.child("type").getValue(String.class);

                if (amt == null) amt = 0.0;
                if ("gave".equalsIgnoreCase(type)) gave += amt;
                else if ("received".equalsIgnoreCase(type)) received += amt;
            }

            double balance = received - gave;
            customerSummaries.add(new CustomerSummary(name, phone, balance));
            loadedCount++;

            if (loadedCount == totalCustomers)
                updateAdapter();
        }).addOnFailureListener(e -> {
            customerSummaries.add(new CustomerSummary(name, phone, 0.0));
            loadedCount++;
            if (loadedCount == totalCustomers)
                updateAdapter();
        });
    }

    private void updateAdapter() {
        adapter = new CustomerBalanceAdapter(customerSummaries);
        rvCustomerList.setAdapter(adapter);
    }

    private void exportCustomerListPdf() {
        if (customerSummaries.isEmpty()) {
            Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show();
            return;
        }
        PdfDocument document = new PdfDocument();
        PdfUtils utils = new PdfUtils(requireContext(), document);
        PdfUtils.Page page = utils.startPage();
        TableDrawer drawer = new TableDrawer(page.canvas, utils);

        drawer.drawHeader("Customer List Report", "", "");
        
        // Draw table headers explicitly for customer list
        drawer.drawCustomerListTable(customerSummaries);

        utils.finishPage(page);

        utils.saveAndShare("Customer_List_Report.pdf");
    }

    public static class CustomerSummary {
        public final String name;
        public final String phone;
        public final double balance;

        public CustomerSummary(String name, String phone, double balance) {
            this.name = name;
            this.phone = phone;
            this.balance = balance;
        }
    }
}
