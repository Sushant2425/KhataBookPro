package com.sandhyasofttechh.mykhatapro.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.SettingsOptionsAdapter;
import com.sandhyasofttechh.mykhatapro.model.SettingsItem;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private RecyclerView rvOptions;
    private SettingsOptionsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        rvOptions = view.findViewById(R.id.rv_settings_options);
        rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SettingsItem> items = getSettingsItems();
        adapter = new SettingsOptionsAdapter(items);
        rvOptions.setAdapter(adapter);

        return view;
    }

    private List<SettingsItem> getSettingsItems() {
        List<SettingsItem> list = new ArrayList<>();

        list.add(new SettingsItem("Account", true));
        list.add(new SettingsItem("Change Password", false));
        list.add(new SettingsItem("SMS Settings", false));
        list.add(new SettingsItem("Payment Settings", false));
        list.add(new SettingsItem("", true)); // Divider using empty header

        list.add(new SettingsItem("App", true));
        list.add(new SettingsItem("Recycle Bin", false));
        list.add(new SettingsItem("App Lock", false));
        list.add(new SettingsItem("Help & Support", false));
        list.add(new SettingsItem("About App", false));
        list.add(new SettingsItem("About Us", false));

        return list;
    }
}
