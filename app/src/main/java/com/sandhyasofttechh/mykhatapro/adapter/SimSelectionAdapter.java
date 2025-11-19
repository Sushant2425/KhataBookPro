package com.sandhyasofttechh.mykhatapro.adapter;

import android.telephony.SubscriptionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;

import java.util.List;

public class SimSelectionAdapter extends RecyclerView.Adapter<SimSelectionAdapter.ViewHolder> {

    public interface OnSimSelectedListener {
        void onSimSelected(SubscriptionInfo simInfo);
    }

    private final List<SubscriptionInfo> simList;
    private final OnSimSelectedListener listener;

    public SimSelectionAdapter(List<SubscriptionInfo> list, OnSimSelectedListener listener) {
        this.simList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SimSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sim_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimSelectionAdapter.ViewHolder holder, int position) {
        SubscriptionInfo sim = simList.get(position);
        holder.tvSimName.setText(sim.getDisplayName());
        holder.itemView.setOnClickListener(v -> listener.onSimSelected(sim));
    }

    @Override
    public int getItemCount() {
        return simList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSimName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSimName = itemView.findViewById(R.id.tv_sim_name);
        }
    }
}
