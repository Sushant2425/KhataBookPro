package com.sandhyasofttechh.mykhatapro.fragments;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.AddServiceActivity;
import com.sandhyasofttechh.mykhatapro.activities.ServiceDetailActivity;
import com.sandhyasofttechh.mykhatapro.adapter.ServiceAdapter;
import com.sandhyasofttechh.mykhatapro.model.ServiceModel;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ServicesFragment extends Fragment {

    FloatingActionButton fabAddService;
    RecyclerView recyclerServices;
    DatabaseReference serviceRef;

    EditText edtSearch;
    TextView txtTotalCount;

    List<ServiceModel> completeList = new ArrayList<>();
    List<ServiceModel> filteredList = new ArrayList<>();

    ServiceAdapter adapter;
    PrefManager pref;

    public ServicesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_services, container, false);

        pref = new PrefManager(requireContext());

        recyclerServices = view.findViewById(R.id.recyclerServices);
        fabAddService = view.findViewById(R.id.fabAddService);

        edtSearch = view.findViewById(R.id.searchService);
        txtTotalCount = view.findViewById(R.id.txtServiceCount);

        recyclerServices.setLayoutManager(new LinearLayoutManager(getContext()));

        // *** UPDATED ADAPTER INITIALIZATION ***
        adapter = new ServiceAdapter(filteredList, model -> {
            Intent intent = new Intent(getContext(), ServiceDetailActivity.class);
            intent.putExtra("serviceData", model);
            startActivity(intent);
        });
        recyclerServices.setAdapter(adapter);

        initFirebase();
        loadServices();
        setupSearch();

        fabAddService.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddServiceActivity.class)));

        return view;
    }

    private void initFirebase() {
        String emailKey = pref.getUserEmail().replace(".", ",");
        String shopId = pref.getCurrentShopId();

        if (shopId == null || shopId.trim().isEmpty()) {
            serviceRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("services");
        } else {
            serviceRef = FirebaseDatabase.getInstance()
                    .getReference("Khatabook")
                    .child(emailKey)
                    .child("shops")
                    .child(shopId)
                    .child("services");
        }
    }

    private void loadServices() {
        serviceRef.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {

                        completeList.clear();
                        filteredList.clear();

                        for (DataSnapshot snap : ds.getChildren()) {
                            ServiceModel model = snap.getValue(ServiceModel.class);

                            if (model != null && model.serviceName != null)
                                completeList.add(model);
                        }

                        filteredList.addAll(completeList);
                        adapter.notifyDataSetChanged();

                        txtTotalCount.setText("Total Services: " + filteredList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start,int c,int a){}
            @Override public void afterTextChanged(Editable s){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toLowerCase();

                filteredList.clear();

                for (ServiceModel m : completeList) {
                    if (m.serviceName.toLowerCase().contains(text)) {
                        filteredList.add(m);
                    }
                }

                adapter.notifyDataSetChanged();
                txtTotalCount.setText("Total Services: " + filteredList.size());
            }
        });
    }
}
