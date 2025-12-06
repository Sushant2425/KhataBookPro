package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.StockHistory;
import java.util.List;

public class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.VH> {

    Context context;
    List<StockHistory> list;

    public StockHistoryAdapter(Context context, List<StockHistory> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int v) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_stock_history, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        StockHistory sh = list.get(pos);

        h.tvType.setText(sh.getType());
        h.tvQty.setText("" + sh.getQuantity());
        h.tvBalance.setText("" + sh.getPrice());
        h.tvDate.setText(sh.getDate());
        h.tvTime.setText(sh.getTime());
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvQty, tvBalance, tvDate, tvTime;
        public VH(@NonNull View v) {
            super(v);
            tvType = v.findViewById(R.id.tvType);
            tvQty = v.findViewById(R.id.tvQty);
            tvBalance = v.findViewById(R.id.tvBalance);
            tvDate = v.findViewById(R.id.tvDate);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}
