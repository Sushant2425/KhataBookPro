package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.AboutUsActivity;
import com.sandhyasofttechh.mykhatapro.activities.ChangePasswordActivity;
import com.sandhyasofttechh.mykhatapro.activities.HelpSupportActivity;
import com.sandhyasofttechh.mykhatapro.activities.RecycleBinActivity;
import com.sandhyasofttechh.mykhatapro.model.SettingsItem;

import android.widget.TextView;

import java.util.List;

public class SettingsOptionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_OPTION = 1;
    private static final int TYPE_DIVIDER = 2;

    private List<SettingsItem> items;

    public SettingsOptionsAdapter(List<SettingsItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        SettingsItem item = items.get(position);
        if (item.isHeader()) {
            return TYPE_HEADER;
        } else if (item.getTitle().isEmpty()) {
            return TYPE_DIVIDER;
        } else {
            return TYPE_OPTION;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_setting_header, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_DIVIDER) {
            View v = inflater.inflate(R.layout.item_divider, parent, false);
            return new DividerViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_setting_option, parent, false);
            return new OptionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingsItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof DividerViewHolder) {
            // No bind needed
        } else if (holder instanceof OptionViewHolder) {
            ((OptionViewHolder) holder).bind(item);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(@NonNull View v) {
            super(v);
            tvHeader = v.findViewById(R.id.tv_header_title);
        }
        void bind(SettingsItem item) {
            tvHeader.setText(item.getTitle());
        }
    }

    class DividerViewHolder extends RecyclerView.ViewHolder {
        DividerViewHolder(@NonNull View v) {
            super(v);
        }
    }

    class OptionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        OptionViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_option_title);
            v.setOnClickListener(this);
        }
        void bind(SettingsItem item) {
            tvTitle.setText(item.getTitle());
        }
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                String title = items.get(pos).getTitle();
                Context ctx = v.getContext();
                switch (title) {
                    case "Change Password":
                        ctx.startActivity(new Intent(ctx, ChangePasswordActivity.class));
                        break;
                    case "Help & Support":
                        ctx.startActivity(new Intent(ctx, HelpSupportActivity.class));
                        break;
                    case "About Us":
                        ctx.startActivity(new Intent(ctx, AboutUsActivity.class));
                        break;
                    case "Recycle Bin":
                        ctx.startActivity(new Intent(ctx, RecycleBinActivity.class));
                        break;
                    // Add other options
                }
            }
        }
    }
}
