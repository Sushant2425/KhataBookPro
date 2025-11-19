package com.sandhyasofttechh.mykhatapro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.adapter.SimSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class SimSelectionBottomSheet extends BottomSheetDialogFragment {

    public interface SimSelectionListener {
        void onSimSelected(int subscriptionId, String simName);
    }

    private SimSelectionListener listener;
    private List<SubscriptionInfo> simList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_sim_selection, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_sim);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SimSelectionAdapter adapter = new SimSelectionAdapter(simList, sim -> {
            if (listener != null) {
                listener.onSimSelected(sim.getSubscriptionId(), sim.getDisplayName().toString());
                dismiss();
            }
        });
        recyclerView.setAdapter(adapter);

        loadSimList();

        return view;
    }

    private void loadSimList() {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_PHONE_STATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Permission should be handled where this fragment is used
            return;
        }
        List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList != null) {
            simList.clear();
            simList.addAll(activeSubscriptionInfoList);
            // Notify adapter to refresh list
            View view = getView();
            if (view != null) {
                RecyclerView recyclerView = view.findViewById(R.id.recycler_view_sim);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SimSelectionListener) {
            listener = (SimSelectionListener) context;
        } else if (getParentFragment() instanceof SimSelectionListener) {
            listener = (SimSelectionListener) getParentFragment();
        } else {
            throw new RuntimeException("Hosting activity or fragment must implement SimSelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
