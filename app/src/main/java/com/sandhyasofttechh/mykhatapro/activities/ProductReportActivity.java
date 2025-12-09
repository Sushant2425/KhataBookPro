package com.sandhyasofttechh.mykhatapro.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.model.Product;
import com.sandhyasofttechh.mykhatapro.utils.PrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductReportActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView tvTitle, tvTotalProducts, tvTotalQty, tvStockValue,
            tvSaleValue, tvLowStockCount, tvOutOfStock, tvReorderCost;

    Button btnLowStockReport, btnOutStockReport, btnFullStockPDF, btnHighStockPDF;

    DatabaseReference productsRef;
    List<Product> productList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_report);

        initViews();
        initFirebase();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        tvStockValue = findViewById(R.id.tvStockValue);
        tvSaleValue = findViewById(R.id.tvSaleValue);
        tvLowStockCount = findViewById(R.id.tvLowStockCount);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvReorderCost = findViewById(R.id.tvReorderCost);

        btnLowStockReport = findViewById(R.id.btnLowStockReport);
        btnOutStockReport = findViewById(R.id.btnOutStockReport);
        btnFullStockPDF = findViewById(R.id.btnFullStockPDF);
        btnHighStockPDF = findViewById(R.id.btnHighStockPDF);

        tvTitle.setText("Stock Summary Report");

        btnBack.setOnClickListener(v -> finish());

        btnLowStockReport.setOnClickListener(v -> exportPdf(1));
        btnOutStockReport.setOnClickListener(v -> exportPdf(2));
        btnFullStockPDF.setOnClickListener(v -> exportPdf(3));
        btnHighStockPDF.setOnClickListener(v -> exportPdf(4));
    }

    private void initFirebase() {
        PrefManager pref = new PrefManager(this);
        String email = pref.getUserEmail();
        String shopId = pref.getCurrentShopId();

        if (email == null) return;

        String emailKey = email.replace(".", ",");

        productsRef = FirebaseDatabase.getInstance()
                .getReference("Khatabook")
                .child(emailKey)
                .child("shops")
                .child(shopId)
                .child("products");
    }

    private void loadData() {

        disableButtonsWhileLoading();

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                productList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product p = ds.getValue(Product.class);

                    if (p != null) {
                        productList.add(p);
                    }
                }

                enableButtonsAfterLoad();

                updateCounts();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductReportActivity.this,
                        "Error Loading Products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableButtonsWhileLoading() {
        btnLowStockReport.setEnabled(false);
        btnOutStockReport.setEnabled(false);
        btnFullStockPDF.setEnabled(false);
        btnHighStockPDF.setEnabled(false);
    }

    private void enableButtonsAfterLoad() {
        btnLowStockReport.setEnabled(true);
        btnOutStockReport.setEnabled(true);
        btnFullStockPDF.setEnabled(true);
        btnHighStockPDF.setEnabled(true);
    }


    private void updateCounts() {

        int total = 0, low = 0, out = 0;
        double totalQty = 0, purchaseTotal = 0, saleTotal = 0, reorderAmt = 0;

        for (Product p : productList) {

            double qty = p.getCurrentStockDouble();
            double pp = p.getPurchasePriceDouble();
            double sp = p.getSalePriceDouble();
            double alert = p.getLowStockAlertDouble();

            total++;
            totalQty += qty;
            purchaseTotal += qty * pp;
            saleTotal += qty * sp;

            if (qty == 0) out++;

            if (qty <= alert) {
                low++;
                reorderAmt += (alert - qty) * pp;
            }
        }

        tvTotalProducts.setText(String.valueOf(total));
        tvTotalQty.setText(String.valueOf(totalQty));
        tvStockValue.setText("₹" + purchaseTotal);
        tvSaleValue.setText("₹" + saleTotal);
        tvLowStockCount.setText(String.valueOf(low));
        tvOutOfStock.setText(String.valueOf(out));
        tvReorderCost.setText("₹" + reorderAmt);
    }

    // ==========================================================
    // EXPORT
    // ==========================================================
    private void exportPdf(int type) {

        List<Product> filtered = new ArrayList<>();

        for (Product p : productList) {
            double qty = p.getCurrentStockDouble();
            double alert = p.getLowStockAlertDouble();

            if (type == 1 && qty <= alert) filtered.add(p);
            else if (type == 2 && qty == 0) filtered.add(p);
            else if (type == 3) filtered.add(p);
            else if (type == 4 && qty > 10) filtered.add(p);
        }

        if (filtered.isEmpty()) {
            Toast.makeText(this, "No matching records", Toast.LENGTH_SHORT).show();
            return;
        }

        createPdf(filtered, type);
    }

    private void createPdf(List<Product> list, int type) {

        PdfDocument document = new PdfDocument();

        int maxRows = 18;
        int pages = (int) Math.ceil(list.size() / (maxRows * 1.0));

        for (int i = 0; i < pages; i++) {

            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(595, 842, i + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            drawPage(
                    page.getCanvas(),
                    list.subList(
                            i * maxRows,
                            Math.min((i + 1) * maxRows, list.size())
                    ),
                    type,
                    i + 1,
                    pages
            );

            document.finishPage(page);
        }

        savePdf(document, type);
    }

    private void drawPage(Canvas canvas,
                          List<Product> rows,
                          int type,
                          int page,
                          int totalPages) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float W = 595;
        float M = 30;
        float y = M + 10;

        paint.setColor(Color.parseColor("#1A237E"));
        canvas.drawRect(0, 0, W, 120, paint);

        paint.setTextSize(26);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("MyKhata Pro", M, 50, paint);

        paint.setTextSize(16);

        String title = "";
        if (type == 1) title = "Low Stock Report";
        else if (type == 2) title = "Out Of Stock Report";
        else if (type == 3) title = "Full Inventory Report";
        else title = "High Stock Report (>10 Qty)";

        canvas.drawText(title, M, 80, paint);

        paint.setTextSize(10);
        canvas.drawText("Page: " + page + "/" + totalPages, W - 120, 100, paint);
        canvas.drawText("Generated: " + new Date(), M, 100, paint);

        y = 140;

        paint.setColor(Color.parseColor("#212121"));
        canvas.drawRect(M, y, W - M, y + 30, paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.WHITE);
        paint.setTextSize(11);

        canvas.drawText("Name", M + 10, y + 20, paint);
        canvas.drawText("Qty", M + 180, y + 20, paint);
        canvas.drawText("Purchase", M + 250, y + 20, paint);
        canvas.drawText("Sale", M + 350, y + 20, paint);
        canvas.drawText("Alert", W - M - 60, y + 20, paint);

        y += 30;
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(10);

        for (Product p : rows) {

            canvas.drawRect(M, y, W - M, y + 26, paint);

            canvas.drawText(p.getName(), M + 10, y + 18, paint);
            canvas.drawText("" + p.getCurrentStockDouble(), M + 180, y + 18, paint);
            canvas.drawText("₹" + p.getPurchasePrice(), M + 250, y + 18, paint);
            canvas.drawText("₹" + p.getSalePrice(), M + 350, y + 18, paint);
            canvas.drawText("" + p.getLowStockAlertDouble(), W - M - 60, y + 18, paint);

            y += 26;
        }
    }

    private void savePdf(PdfDocument doc, int type) {

        File folder = new File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "StockReports");

        if (!folder.exists()) folder.mkdirs();

        String name = "";

        if (type == 1) name = "LowStock_";
        else if (type == 2) name = "OutStock_";
        else if (type == 3) name = "FullStock_";
        else name = "HighStock_";

        name += System.currentTimeMillis() + ".pdf";

        File file = new File(folder, name);

        try {

            doc.writeTo(new FileOutputStream(file));
            doc.close();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file);

            Intent open = new Intent(Intent.ACTION_VIEW);
            open.putExtra(Intent.EXTRA_STREAM, uri);
            open.setDataAndType(uri, "application/pdf");
            open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(open, "Open PDF"));

        } catch (Exception e) {
            Toast.makeText(this, "PDF error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
