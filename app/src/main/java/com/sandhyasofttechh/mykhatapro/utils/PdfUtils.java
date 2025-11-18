package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.sandhyasofttechh.mykhatapro.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfUtils {

    private final PdfDocument document;

    private final Context context;
    private int pageNumber = 0;

    private final Paint titlePaint = new Paint();
    private final Paint headerPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint linePaint = new Paint();

    public PdfUtils(Context ctx, PdfDocument doc) {
        this.context = ctx;
        this.document = doc;

        titlePaint.setColor(0xFF1565C0);
        titlePaint.setTextSize(28);
        titlePaint.setFakeBoldText(true);
        titlePaint.setAntiAlias(true);

        headerPaint.setColor(0xFF212121);
        headerPaint.setTextSize(16);
        headerPaint.setFakeBoldText(true);
        headerPaint.setAntiAlias(true);

        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(14);
        textPaint.setAntiAlias(true);

        linePaint.setColor(0xFFCCCCCC);
        linePaint.setStrokeWidth(1);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
    }

    public static class Page {
        public final Canvas canvas;
        public final PdfDocument.PageInfo info;
        public final PdfDocument.Page pdfPage;
        public float y = 100;

        Page(Canvas c, PdfDocument.PageInfo i, PdfDocument.Page p) {
            canvas = c; info = i; pdfPage = p;
        }
    }

    public Page startPage() {
        pageNumber++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page pdfPage = document.startPage(pageInfo);
        Canvas canvas = pdfPage.getCanvas();

        drawPageHeader(canvas);
        return new Page(canvas, pageInfo, pdfPage);
    }

    public void finishPage(Page page) {
        drawFooter(page.canvas, page.info.getPageNumber());
        document.finishPage(page.pdfPage);
    }

    private void drawPageHeader(Canvas canvas) {
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_shop_logo);
        if (logo != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(logo, 80, 80, true);
            canvas.drawBitmap(scaled, 40, 20, null);
        }

        canvas.drawText("Sandhya Computer", 140, 55, titlePaint);

        String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date());
        canvas.drawText(date, 400, 55, headerPaint);
    }

    private void drawFooter(Canvas canvas, int pageNo) {
        String footer = "Page " + pageNo;
        float x = 595 - 40 - textPaint.measureText(footer);
        canvas.drawText(footer, x, 830, textPaint);
    }

    public void saveAndShare(String fileName) {
        try {
            File pdfFile = new File(context.getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            fos.flush();
            fos.close();
            document.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(Intent.createChooser(intent, "Open PDF"));
        } catch (IOException e) {
            Toast.makeText(context, "PDF Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public Paint getTitlePaint()   { return titlePaint; }
    public Paint getHeaderPaint()  { return headerPaint; }
    public Paint getTextPaint()    { return textPaint; }
    public Paint getLinePaint()    { return linePaint; }
}