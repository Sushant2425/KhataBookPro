////package com.sandhyasofttechh.mykhatapro.adapter;
////
////import android.content.Context;
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.LinearLayout;
////import android.widget.TextView;
////
////import androidx.annotation.NonNull;
////import androidx.recyclerview.widget.RecyclerView;
////
////import com.sandhyasofttechh.mykhatapro.R;
////import com.sandhyasofttechh.mykhatapro.model.Product;
////import com.sandhyasofttechh.mykhatapro.model.StockHistory;
////
////import java.util.List;
////
////public class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.VH> {
////
////    Context context;
////    List<StockHistory> list;
////    Product product; // ✅ NEW: For HSN/GST fallback
////
////    public StockHistoryAdapter(Context context, List<StockHistory> list, Product product) {
////        this.context = context;
////        this.list = list;
////        this.product = product;
////    }
////
////    @NonNull
////    @Override
////    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////        View v = LayoutInflater.from(context)
////                .inflate(R.layout.item_stock_history, parent, false);
////        return new VH(v);
////    }
////
////    @Override
////    public void onBindViewHolder(@NonNull VH h, int pos) {
////        StockHistory sh = list.get(pos);
////
////        // ✅ COMPLETE SAFE VALUE HANDLING
////        String date = sh.getDate() != null ? sh.getDate() : "";
////        String time = sh.getTime() != null ? sh.getTime() : "";
////        String qty  = sh.getQuantity() != null ? sh.getQuantity() : "0";
////        String price = sh.getPrice() != null ? sh.getPrice() : "0";
////        String note = sh.getNote() != null ? sh.getNote() : "";
////        String unit = sh.getUnit() != null ? sh.getUnit() : "pcs";
////        String type = sh.getType() != null ? sh.getType().toLowerCase() : "";
////
////        // ✅ FIXED HSN/GST WITH FALLBACK
////        String hsn = sh.getHsn() != null && !sh.getHsn().trim().isEmpty() ?
////                sh.getHsn() : (product != null ? product.getHsn() : "N/A");
////        String gst = sh.getGst() != null && !sh.getGst().trim().isEmpty() ?
////                sh.getGst() : (product != null ? product.getGst() : "0");
////
////        // ====================================
////        // LEFT SIDE ALWAYS DISPLAY
////        // ====================================
////        h.tv_left_date.setText(date);
////        h.tv_left_time.setText(time);
////        h.tv_left_balance.setText("₹" + price);
////
////        if (!note.trim().isEmpty()) {
////            h.tv_left_note.setVisibility(View.VISIBLE);
////            h.tv_left_note.setText(note);
////        } else {
////            h.tv_left_note.setVisibility(View.GONE);
////        }
////
////        // ✅ NEW: HSN/GST DISPLAY
////        h.tv_hsn.setText("HSN: " + hsn);
////        h.tv_gst.setText("GST: " + gst + "%");
////
////        // ====================================
////        // STOCK OUT Conditions
////        // ====================================
////        boolean isStockOut =
////                type.contains("out") ||
////                        type.contains("sale") ||
////                        type.contains("damage") ||
////                        type.contains("loss");
////
////        if (isStockOut) {
////            // Show OUT Section
////            h.layout_stock_out.setVisibility(View.VISIBLE);
////            h.layout_stock_in.setVisibility(View.INVISIBLE);
////
////            h.tv_out_qty.setText(qty);
////            h.tv_out_unit.setText(unit);
////            h.tv_out_amount.setText("₹" + price);
////            h.tv_out_amount.setTextColor(context.getColor(R.color.error));
////
////        } else {
////            // Show IN Section
////            h.layout_stock_out.setVisibility(View.INVISIBLE);
////            h.layout_stock_in.setVisibility(View.VISIBLE);
////
////            h.tv_in_qty.setText(qty);
////            h.tv_in_unit.setText(unit);
////            h.tv_in_amount.setText("₹" + price);
////            h.tv_in_amount.setTextColor(context.getColor(R.color.green));
////        }
////
////        // ====================================
////        // DATE HEADER GROUPING
////        // ====================================
////        if (pos == 0) {
////            h.tv_top_header.setVisibility(View.VISIBLE);
////            h.tv_top_header.setText(date);
////        } else {
////            String previous = list.get(pos - 1).getDate();
////            if (previous != null && previous.equals(date)) {
////                h.tv_top_header.setVisibility(View.GONE);
////            } else {
////                h.tv_top_header.setVisibility(View.VISIBLE);
////                h.tv_top_header.setText(date);
////            }
////        }
////    }
////
////    @Override
////    public int getItemCount() {
////        return list.size();
////    }
////
////    // ====================================
////    // VIEW HOLDER - COMPLETE WITH HSN/GST
////    // ====================================
////    static class VH extends RecyclerView.ViewHolder {
////        TextView tv_top_header, tv_left_date, tv_left_time, tv_left_balance, tv_left_note;
////        TextView tv_hsn, tv_gst; // ✅ NEW: HSN/GST Views
////
////        LinearLayout layout_stock_out, layout_stock_in;
////
////        TextView tv_out_qty, tv_out_unit, tv_out_amount;
////        TextView tv_in_qty, tv_in_unit, tv_in_amount;
////
////        public VH(@NonNull View v) {
////            super(v);
////
////            tv_top_header = v.findViewById(R.id.tv_top_header);
////            tv_left_date = v.findViewById(R.id.tv_left_date);
////            tv_left_time = v.findViewById(R.id.tv_left_time);
////            tv_left_balance = v.findViewById(R.id.tv_left_balance);
////            tv_left_note = v.findViewById(R.id.tv_left_note);
////
////            // ✅ NEW: HSN/GST Views
////            tv_hsn = v.findViewById(R.id.tv_hsn);
////            tv_gst = v.findViewById(R.id.tv_gst);
////
////            layout_stock_out = v.findViewById(R.id.layout_stock_out);
////            tv_out_qty = v.findViewById(R.id.tv_out_qty);
////            tv_out_unit = v.findViewById(R.id.tv_out_unit);
////            tv_out_amount = v.findViewById(R.id.tv_out_amount);
////
////            layout_stock_in = v.findViewById(R.id.layout_stock_in);
////            tv_in_qty = v.findViewById(R.id.tv_in_qty);
////            tv_in_unit = v.findViewById(R.id.tv_in_unit);
////            tv_in_amount = v.findViewById(R.id.tv_in_amount);
////        }
////    }
////}
//
//
//
//package com.sandhyasofttechh.mykhatapro.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.sandhyasofttechh.mykhatapro.R;
//import com.sandhyasofttechh.mykhatapro.model.Product;
//import com.sandhyasofttechh.mykhatapro.model.StockHistory;
//
//import java.util.List;
//
//public class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.VH> {
//
//    public interface HistoryActionListener {
//        void onHistoryOptions(StockHistory history, int position);
//    }
//
//    private HistoryActionListener actionListener;
//
//    public void setHistoryActionListener(HistoryActionListener listener) {
//        this.actionListener = listener;
//    }
//
//    Context context;
//    List<StockHistory> list;
//    Product product; // For HSN/GST fallback
//
//    public StockHistoryAdapter(Context context, List<StockHistory> list, Product product) {
//        this.context = context;
//        this.list = list;
//        this.product = product;
//    }
//
//    @NonNull
//    @Override
//    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(context)
//                .inflate(R.layout.item_stock_history, parent, false);
//        return new VH(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull VH h, int pos) {
//        StockHistory sh = list.get(pos);
//
//        String date = sh.getDate() != null ? sh.getDate() : "";
//        String time = sh.getTime() != null ? sh.getTime() : "";
//        String qty  = sh.getQuantity() != null ? sh.getQuantity() : "0";
//        String price = sh.getPrice() != null ? sh.getPrice() : "0";
//        String note = sh.getNote() != null ? sh.getNote() : "";
//        String unit = sh.getUnit() != null ? sh.getUnit() : "pcs";
//        String type = sh.getType() != null ? sh.getType().toLowerCase() : "";
//
//        String hsn = sh.getHsn() != null && !sh.getHsn().trim().isEmpty() ?
//                sh.getHsn() : (product != null ? product.getHsn() : "N/A");
//        String gst = sh.getGst() != null && !sh.getGst().trim().isEmpty() ?
//                sh.getGst() : (product != null ? product.getGst() : "0");
//
//        h.tv_left_date.setText(date);
//        h.tv_left_time.setText(time);
//        h.tv_left_balance.setText("₹" + price);
//
//        if (!note.trim().isEmpty()) {
//            h.tv_left_note.setVisibility(View.VISIBLE);
//            h.tv_left_note.setText(note);
//        } else {
//            h.tv_left_note.setVisibility(View.GONE);
//        }
//
//        h.tv_hsn.setText("HSN: " + hsn);
//        h.tv_gst.setText("GST: " + gst + "%");
//
//        boolean isStockOut =
//                type.contains("out") ||
//                        type.contains("sale") ||
//                        type.contains("damage") ||
//                        type.contains("loss");
//
//        if (isStockOut) {
//            h.layout_stock_out.setVisibility(View.VISIBLE);
//            h.layout_stock_in.setVisibility(View.INVISIBLE);
//
//            h.tv_out_qty.setText(qty);
//            h.tv_out_unit.setText(unit);
//            h.tv_out_amount.setText("₹" + price);
//            h.tv_out_amount.setTextColor(context.getColor(R.color.error));
//
//        } else {
//            h.layout_stock_out.setVisibility(View.INVISIBLE);
//            h.layout_stock_in.setVisibility(View.VISIBLE);
//
//            h.tv_in_qty.setText(qty);
//            h.tv_in_unit.setText(unit);
//            h.tv_in_amount.setText("₹" + price);
//            h.tv_in_amount.setTextColor(context.getColor(R.color.green));
//        }
//
//        if (pos == 0) {
//            h.tv_top_header.setVisibility(View.VISIBLE);
//            h.tv_top_header.setText(date);
//        } else {
//            String previous = list.get(pos - 1).getDate();
//            if (previous != null && previous.equals(date)) {
//                h.tv_top_header.setVisibility(View.GONE);
//            } else {
//                h.tv_top_header.setVisibility(View.VISIBLE);
//                h.tv_top_header.setText(date);
//            }
//        }
//
//        h.itemView.setOnLongClickListener(v -> {
//            if (actionListener != null) {
//                int adapterPos = h.getAdapterPosition();
//                if (adapterPos != RecyclerView.NO_POSITION) {
//                    actionListener.onHistoryOptions(sh, adapterPos);
//                }
//            }
//            return true;
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    static class VH extends RecyclerView.ViewHolder {
//        TextView tv_top_header, tv_left_date, tv_left_time, tv_left_balance, tv_left_note;
//        TextView tv_hsn, tv_gst;
//        LinearLayout layout_stock_out, layout_stock_in;
//        TextView tv_out_qty, tv_out_unit, tv_out_amount;
//        TextView tv_in_qty, tv_in_unit, tv_in_amount;
//
//        public VH(@NonNull View v) {
//            super(v);
//            tv_top_header = v.findViewById(R.id.tv_top_header);
//            tv_left_date = v.findViewById(R.id.tv_left_date);
//            tv_left_time = v.findViewById(R.id.tv_left_time);
//            tv_left_balance = v.findViewById(R.id.tv_left_balance);
//            tv_left_note = v.findViewById(R.id.tv_left_note);
//            tv_hsn = v.findViewById(R.id.tv_hsn);
//            tv_gst = v.findViewById(R.id.tv_gst);
//            layout_stock_out = v.findViewById(R.id.layout_stock_out);
//            tv_out_qty = v.findViewById(R.id.tv_out_qty);
//            tv_out_unit = v.findViewById(R.id.tv_out_unit);
//            tv_out_amount = v.findViewById(R.id.tv_out_amount);
//            layout_stock_in = v.findViewById(R.id.layout_stock_in);
//            tv_in_qty = v.findViewById(R.id.tv_in_qty);
//            tv_in_unit = v.findViewById(R.id.tv_in_unit);
//            tv_in_amount = v.findViewById(R.id.tv_in_amount);
//        }
//    }
//}




package com.sandhyasofttechh.mykhatapro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Product;
import com.sandhyasofttechh.mykhatapro.model.StockHistory;

import java.util.List;

public class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.VH> {

    public interface HistoryActionListener {
        void onHistoryOptions(StockHistory history, int position);
    }

    private HistoryActionListener actionListener;

    public void setHistoryActionListener(HistoryActionListener listener) {
        this.actionListener = listener;
    }

    Context context;
    List<StockHistory> list;
    Product product; // For HSN/GST fallback

    public StockHistoryAdapter(Context context, List<StockHistory> list, Product product) {
        this.context = context;
        this.list = list;
        this.product = product;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_stock_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        StockHistory sh = list.get(pos);

        String date = sh.getDate() != null ? sh.getDate() : "";
        String time = sh.getTime() != null ? sh.getTime() : "";
        String qty  = sh.getQuantity() != null ? sh.getQuantity() : "0";
        String price = sh.getPrice() != null ? sh.getPrice() : "0";
        String note = sh.getNote() != null ? sh.getNote() : "";
        String unit = sh.getUnit() != null ? sh.getUnit() : "pcs";
        String type = sh.getType() != null ? sh.getType().toLowerCase() : "";

        String hsn = sh.getHsn() != null && !sh.getHsn().trim().isEmpty() ?
                sh.getHsn() : (product != null ? product.getHsn() : "N/A");
        String gst = sh.getGst() != null && !sh.getGst().trim().isEmpty() ?
                sh.getGst() : (product != null ? product.getGst() : "0");

        h.tv_left_date.setText(date);
        h.tv_left_time.setText(time);
        h.tv_left_balance.setText("₹" + price);

        if (!note.trim().isEmpty()) {
            h.tv_left_note.setVisibility(View.VISIBLE);
            h.tv_left_note.setText(note);
        } else {
            h.tv_left_note.setVisibility(View.GONE);
        }

        h.tv_hsn.setText("HSN: " + hsn);
        h.tv_gst.setText("GST: " + gst + "%");

        boolean isStockOut =
                type.contains("out") ||
                        type.contains("sale") ||
                        type.contains("damage") ||
                        type.contains("loss");

        if (isStockOut) {
            h.layout_stock_out.setVisibility(View.VISIBLE);
            h.layout_stock_in.setVisibility(View.INVISIBLE);

            h.tv_out_qty.setText(qty);
            h.tv_out_unit.setText(unit);
            h.tv_out_amount.setText("₹" + price);
            h.tv_out_amount.setTextColor(context.getColor(R.color.error));

        } else {
            h.layout_stock_out.setVisibility(View.INVISIBLE);
            h.layout_stock_in.setVisibility(View.VISIBLE);

            h.tv_in_qty.setText(qty);
            h.tv_in_unit.setText(unit);
            h.tv_in_amount.setText("₹" + price);
            h.tv_in_amount.setTextColor(context.getColor(R.color.green));
        }

        if (pos == 0) {
            h.tv_top_header.setVisibility(View.VISIBLE);
            h.tv_top_header.setText(date);
        } else {
            String previous = list.get(pos - 1).getDate();
            if (previous != null && previous.equals(date)) {
                h.tv_top_header.setVisibility(View.GONE);
            } else {
                h.tv_top_header.setVisibility(View.VISIBLE);
                h.tv_top_header.setText(date);
            }
        }

        h.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    actionListener.onHistoryOptions(sh, adapterPos);
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv_top_header, tv_left_date, tv_left_time, tv_left_balance, tv_left_note;
        TextView tv_hsn, tv_gst;
        LinearLayout layout_stock_out, layout_stock_in;
        TextView tv_out_qty, tv_out_unit, tv_out_amount;
        TextView tv_in_qty, tv_in_unit, tv_in_amount;

        public VH(@NonNull View v) {
            super(v);
            tv_top_header = v.findViewById(R.id.tv_top_header);
            tv_left_date = v.findViewById(R.id.tv_left_date);
            tv_left_time = v.findViewById(R.id.tv_left_time);
            tv_left_balance = v.findViewById(R.id.tv_left_balance);
            tv_left_note = v.findViewById(R.id.tv_left_note);
            tv_hsn = v.findViewById(R.id.tv_hsn);
            tv_gst = v.findViewById(R.id.tv_gst);
            layout_stock_out = v.findViewById(R.id.layout_stock_out);
            tv_out_qty = v.findViewById(R.id.tv_out_qty);
            tv_out_unit = v.findViewById(R.id.tv_out_unit);
            tv_out_amount = v.findViewById(R.id.tv_out_amount);
            layout_stock_in = v.findViewById(R.id.layout_stock_in);
            tv_in_qty = v.findViewById(R.id.tv_in_qty);
            tv_in_unit = v.findViewById(R.id.tv_in_unit);
            tv_in_amount = v.findViewById(R.id.tv_in_amount);
        }
    }
}
