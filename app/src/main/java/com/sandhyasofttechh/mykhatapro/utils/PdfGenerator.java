package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.sandhyasofttechh.mykhatapro.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PdfGenerator {

    public static File generatePdf(Context context, String customerName, String customerPhone,
                                   List<Transaction> transactions, String dateRangeLabel) {
        final int pageWidth = 595, pageHeight = 842;

        // --- Colors (Khatabook style) ---
        final int COLOR_PRIMARY = Color.parseColor("#5B60FF");
        final int COLOR_TEXT_DARK = Color.parseColor("#1A1A1A");
        final int COLOR_TEXT_LIGHT = Color.parseColor("#8A8A8A");
        final int COLOR_BORDER = Color.parseColor("#E5E5E5");
        final int COLOR_BG_LIGHT = Color.parseColor("#F9F9F9");
        final int COLOR_SUCCESS = Color.parseColor("#10B981");
        final int COLOR_DANGER = Color.parseColor("#EF4444");

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        // Calculate summary data
        double totalDebit = transactions.stream()
                .filter(t -> "gave".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        double totalCredit = transactions.stream()
                .filter(t -> "got".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        double openingBalance = 0; // Set from your data
        double netBalance = openingBalance + totalCredit - totalDebit;

        // --- Paint Styles ---
        Paint brandPaint = createPaint(COLOR_PRIMARY, 24, Typeface.BOLD, true);
        Paint headingPaint = createPaint(COLOR_TEXT_DARK, 16, Typeface.BOLD, true);
        Paint labelPaint = createPaint(COLOR_TEXT_LIGHT, 11, Typeface.NORMAL, true);
        Paint valuePaint = createPaint(COLOR_TEXT_DARK, 13, Typeface.NORMAL, true);
        Paint tableHeaderPaint = createPaint(COLOR_TEXT_DARK, 11, Typeface.BOLD, true);
        Paint tableTextPaint = createPaint(COLOR_TEXT_DARK, 10, Typeface.NORMAL, true);
        Paint borderPaint = createBorderPaint(COLOR_BORDER);
        Paint bgLightPaint = createBgPaint(COLOR_BG_LIGHT);

        int pageNum = 1;
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        int y = drawHeader(canvas, customerName, customerPhone, dateRangeLabel, brandPaint, labelPaint, valuePaint, 32);

        // --- Section 1: Opening Balance & Summary Info ---
        y += 18;
        canvas.drawText("Account Summary", 32, y, headingPaint);
        y += 18;

        int col1 = 32, col2 = 250, col3 = 450;

        canvas.drawText("Opening Balance", col1, y, labelPaint);
        canvas.drawText("₹0.00", col1, y + 14, valuePaint);
        y += 30;

        canvas.drawText("Total Debit (You Gave)", col1, y, labelPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", totalDebit), col1, y + 14, valuePaint);
        y += 30;

        canvas.drawText("Total Credit (You Got)", col1, y, labelPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", totalCredit), col1, y + 14, valuePaint);
        y += 30;

        // Net Balance colored box
        boolean isPositive = netBalance >= 0;
        int statusColor = isPositive ? COLOR_SUCCESS : COLOR_DANGER;
        Paint statusBgPaint = new Paint();
        statusBgPaint.setColor(isPositive ? Color.parseColor("#ECFDF5") : Color.parseColor("#FEF2F2"));

        canvas.drawRect(col1, y - 8, 595 - 32, y + 28, statusBgPaint);
        Paint statusPaint = createPaint(statusColor, 13, Typeface.BOLD, true);
        String statusLabel = isPositive ? "Net Balance (Customer gives to you)" : "Net Balance (You give to customer)";
        canvas.drawText(statusLabel, col1 + 8, y + 6, statusPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance)), col3, y + 6, statusPaint);

        y += 40;
        canvas.drawLine(32, y, pageWidth - 32, y, borderPaint);
        y += 18;

        // --- Section 2: Number of Transactions ---
        canvas.drawText("Total Transactions: " + transactions.size(), 32, y, tableHeaderPaint);
        y += 18;
        canvas.drawLine(32, y, pageWidth - 32, y, borderPaint);
        y += 18;

        // --- Section 3: Transaction Table Header ---
        canvas.drawRect(32, y - 12, pageWidth - 32, y + 8, bgLightPaint);
        canvas.drawText("Date", 32, y, tableHeaderPaint);
        canvas.drawText("Details / Note", 110, y, tableHeaderPaint);
        canvas.drawText("Debit", 340, y, tableHeaderPaint);
        canvas.drawText("Credit", 430, y, tableHeaderPaint);
        canvas.drawText("Balance", 520, y, tableHeaderPaint);
        y += 14;
        canvas.drawLine(32, y, pageWidth - 32, y, borderPaint);
        y += 8;

        // --- Section 4: Group transactions by Month ---
        Map<String, List<Transaction>> transactionsByMonth = groupByMonth(transactions);
        double runningBalance = openingBalance;

        for (String month : transactionsByMonth.keySet()) {
            List<Transaction> monthTransactions = transactionsByMonth.get(month);

            // Check if page break needed
            if (y > pageHeight - 150) {
                document.finishPage(page);
                pageNum++;
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 32;
                // Redraw header
                drawHeader(canvas, customerName, customerPhone, dateRangeLabel, brandPaint, labelPaint, valuePaint, 32);
                y += 150;
            }

            // Month header
            Paint monthPaint = createPaint(COLOR_PRIMARY, 12, Typeface.BOLD, true);
            canvas.drawText(month, 32, y, monthPaint);
            y += 14;
            canvas.drawLine(32, y, pageWidth - 32, y, new Paint() {{
                setColor(COLOR_PRIMARY);
                setStrokeWidth(1.5f);
            }});
            y += 8;

            // Transactions for this month
            for (Transaction t : monthTransactions) {
                if (y > pageHeight - 50) {
                    document.finishPage(page);
                    pageNum++;
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 32;
                    drawHeader(canvas, customerName, customerPhone, dateRangeLabel, brandPaint, labelPaint, valuePaint, 32);
                    y += 150;
                }

                double debitAmount = 0, creditAmount = 0;
                if ("gave".equalsIgnoreCase(t.getType())) {
                    debitAmount = t.getAmount();
                    runningBalance -= debitAmount;
                } else {
                    creditAmount = t.getAmount();
                    runningBalance += creditAmount;
                }

                canvas.drawText(t.getDate(), 32, y, tableTextPaint);
                canvas.drawText(truncate(t.getNote(), 25), 110, y, tableTextPaint);
                if (debitAmount > 0) {
                    canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", debitAmount), 340, y, tableTextPaint);
                } else {
                    canvas.drawText("-", 340, y, tableTextPaint);
                }
                if (creditAmount > 0) {
                    canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", creditAmount), 430, y, tableTextPaint);
                } else {
                    canvas.drawText("-", 430, y, tableTextPaint);
                }
                canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", runningBalance), 520, y, tableTextPaint);
                y += 16;
            }

            y += 6;
        }

        // --- Section 5: Grand Total ---
        y += 8;
        canvas.drawLine(32, y, pageWidth - 32, y, borderPaint);
        y += 12;

        canvas.drawRect(32, y - 8, pageWidth - 32, y + 20, bgLightPaint);
        Paint grandTotalPaint = createPaint(COLOR_PRIMARY, 12, Typeface.BOLD, true);

        canvas.drawText("GRAND TOTAL", 32, y, grandTotalPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", totalDebit), 340, y, grandTotalPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", totalCredit), 430, y, grandTotalPaint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", netBalance), 520, y, grandTotalPaint);

        // --- Footer ---
        Paint footerPaint = createPaint(COLOR_TEXT_LIGHT, 9, Typeface.NORMAL, true);
        footerPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("KhataPro · Page " + pageNum, pageWidth / 2f, pageHeight - 16, footerPaint);

        document.finishPage(page);

        // --- Save PDF ---
        String fileName = "Statement_" + customerName.replaceAll("\\s", "_") + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Toast.makeText(context, "Statement saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        } finally {
            document.close();
        }
        return file;
    }

    // --- Helper Methods ---
    private static int drawHeader(Canvas canvas, String customerName, String customerPhone,
                                  String dateRange, Paint brandPaint, Paint labelPaint, Paint valuePaint, int margin) {
        int y = margin;
        canvas.drawText("KhataPro", margin, y, brandPaint);
        y += 12;
        canvas.drawText("Account Statement", margin, y, labelPaint);
        y += 18;

        canvas.drawText("Customer Name: " + customerName, margin, y, valuePaint);
        y += 14;
        canvas.drawText("Phone: " + customerPhone, margin, y, valuePaint);
        y += 14;
        canvas.drawText("Period: " + dateRange, margin, y, valuePaint);
        y += 14;

        return y;
    }

    private static Map<String, List<Transaction>> groupByMonth(List<Transaction> transactions) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat display = new SimpleDateFormat("MMMM yyyy", new Locale("en", "IN"));

        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            try {
                Date date = sdf.parse(t.getDate().substring(0, 7));
                String monthKey = display.format(date);
                grouped.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(t);
            } catch (Exception e) {
                grouped.computeIfAbsent("Other", k -> new ArrayList<>()).add(t);
            }
        }

        return grouped;
    }

    private static Paint createPaint(int color, int textSize, int typeface, boolean antiAlias) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, typeface));
        paint.setAntiAlias(antiAlias);
        return paint;
    }

    private static Paint createBorderPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(0.8f);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    private static Paint createBgPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    private static String truncate(String text, int maxLen) {
        if (text == null || text.isEmpty()) return "-";
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }
}
