package com.sandhyasofttechh.mykhatapro.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sandhyasofttechh.mykhatapro.R;

public class ReportsFragment extends Fragment {

    LinearLayout rowCustomerReports, rowCustomerList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reports, container, false);


        rowCustomerReports = v.findViewById(R.id.rowCustomerReports);
        rowCustomerList = v.findViewById(R.id.rowCustomerList);

        rowCustomerReports.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CustomerReportFragment())
                    .addToBackStack(null)
                    .commit();
        });

        rowCustomerList.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CustomerListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }
}
