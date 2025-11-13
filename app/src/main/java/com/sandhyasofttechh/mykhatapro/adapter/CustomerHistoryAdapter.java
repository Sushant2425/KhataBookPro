package com.sandhyasofttechh.mykhatapro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.PaymentEntry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerHistoryAdapter extends RecyclerView.Adapter<CustomerHistoryAdapter.HistoryViewHolder> {

    private final List<PaymentEntry> entries = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public void setEntries(List<PaymentEntry> list) {
        entries.clear();
        if (list != null) entries.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        PaymentEntry e = entries.get(position);

        holder.tvType.setText(e.getType().equals("gave") ? "You Gave" : "You Got");
        holder.tvAmount.setText(String.format("₹%.2f", e.getAmount()));
        holder.tvDate.setText(sdf.format(new Date(e.getDate())));

        if (e.getNote() == null || e.getNote().trim().isEmpty()) {
            holder.tvNote.setVisibility(View.GONE);
        } else {
            holder.tvNote.setText(e.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        }

        int iconRes = e.getType().equals("gave") ? R.drawable.ic_gave : R.drawable.ic_got;
        int color = e.getType().equals("gave")
                ? holder.itemView.getContext().getColor(R.color.red)
                : holder.itemView.getContext().getColor(R.color.green);

        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setColorFilter(color);
        holder.tvAmount.setTextColor(color);
        holder.tvType.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvDate, tvNote;
        ImageView ivIcon;

        HistoryViewHolder(View v) {
            super(v);
            tvType = v.findViewById(R.id.tv_history_type);
            tvAmount = v.findViewById(R.id.tv_history_amount);
            tvDate = v.findViewById(R.id.tv_history_date);
            tvNote = v.findViewById(R.id.tv_history_note);
            ivIcon = v.findViewById(R.id.iv_history_icon);
        }
    }
}