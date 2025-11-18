package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.fragments.CustomerReportFragment.CustomerReport;
import java.util.List;

public class CustomerReportAdapter extends RecyclerView.Adapter<CustomerReportAdapter.ViewHolder> {

    public interface OnExportClickListener {
        void onExportClicked(CustomerReport report);
    }

    private final List<CustomerReport> list;
    private final OnExportClickListener listener;

    public CustomerReportAdapter(List<CustomerReport> l, OnExportClickListener lis) {
        this.list = l; this.listener = lis;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new ViewHolder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_customer_report, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CustomerReport r = list.get(pos);
        h.tvName.setText(r.getName() + " (" + r.getPhone() + ")");
        h.tvBalance.setText(String.format("Balance: ₹%.2f", r.getBalance()));
        h.btnExport.setOnClickListener(v -> listener.onExportClicked(r));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBalance; Button btnExport;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvBalance = v.findViewById(R.id.tvBalance);
            btnExport = v.findViewById(R.id.btnExport);
        }
    }
}