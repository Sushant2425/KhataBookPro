package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;
import com.sandhyasofttechh.mykhatapro.model.Transaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    public static File generatePdf(Context context, String customerName, String customerPhone, List<Transaction> transactions, String dateRangeLabel) {
        
        // Standard A4 page size

        int pageWidth = 595;
        int pageHeight = 842;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // --- Paint Objects for Styling ---
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(12);
        
        Paint tableHeaderPaint = new Paint();
        tableHeaderPaint.setColor(Color.BLACK);
        tableHeaderPaint.setTextSize(10);
        tableHeaderPaint.setFakeBoldText(true);
        
        Paint tableTextPaint = new Paint();
        tableTextPaint.setColor(Color.DKGRAY);
        tableTextPaint.setTextSize(10);

        int x = 40, y = 50;

        // --- 1. Document Header ---
        canvas.drawText("Transaction Report", x, y, titlePaint);
        y += 30;
        canvas.drawText("Customer: " + customerName, x, y, headerPaint);
        y += 15;
        canvas.drawText("Phone: " + customerPhone, x, y, headerPaint);
        y += 15;
        canvas.drawText("Period: " + dateRangeLabel, x, y, headerPaint);
        y += 30;

        // --- 2. Table Header ---
        canvas.drawLine(x, y, pageWidth - x, y, headerPaint);
        y += 15;
        canvas.drawText("Date", x, y, tableHeaderPaint);
        canvas.drawText("Note", x + 100, y, tableHeaderPaint);
        canvas.drawText("You Gave", x + 350, y, tableHeaderPaint);
        canvas.drawText("You Got", x + 450, y, tableHeaderPaint);
        y += 5;
        canvas.drawLine(x, y, pageWidth - x, y, headerPaint);
        y += 15;

        // --- 3. Table Rows ---
        double totalGave = 0, totalGot = 0;
        for (Transaction t : transactions) {
            if (y > pageHeight - 50) { // Check for page end, create new page
                 document.finishPage(page);
                 page = document.startPage(pageInfo);
                 canvas = page.getCanvas();
                 y = 50;
            }
            canvas.drawText(t.getDate(), x, y, tableTextPaint);
            canvas.drawText(t.getNote(), x + 100, y, tableTextPaint);
            if ("gave".equals(t.getType())) {
                canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", t.getAmount()), x + 350, y, tableTextPaint);
                totalGave += t.getAmount();
            } else {
                canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", t.getAmount()), x + 450, y, tableTextPaint);
                totalGot += t.getAmount();
            }
            y += 20;
        }
        
        // --- 4. Table Footer & Summary ---
        canvas.drawLine(x, y, pageWidth - x, y, headerPaint);
        y += 15;
        canvas.drawText("Total", x + 250, y, tableHeaderPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", totalGave), x + 350, y, tableHeaderPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", totalGot), x + 450, y, tableHeaderPaint);
        y += 30;
        
        double netBalance = totalGave - totalGot;
        String balanceText = (netBalance >= 0) ? "Net (You will get):" : "Net (You will give):";
        canvas.drawText(balanceText, x + 250, y, tableHeaderPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance)), x + 350, y, tableHeaderPaint);

        document.finishPage(page);

        // --- 5. Save the file ---
        String fileName = "Report_" + customerName.replaceAll("\\s", "_") + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        } finally {
            document.close();
        }
        return file;
    }
}