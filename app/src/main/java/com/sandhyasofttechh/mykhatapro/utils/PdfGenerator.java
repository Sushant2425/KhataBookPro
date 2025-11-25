//package com.sandhyasofttechh.mykhatapro.utils;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.Typeface;
//import android.graphics.pdf.PdfDocument;
//import android.os.Environment;
//import android.widget.Toast;
//
//import com.sandhyasofttechh.mykhatapro.model.Transaction;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class PdfGenerator {
//
//    // Table column definitions
//    private static class TableColumns {
//        int dateX, dateWidth;
//        int noteX, noteWidth;
//        int debitX, debitWidth;
//        int creditX, creditWidth;
//        int balanceX, balanceWidth;
//        int tableLeft, tableRight;
//        int padding = 8; // Padding inside each cell
//
//        TableColumns(int left, int right) {
//            tableLeft = left;
//            tableRight = right;
//            int totalWidth = right - left;
//
//            // Define percentage-based widths
//            dateWidth = (int)(totalWidth * 0.25);      // 25%
//            noteWidth = (int)(totalWidth * 0.25);      // 25%
//            debitWidth = (int)(totalWidth * 0.13);     // 13%
//            creditWidth = (int)(totalWidth * 0.13);    // 13%
//            balanceWidth = totalWidth - dateWidth - noteWidth - debitWidth - creditWidth; // Remaining (~24%)
//
//            // Calculate X positions for column boundaries
//            dateX = left;
//            noteX = dateX + dateWidth;
//            debitX = noteX + noteWidth;
//            creditX = debitX + debitWidth;
//            balanceX = creditX + creditWidth;
//        }
//
//        // Get text X position (with padding from left border)
//        int getDateTextX() { return dateX + padding; }
//        int getNoteTextX() { return noteX + padding; }
//        int getDebitTextX() { return debitX + padding; }
//        int getCreditTextX() { return creditX + padding; }
//        int getBalanceTextX() { return balanceX + padding; }
//
//        int[] getVerticalLines() {
//            return new int[]{noteX, debitX, creditX, balanceX};
//        }
//    }
//
//    public static File generatePdf(Context context,
//                                   String customerName,
//                                   String customerPhone,
//                                   List<Transaction> transactions,
//                                   String dateRangeLabel,
//                                   double openingBalance) {
//
//        final int pageWidth = 595;
//        final int pageHeight = 842;
//        final int ROW_HEIGHT = 24;
//        final int HEADER_ROW_HEIGHT = 28;
//        final int MONTH_HEADER_HEIGHT = 26;
//
//        // Colors
//        final int COLOR_HEADER = Color.parseColor("#1565C0");
//        final int COLOR_TEXT_DARK = Color.parseColor("#212121");
//        final int COLOR_TEXT_GRAY = Color.parseColor("#757575");
//        final int COLOR_BORDER = Color.parseColor("#BDBDBD");
//        final int COLOR_BG_LIGHT = Color.parseColor("#F5F5F5");
//        final int COLOR_GREEN = Color.parseColor("#4CAF50");
//        final int COLOR_RED = Color.parseColor("#F44336");
//        final int COLOR_WHITE = Color.WHITE;
//
//        PdfDocument document = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
//
//        // Initialize paints
//        Paint headerBgPaint = new Paint();
//        headerBgPaint.setColor(COLOR_HEADER);
//        headerBgPaint.setStyle(Paint.Style.FILL);
//
//        Paint headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        headerTextPaint.setColor(COLOR_WHITE);
//        headerTextPaint.setTextSize(11f);
//        headerTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//
//        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        titlePaint.setColor(COLOR_TEXT_DARK);
//        titlePaint.setTextSize(15f);
//        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//
//        Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        subtitlePaint.setColor(COLOR_TEXT_GRAY);
//        subtitlePaint.setTextSize(9.5f);
//
//        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        labelPaint.setColor(COLOR_TEXT_GRAY);
//        labelPaint.setTextSize(8.5f);
//
//        Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        valuePaint.setColor(COLOR_TEXT_DARK);
//        valuePaint.setTextSize(12f);
//        valuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//
//        Paint tableHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        tableHeaderPaint.setColor(COLOR_TEXT_DARK);
//        tableHeaderPaint.setTextSize(10f);
//        tableHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//
//        Paint tableTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        tableTextPaint.setColor(COLOR_TEXT_DARK);
//        tableTextPaint.setTextSize(9.5f);
//
//        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        borderPaint.setColor(COLOR_BORDER);
//        borderPaint.setStyle(Paint.Style.STROKE);
//        borderPaint.setStrokeWidth(0.8f);
//
//        Paint bgLightPaint = new Paint();
//        bgLightPaint.setColor(COLOR_BG_LIGHT);
//        bgLightPaint.setStyle(Paint.Style.FILL);
//
//        Paint whiteBgPaint = new Paint();
//        whiteBgPaint.setColor(COLOR_WHITE);
//        whiteBgPaint.setStyle(Paint.Style.FILL);
//
//        // Calculate totals
//        double totalDebit = 0.0;
//        double totalCredit = 0.0;
//        if (transactions != null) {
//            for (Transaction t : transactions) {
//                if (t == null) continue;
//                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.getDefault());
//                double amt = t.getAmount();
//                if ("gave".equals(type) || "debit".equals(type) || "dr".equals(type)) {
//                    totalDebit += amt;
//                } else {
//                    totalCredit += amt;
//                }
//            }
//        } else {
//            transactions = new ArrayList<>();
//        }
//        double netBalance = openingBalance + totalCredit - totalDebit;
//
//        // Start first page
//        PdfDocument.Page page = document.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//
//        int marginH = 15;
//        int cursorY = 0;
//
//        // Blue header bar
//        canvas.drawRect(0, 0, pageWidth, 38, headerBgPaint);
//        canvas.drawText("All Record", marginH + 5, 24, headerTextPaint);
//
//        String logoText = "Khatabook";
//        float logoWidth = headerTextPaint.measureText(logoText);
//        canvas.drawText(logoText, pageWidth - marginH - logoWidth - 5, 24, headerTextPaint);
//
//        cursorY = 58;
//
//        // Title section
//        String statementTitle = customerName + " Statement";
//        drawCenteredText(canvas, statementTitle, pageWidth / 2f, cursorY, titlePaint);
//        cursorY += 20;
//
//        drawCenteredText(canvas, "Phone Number: " + customerPhone, pageWidth / 2f, cursorY, subtitlePaint);
//        cursorY += 16;
//
//        drawCenteredText(canvas, dateRangeLabel, pageWidth / 2f, cursorY, subtitlePaint);
//        cursorY += 26;
//
//        // Summary box
//        int boxLeft = marginH + 8;
//        int boxRight = pageWidth - marginH - 8;
//        int boxTop = cursorY;
//        int boxHeight = 70;
//        int boxBottom = boxTop + boxHeight;
//
//        // Draw box with border
//        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, whiteBgPaint);
//        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, borderPaint);
//
//        // Calculate 4 equal columns
//        int boxWidth = boxRight - boxLeft;
//        int colWidth = boxWidth / 4;
//
//        // Draw vertical separators
//        for (int i = 1; i < 4; i++) {
//            float x = boxLeft + (i * colWidth);
//            canvas.drawLine(x, boxTop, x, boxBottom, borderPaint);
//        }
//
//        int summaryLabelY = boxTop + 20;
//        int summaryValueY = boxTop + 40;
//        int summarySubY = boxTop + 56;
//
//        // Opening Balance
//        drawSummaryCell(canvas, "Opening Balance",
//                String.format(Locale.getDefault(), "₹%.2f", openingBalance),
//                "(on " + extractStartDate(dateRangeLabel) + ")",
//                boxLeft + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
//                labelPaint, valuePaint, subtitlePaint);
//
//        // Total Debit
//        Paint debitPaint = new Paint(valuePaint);
//        debitPaint.setColor(COLOR_RED);
//        drawSummaryCell(canvas, "Total Debit(-)",
//                String.format(Locale.getDefault(), "₹%.2f", totalDebit),
//                "",
//                boxLeft + colWidth + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
//                labelPaint, debitPaint, subtitlePaint);
//
//        // Total Credit
//        Paint creditPaint = new Paint(valuePaint);
//        creditPaint.setColor(COLOR_GREEN);
//        drawSummaryCell(canvas, "Total Credit(+)",
//                String.format(Locale.getDefault(), "₹%.2f", totalCredit),
//                "",
//                boxLeft + 2 * colWidth + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
//                labelPaint, creditPaint, subtitlePaint);
//
//        // Net Balance
//        boolean isDebit = netBalance < 0;
//        Paint netPaint = new Paint(valuePaint);
//        netPaint.setColor(isDebit ? COLOR_RED : COLOR_GREEN);
//        String netText = String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance));
//        if (isDebit) netText += " Dr";
//        String subText = "(" + customerName + " will give)";
//        drawSummaryCell(canvas, "Net Balance", netText, subText,
//                boxLeft + 3 * colWidth + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
//                labelPaint, netPaint, subtitlePaint);
//
//        cursorY = boxBottom + 22;
//
//        // Entry count
//        Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        countPaint.setColor(COLOR_TEXT_DARK);
//        countPaint.setTextSize(9.5f);
//        countPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        canvas.drawText("No. of Entries: " + transactions.size() + " (All)", marginH + 8, cursorY, countPaint);
//        cursorY += 18;
//
//        // Initialize table columns
//        TableColumns cols = new TableColumns(marginH + 8, pageWidth - marginH - 8);
//
//        // Draw table header
//        cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
//                tableHeaderPaint, bgLightPaint, borderPaint);
//
//        // Sort and group transactions
//        sortTransactionsByDate(transactions);
//        Map<String, List<Transaction>> monthlyGroups = groupByMonth(transactions);
//
//        double runningBalance = openingBalance;
//        int pageNumber = 1;
//        int totalPages = 1; // Will be updated if needed
//
//        for (Map.Entry<String, List<Transaction>> monthEntry : monthlyGroups.entrySet()) {
//            String monthName = monthEntry.getKey();
//            List<Transaction> monthTransactions = monthEntry.getValue();
//
//            // Check if we need new page for month header
//            if (cursorY + MONTH_HEADER_HEIGHT > pageHeight - 70) {
//                document.finishPage(page);
//                pageNumber++;
//                page = document.startPage(pageInfo);
//                canvas = page.getCanvas();
//                cursorY = 50;
//                cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
//                        tableHeaderPaint, bgLightPaint, borderPaint);
//            }
//
//            // Draw month header
//            cursorY = drawMonthHeader(canvas, cols, cursorY, MONTH_HEADER_HEIGHT,
//                    monthName, runningBalance,
//                    tableTextPaint, whiteBgPaint, borderPaint);
//
//            double monthDebit = 0;
//            double monthCredit = 0;
//
//            // Draw transaction rows
//            for (Transaction t : monthTransactions) {
//                // Check page break
//                if (cursorY + ROW_HEIGHT > pageHeight - 70) {
//                    document.finishPage(page);
//                    pageNumber++;
//                    page = document.startPage(pageInfo);
//                    canvas = page.getCanvas();
//                    cursorY = 50;
//                    cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
//                            tableHeaderPaint, bgLightPaint, borderPaint);
//                }
//
//                double debit = 0, credit = 0;
//                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.getDefault());
//                if ("gave".equals(type) || "debit".equals(type) || "dr".equals(type)) {
//                    debit = t.getAmount();
//                    runningBalance -= debit;
//                    monthDebit += debit;
//                } else {
//                    credit = t.getAmount();
//                    runningBalance += credit;
//                    monthCredit += credit;
//                }
//
//                cursorY = drawTransactionRow(canvas, cols, cursorY, ROW_HEIGHT,
//                        t, debit, credit, runningBalance,
//                        tableTextPaint, whiteBgPaint, borderPaint,
//                        COLOR_RED, COLOR_GREEN);
//            }
//
//            // Draw month total
//            if (cursorY + ROW_HEIGHT > pageHeight - 70) {
//                document.finishPage(page);
//                pageNumber++;
//                page = document.startPage(pageInfo);
//                canvas = page.getCanvas();
//                cursorY = 50;
//                cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
//                        tableHeaderPaint, bgLightPaint, borderPaint);
//            }
//
//            cursorY = drawMonthTotal(canvas, cols, cursorY, ROW_HEIGHT,
//                    monthName, monthDebit, monthCredit,
//                    tableTextPaint, bgLightPaint, borderPaint);
//        }
//
//        // Draw Grand Total
//        if (cursorY + ROW_HEIGHT > pageHeight - 70) {
//            document.finishPage(page);
//            pageNumber++;
//            page = document.startPage(pageInfo);
//            canvas = page.getCanvas();
//            cursorY = 50;
//        }
//
//        cursorY = drawGrandTotal(canvas, cols, cursorY, ROW_HEIGHT + 4,
//                totalDebit, totalCredit, netBalance,
//                tableTextPaint, bgLightPaint, borderPaint,
//                COLOR_RED, COLOR_GREEN);
//
//        // Footer
//        drawFooter(canvas, pageNumber, pageHeight);
//
//        document.finishPage(page);
//
//        // Save PDF
//        String fileName = "Statement_" + safeFileName(customerName) + "_" +
//                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
//
//        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//        if (dir == null) {
//            dir = new File(Environment.getExternalStorageDirectory(), "Documents");
//            if (!dir.exists()) dir.mkdirs();
//        }
//
//        File file = new File(dir, fileName);
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(file);
//            document.writeTo(fos);
//            Toast.makeText(context, "Statement saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(context, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            return null;
//        } finally {
//            document.close();
//            if (fos != null) {
//                try { fos.close(); } catch (IOException ignored) {}
//            }
//        }
//        return file;
//    }
//
//    private static int drawTableHeader(Canvas canvas, TableColumns cols, int startY, int height,
//                                       Paint textPaint, Paint bgPaint, Paint borderPaint) {
//        int top = startY;
//        int bottom = top + height;
//
//        // Draw background
//        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);
//
//        // Draw cell borders
//        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);
//
//        // Draw header text (centered vertically) with proper padding
//        int textY = top + (height / 2) + 4;
//        canvas.drawText("Date", cols.getDateTextX(), textY, textPaint);
//        canvas.drawText("Note", cols.getNoteTextX(), textY, textPaint);
//        canvas.drawText("Debit(-)", cols.getDebitTextX(), textY, textPaint);
//        canvas.drawText("Credit(+)", cols.getCreditTextX(), textY, textPaint);
//        canvas.drawText("Balance", cols.getBalanceTextX(), textY, textPaint);
//
//        return bottom;
//    }
//
//    private static int drawMonthHeader(Canvas canvas, TableColumns cols, int startY, int height,
//                                       String monthName, double openingBalance,
//                                       Paint textPaint, Paint bgPaint, Paint borderPaint) {
//        int top = startY;
//        int bottom = top + height;
//
//        // Draw background
//        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);
//
//        // Draw borders
//        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);
//
//        // Draw month name with proper padding
//        Paint monthPaint = new Paint(textPaint);
//        monthPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        monthPaint.setTextSize(10.5f);
//        canvas.drawText(monthName, cols.getDateTextX(), top + (height / 2) + 4, monthPaint);
//
//        // Draw opening balance with proper padding
//        Paint openingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        openingPaint.setColor(Color.parseColor("#757575"));
//        openingPaint.setTextSize(8.5f);
//        canvas.drawText("(Opening Balance: " + String.format(Locale.getDefault(), "%.2f", openingBalance) + ")",
//                cols.getBalanceTextX(), top + (height / 2) + 4, openingPaint);
//
//        return bottom;
//    }
//
//    private static int drawTransactionRow(Canvas canvas, TableColumns cols, int startY, int height,
//                                          Transaction t, double debit, double credit, double balance,
//                                          Paint textPaint, Paint bgPaint, Paint borderPaint,
//                                          int colorRed, int colorGreen) {
//        int top = startY;
//        int bottom = top + height;
//
//        // Draw background
//        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);
//
//        // Draw borders
//        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);
//
//        // Text Y position (centered)
//        int textY = top + (height / 2) + 4;
//
//        // Date with proper padding
//        canvas.drawText(formatShortDate(t.getDate()), cols.getDateTextX(), textY, textPaint);
//
//        // Note with proper padding and truncation
//        String note = t.getNote() != null ? t.getNote() : "";
//        int maxNoteWidth = cols.noteWidth - (cols.padding * 2);
//        String truncatedNote = truncateText(note, maxNoteWidth, textPaint);
//        canvas.drawText(truncatedNote, cols.getNoteTextX(), textY, textPaint);
//
//        // Debit with proper padding
//        if (debit > 0) {
//            canvas.drawText(String.format(Locale.getDefault(), "%.2f", debit),
//                    cols.getDebitTextX(), textY, textPaint);
//        }
//
//        // Credit with proper padding
//        if (credit > 0) {
//            canvas.drawText(String.format(Locale.getDefault(), "%.2f", credit),
//                    cols.getCreditTextX(), textY, textPaint);
//        }
//
//        // Balance with color and proper padding
//        Paint balPaint = new Paint(textPaint);
//        balPaint.setColor(balance < 0 ? colorRed : colorGreen);
//        String balText = String.format(Locale.getDefault(), "%.2f", Math.abs(balance));
//        if (balance < 0) balText += " Dr";
//        else balText += " Cr";
//        canvas.drawText(balText, cols.getBalanceTextX(), textY, balPaint);
//
//        return bottom;
//    }
//
//    private static int drawMonthTotal(Canvas canvas, TableColumns cols, int startY, int height,
//                                      String monthName, double debit, double credit,
//                                      Paint textPaint, Paint bgPaint, Paint borderPaint) {
//        int top = startY;
//        int bottom = top + height;
//
//        // Draw background
//        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);
//
//        // Draw borders
//        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);
//
//        // Text with proper padding
//        Paint totalPaint = new Paint(textPaint);
//        totalPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        int textY = top + (height / 2) + 4;
//
//        canvas.drawText(monthName + " Total", cols.getDateTextX(), textY, totalPaint);
//        canvas.drawText(String.format(Locale.getDefault(), "%.2f", debit),
//                cols.getDebitTextX(), textY, totalPaint);
//        canvas.drawText(String.format(Locale.getDefault(), "%.2f", credit),
//                cols.getCreditTextX(), textY, totalPaint);
//
//        return bottom;
//    }
//
//    private static int drawGrandTotal(Canvas canvas, TableColumns cols, int startY, int height,
//                                      double totalDebit, double totalCredit, double netBalance,
//                                      Paint textPaint, Paint bgPaint, Paint borderPaint,
//                                      int colorRed, int colorGreen) {
//        int top = startY;
//        int bottom = top + height;
//
//        // Draw background
//        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);
//
//        // Draw borders
//        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);
//
//        // Text with proper padding
//        Paint grandPaint = new Paint(textPaint);
//        grandPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        grandPaint.setTextSize(11f);
//        int textY = top + (height / 2) + 4;
//
//        canvas.drawText("Grand Total", cols.getDateTextX(), textY, grandPaint);
//        canvas.drawText(String.format(Locale.getDefault(), "%.2f", totalDebit),
//                cols.getDebitTextX(), textY, grandPaint);
//        canvas.drawText(String.format(Locale.getDefault(), "%.2f", totalCredit),
//                cols.getCreditTextX(), textY, grandPaint);
//
//        // Net balance with color and proper padding
//        Paint netPaint = new Paint(grandPaint);
//        netPaint.setColor(netBalance < 0 ? colorRed : colorGreen);
//        String netText = String.format(Locale.getDefault(), "%.2f", Math.abs(netBalance));
//        if (netBalance < 0) netText += " Dr";
//        canvas.drawText(netText, cols.getBalanceTextX(), textY, netPaint);
//
//        return bottom;
//    }
//
//    private static void drawTableRowBorders(Canvas canvas, TableColumns cols, int top, int bottom,
//                                            Paint borderPaint) {
//        // Draw horizontal lines
//        canvas.drawLine(cols.tableLeft, top, cols.tableRight, top, borderPaint);
//        canvas.drawLine(cols.tableLeft, bottom, cols.tableRight, bottom, borderPaint);
//
//        // Draw vertical lines
//        canvas.drawLine(cols.tableLeft, top, cols.tableLeft, bottom, borderPaint);
//        canvas.drawLine(cols.noteX, top, cols.noteX, bottom, borderPaint);
//        canvas.drawLine(cols.debitX, top, cols.debitX, bottom, borderPaint);
//        canvas.drawLine(cols.creditX, top, cols.creditX, bottom, borderPaint);
//        canvas.drawLine(cols.balanceX, top, cols.balanceX, bottom, borderPaint);
//        canvas.drawLine(cols.tableRight, top, cols.tableRight, bottom, borderPaint);
//    }
//
//    private static void drawSummaryCell(Canvas canvas, String label, String value, String subText,
//                                        int centerX, int labelY, int valueY, int subY,
//                                        Paint labelPaint, Paint valuePaint, Paint subPaint) {
//        // Draw centered text
//        drawCenteredText(canvas, label, centerX, labelY, labelPaint);
//        drawCenteredText(canvas, value, centerX, valueY, valuePaint);
//        if (subText != null && !subText.isEmpty()) {
//            drawCenteredText(canvas, subText, centerX, subY, subPaint);
//        }
//    }
//
//    private static void drawFooter(Canvas canvas, int pageNum, int pageHeight) {
//        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        footerPaint.setColor(Color.parseColor("#757575"));
//        footerPaint.setTextSize(8.5f);
//
//        String pageText = "Page " + pageNum + " of " + pageNum;
//        float textWidth = footerPaint.measureText(pageText);
//        canvas.drawText(pageText, (595 / 2f) - (textWidth / 2f), pageHeight - 20, footerPaint);
//    }
//
//    private static String extractStartDate(String dateRange) {
//        if (dateRange == null) return "";
//        if (dateRange.contains("-")) {
//            String[] parts = dateRange.split("-");
//            return parts[0].trim();
//        }
//        return dateRange;
//    }
//
//    private static Map<String, List<Transaction>> groupByMonth(List<Transaction> transactions) {
//        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();
//        if (transactions == null) return grouped;
//
//        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
//        DateFormat[] parseFormats = getDateFormats();
//
//        for (Transaction t : transactions) {
//            String raw = t == null ? null : t.getDate();
//            Date parsed = parseDate(raw, parseFormats);
//
//            String monthKey = parsed != null ? monthFormat.format(parsed) : "Unknown";
//            List<Transaction> list = grouped.get(monthKey);
//            if (list == null) {
//                list = new ArrayList<>();
//                grouped.put(monthKey, list);
//            }
//            list.add(t);
//        }
//        return grouped;
//    }
//
//    private static String formatShortDate(String rawDate) {
//        if (rawDate == null) return "-";
//        Date parsed = parseDate(rawDate, getDateFormats());
//        if (parsed != null) {
//            return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(parsed);
//        }
//        return rawDate.length() > 10 ? rawDate.substring(0, 10) : rawDate;
//    }
//
//    private static DateFormat[] getDateFormats() {
//        return new DateFormat[] {
//                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
//                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
//                new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()),
//                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
//                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//        };
//    }
//
//    private static Date parseDate(String dateStr, DateFormat[] formats) {
//        if (dateStr == null) return null;
//        dateStr = dateStr.trim();
//        for (DateFormat df : formats) {
//            try {
//                return df.parse(dateStr);
//            } catch (ParseException ignored) {}
//        }
//        return null;
//    }
//
//    private static void sortTransactionsByDate(List<Transaction> tx) {
//        if (tx == null) return;
//        final DateFormat[] formats = getDateFormats();
//        Collections.sort(tx, new Comparator<Transaction>() {
//            @Override
//            public int compare(Transaction o1, Transaction o2) {
//                Date d1 = parseDate(o1 == null ? null : o1.getDate(), formats);
//                Date d2 = parseDate(o2 == null ? null : o2.getDate(), formats);
//                if (d1 == null && d2 == null) return 0;
//                if (d1 == null) return -1;
//                if (d2 == null) return 1;
//                return d1.compareTo(d2);
//            }
//        });
//    }
//
//    private static String safeFileName(String name) {
//        if (name == null) name = "unknown";
//        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
//    }
//
//    private static String truncateText(String text, int maxWidth, Paint paint) {
//        if (text == null || text.isEmpty()) return "";
//
//        text = text.trim();
//        float textWidth = paint.measureText(text);
//
//        if (textWidth <= maxWidth) {
//            return text;
//        }
//
//        // Truncate with ellipsis
//        String ellipsis = "...";
//        float ellipsisWidth = paint.measureText(ellipsis);
//
//        int len = text.length();
//        while (len > 0) {
//            String truncated = text.substring(0, len);
//            if (paint.measureText(truncated) + ellipsisWidth <= maxWidth) {
//                return truncated + ellipsis;
//            }
//            len--;
//        }
//
//        return ellipsis;
//    }
//
//    private static void drawCenteredText(Canvas canvas, String text, float cx, int y, Paint paint) {
//        float w = paint.measureText(text);
//        canvas.drawText(text, cx - (w / 2f), y, paint);
//    }
//}






package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.sandhyasofttechh.mykhatapro.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PdfGenerator {

    // Table column definitions
    private static class TableColumns {
        int dateX, dateWidth;
        int noteX, noteWidth;
        int debitX, debitWidth;
        int creditX, creditWidth;
        int balanceX, balanceWidth;
        int tableLeft, tableRight;
        int padding = 8; // Padding inside each cell

        TableColumns(int left, int right) {
            tableLeft = left;
            tableRight = right;
            int totalWidth = right - left;

            // Define percentage-based widths
            dateWidth = (int)(totalWidth * 0.25);      // 25%
            noteWidth = (int)(totalWidth * 0.25);      // 25%
            debitWidth = (int)(totalWidth * 0.13);     // 13%
            creditWidth = (int)(totalWidth * 0.13);    // 13%
            balanceWidth = totalWidth - dateWidth - noteWidth - debitWidth - creditWidth; // Remaining (~24%)

            // Calculate X positions for column boundaries
            dateX = left;
            noteX = dateX + dateWidth;
            debitX = noteX + noteWidth;
            creditX = debitX + debitWidth;
            balanceX = creditX + creditWidth;
        }

        // Get text X position (with padding from left border)
        int getDateTextX() { return dateX + padding; }
        int getNoteTextX() { return noteX + padding; }
        int getDebitTextX() { return debitX + padding; }
        int getCreditTextX() { return creditX + padding; }
        int getBalanceTextX() { return balanceX + padding; }

        int[] getVerticalLines() {
            return new int[]{noteX, debitX, creditX, balanceX};
        }
    }

    public static File generatePdf(Context context,
                                   String customerName,
                                   String customerPhone,
                                   List<Transaction> transactions,
                                   String dateRangeLabel,
                                   double openingBalance) {

        final int pageWidth = 595;
        final int pageHeight = 842;
        final int ROW_HEIGHT = 24;
        final int HEADER_ROW_HEIGHT = 28;
        final int MONTH_HEADER_HEIGHT = 26;

        // Colors
        final int COLOR_HEADER = Color.parseColor("#1565C0");
        final int COLOR_TEXT_DARK = Color.parseColor("#212121");
        final int COLOR_TEXT_GRAY = Color.parseColor("#757575");
        final int COLOR_BORDER = Color.parseColor("#BDBDBD");
        final int COLOR_BG_LIGHT = Color.parseColor("#F5F5F5");
        final int COLOR_GREEN = Color.parseColor("#4CAF50");
        final int COLOR_RED = Color.parseColor("#F44336");
        final int COLOR_WHITE = Color.WHITE;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        // Initialize paints
        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(COLOR_HEADER);
        headerBgPaint.setStyle(Paint.Style.FILL);

        Paint headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(COLOR_WHITE);
        headerTextPaint.setTextSize(11f);
        headerTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(COLOR_TEXT_DARK);
        titlePaint.setTextSize(15f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(COLOR_TEXT_GRAY);
        subtitlePaint.setTextSize(9.5f);

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(COLOR_TEXT_GRAY);
        labelPaint.setTextSize(8.5f);

        Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(COLOR_TEXT_DARK);
        valuePaint.setTextSize(12f);
        valuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint tableHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableHeaderPaint.setColor(COLOR_TEXT_DARK);
        tableHeaderPaint.setTextSize(10f);
        tableHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint tableTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableTextPaint.setColor(COLOR_TEXT_DARK);
        tableTextPaint.setTextSize(9.5f);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(COLOR_BORDER);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(0.8f);

        Paint bgLightPaint = new Paint();
        bgLightPaint.setColor(COLOR_BG_LIGHT);
        bgLightPaint.setStyle(Paint.Style.FILL);

        Paint whiteBgPaint = new Paint();
        whiteBgPaint.setColor(COLOR_WHITE);
        whiteBgPaint.setStyle(Paint.Style.FILL);

        // Calculate totals
        double totalDebit = 0.0;
        double totalCredit = 0.0;
        if (transactions != null) {
            for (Transaction t : transactions) {
                if (t == null) continue;
                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.getDefault());
                double amt = t.getAmount();
                if ("gave".equals(type) || "debit".equals(type) || "dr".equals(type)) {
                    totalDebit += amt;
                } else {
                    totalCredit += amt;
                }
            }
        } else {
            transactions = new ArrayList<>();
        }
        double netBalance = openingBalance + totalCredit - totalDebit;

        // Start first page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int marginH = 15;
        int cursorY = 0;

        // Blue header bar
        canvas.drawRect(0, 0, pageWidth, 38, headerBgPaint);
        canvas.drawText("All Record", marginH + 5, 24, headerTextPaint);

        String logoText = "Khatabook";
        float logoWidth = headerTextPaint.measureText(logoText);
        canvas.drawText(logoText, pageWidth - marginH - logoWidth - 5, 24, headerTextPaint);

        cursorY = 58;

        // Title section
        String statementTitle = customerName + " Dyp Statement";
        drawCenteredText(canvas, statementTitle, pageWidth / 2f, cursorY, titlePaint);
        cursorY += 20;

        drawCenteredText(canvas, "Phone Number: " + customerPhone, pageWidth / 2f, cursorY, subtitlePaint);
        cursorY += 16;

        drawCenteredText(canvas, dateRangeLabel, pageWidth / 2f, cursorY, subtitlePaint);
        cursorY += 26;

        // Summary box
        int boxLeft = marginH + 8;
        int boxRight = pageWidth - marginH - 8;
        int boxTop = cursorY;
        int boxHeight = 70;
        int boxBottom = boxTop + boxHeight;

        // Draw box with border
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, whiteBgPaint);
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, borderPaint);

        // Calculate 4 equal columns
        int boxWidth = boxRight - boxLeft;
        int colWidth = boxWidth / 4;

        // Draw vertical separators
        for (int i = 1; i < 4; i++) {
            float x = boxLeft + (i * colWidth);
            canvas.drawLine(x, boxTop, x, boxBottom, borderPaint);
        }

        int summaryLabelY = boxTop + 20;
        int summaryValueY = boxTop + 40;
        int summarySubY = boxTop + 56;

        // Opening Balance
        drawSummaryCell(canvas, "Opening Balance",
                String.format(Locale.getDefault(), "₹%.2f", openingBalance),
                "(on " + extractStartDate(dateRangeLabel) + ")",
                boxLeft + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
                labelPaint, valuePaint, subtitlePaint);

        // Total Debit
        Paint debitPaint = new Paint(valuePaint);
        debitPaint.setColor(COLOR_RED);
        drawSummaryCell(canvas, "Total Debit(-)",
                String.format(Locale.getDefault(), "₹%.2f", totalDebit),
                "",
                boxLeft + colWidth + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
                labelPaint, debitPaint, subtitlePaint);

        // Total Credit
        Paint creditPaint = new Paint(valuePaint);
        creditPaint.setColor(COLOR_GREEN);
        drawSummaryCell(canvas, "Total Credit(+)",
                String.format(Locale.getDefault(), "₹%.2f", totalCredit),
                "",
                boxLeft + 2 * colWidth + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
                labelPaint, creditPaint, subtitlePaint);

        // Net Balance
        boolean isDebit = netBalance < 0;
        Paint netPaint = new Paint(valuePaint);
        netPaint.setColor(isDebit ? COLOR_RED : COLOR_GREEN);
        String netText = String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance));
        if (isDebit) netText += " Dr";
        String subText = "(" + customerName + " will give)";
        drawSummaryCell(canvas, "Net Balance", netText, subText,
                boxLeft + 3 * colWidth + colWidth / 2, summaryLabelY, summaryValueY, summarySubY,
                labelPaint, netPaint, subtitlePaint);

        cursorY = boxBottom + 22;

        // Entry count
        Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        countPaint.setColor(COLOR_TEXT_DARK);
        countPaint.setTextSize(9.5f);
        countPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("No. of Entries: " + transactions.size() + " (All)", marginH + 8, cursorY, countPaint);
        cursorY += 18;

        // Initialize table columns
        TableColumns cols = new TableColumns(marginH + 8, pageWidth - marginH - 8);

        // Draw table header
        cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
                tableHeaderPaint, bgLightPaint, borderPaint);

        // Sort and group transactions
        sortTransactionsByDate(transactions);
        Map<String, List<Transaction>> monthlyGroups = groupByMonth(transactions);

        double runningBalance = openingBalance;
        int pageNumber = 1;
        int totalPages = 1; // Will be updated if needed

        for (Map.Entry<String, List<Transaction>> monthEntry : monthlyGroups.entrySet()) {
            String monthName = monthEntry.getKey();
            List<Transaction> monthTransactions = monthEntry.getValue();

            // Check if we need new page for month header
            if (cursorY + MONTH_HEADER_HEIGHT > pageHeight - 70) {
                document.finishPage(page);
                pageNumber++;
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                cursorY = 50;
                cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
                        tableHeaderPaint, bgLightPaint, borderPaint);
            }

            // Draw month header
            cursorY = drawMonthHeader(canvas, cols, cursorY, MONTH_HEADER_HEIGHT,
                    monthName, runningBalance,
                    tableTextPaint, whiteBgPaint, borderPaint);

            double monthDebit = 0;
            double monthCredit = 0;

            // Draw transaction rows
            for (Transaction t : monthTransactions) {
                // Check page break
                if (cursorY + ROW_HEIGHT > pageHeight - 70) {
                    document.finishPage(page);
                    pageNumber++;
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    cursorY = 50;
                    cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
                            tableHeaderPaint, bgLightPaint, borderPaint);
                }

                double debit = 0, credit = 0;
                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.getDefault());
                if ("gave".equals(type) || "debit".equals(type) || "dr".equals(type)) {
                    debit = t.getAmount();
                    runningBalance -= debit;
                    monthDebit += debit;
                } else {
                    credit = t.getAmount();
                    runningBalance += credit;
                    monthCredit += credit;
                }

                cursorY = drawTransactionRow(canvas, cols, cursorY, ROW_HEIGHT,
                        t, debit, credit, runningBalance,
                        tableTextPaint, whiteBgPaint, borderPaint,
                        COLOR_RED, COLOR_GREEN);
            }

            // Draw month total
            if (cursorY + ROW_HEIGHT > pageHeight - 70) {
                document.finishPage(page);
                pageNumber++;
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                cursorY = 50;
                cursorY = drawTableHeader(canvas, cols, cursorY, HEADER_ROW_HEIGHT,
                        tableHeaderPaint, bgLightPaint, borderPaint);
            }

            cursorY = drawMonthTotal(canvas, cols, cursorY, ROW_HEIGHT,
                    monthName, monthDebit, monthCredit,
                    tableTextPaint, bgLightPaint, borderPaint);
        }

        // Draw Grand Total
        if (cursorY + ROW_HEIGHT > pageHeight - 70) {
            document.finishPage(page);
            pageNumber++;
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            cursorY = 50;
        }

        cursorY = drawGrandTotal(canvas, cols, cursorY, ROW_HEIGHT + 4,
                totalDebit, totalCredit, netBalance,
                tableTextPaint, bgLightPaint, borderPaint,
                COLOR_RED, COLOR_GREEN);

        // Footer
        drawFooter(canvas, pageNumber, pageHeight);

        document.finishPage(page);

        // Save PDF
        String fileName = "Statement_" + safeFileName(customerName) + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) {
            dir = new File(Environment.getExternalStorageDirectory(), "Documents");
            if (!dir.exists()) dir.mkdirs();
        }

        File file = new File(dir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            document.writeTo(fos);
            Toast.makeText(context, "Statement saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        } finally {
            document.close();
            if (fos != null) {
                try { fos.close(); } catch (IOException ignored) {}
            }
        }
        return file;
    }

    private static int drawTableHeader(Canvas canvas, TableColumns cols, int startY, int height,
                                       Paint textPaint, Paint bgPaint, Paint borderPaint) {
        int top = startY;
        int bottom = top + height;

        // Draw background
        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);

        // Draw cell borders
        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);

        // Draw header text (centered vertically) with proper padding
        int textY = top + (height / 2) + 4;
        canvas.drawText("Date", cols.getDateTextX(), textY, textPaint);
        canvas.drawText("Note", cols.getNoteTextX(), textY, textPaint);
        canvas.drawText("Debit(-)", cols.getDebitTextX(), textY, textPaint);
        canvas.drawText("Credit(+)", cols.getCreditTextX(), textY, textPaint);
        canvas.drawText("Balance", cols.getBalanceTextX(), textY, textPaint);

        return bottom;
    }

    private static int drawMonthHeader(Canvas canvas, TableColumns cols, int startY, int height,
                                       String monthName, double openingBalance,
                                       Paint textPaint, Paint bgPaint, Paint borderPaint) {
        int top = startY;
        int bottom = top + height;

        // Draw background
        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);

        // Draw borders
        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);

        // Draw month name with proper padding
        Paint monthPaint = new Paint(textPaint);
        monthPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        monthPaint.setTextSize(10.5f);
        canvas.drawText(monthName, cols.getDateTextX(), top + (height / 2) + 4, monthPaint);

        // Draw opening balance with proper padding
        Paint openingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        openingPaint.setColor(Color.parseColor("#757575"));
        openingPaint.setTextSize(8.5f);
        canvas.drawText("(Opening Balance: " + String.format(Locale.getDefault(), "%.2f", openingBalance) + ")",
                cols.getBalanceTextX(), top + (height / 2) + 4, openingPaint);

        return bottom;
    }

    private static int drawTransactionRow(Canvas canvas, TableColumns cols, int startY, int height,
                                          Transaction t, double debit, double credit, double balance,
                                          Paint textPaint, Paint bgPaint, Paint borderPaint,
                                          int colorRed, int colorGreen) {
        int top = startY;
        int bottom = top + height;

        // Draw background
        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);

        // Draw borders
        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);

        // Text Y position (centered)
        int textY = top + (height / 2) + 4;

        // Date with proper padding
        canvas.drawText(formatShortDate(t.getDate()), cols.getDateTextX(), textY, textPaint);

        // Note with proper padding and truncation
        String note = t.getNote() != null ? t.getNote() : "";
        int maxNoteWidth = cols.noteWidth - (cols.padding * 2);
        String truncatedNote = truncateText(note, maxNoteWidth, textPaint);
        canvas.drawText(truncatedNote, cols.getNoteTextX(), textY, textPaint);

        // Debit with proper padding
        if (debit > 0) {
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", debit),
                    cols.getDebitTextX(), textY, textPaint);
        }

        // Credit with proper padding
        if (credit > 0) {
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", credit),
                    cols.getCreditTextX(), textY, textPaint);
        }

        // Balance with color and proper padding
        Paint balPaint = new Paint(textPaint);
        balPaint.setColor(balance < 0 ? colorRed : colorGreen);
        String balText = String.format(Locale.getDefault(), "%.2f", Math.abs(balance));
        if (balance < 0) balText += " Dr";
        else balText += " Cr";
        canvas.drawText(balText, cols.getBalanceTextX(), textY, balPaint);

        return bottom;
    }

    private static int drawMonthTotal(Canvas canvas, TableColumns cols, int startY, int height,
                                      String monthName, double debit, double credit,
                                      Paint textPaint, Paint bgPaint, Paint borderPaint) {
        int top = startY;
        int bottom = top + height;

        // Draw background
        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);

        // Draw borders
        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);

        // Text with proper padding
        Paint totalPaint = new Paint(textPaint);
        totalPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int textY = top + (height / 2) + 4;

        canvas.drawText(monthName + " Total", cols.getDateTextX(), textY, totalPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.2f", debit),
                cols.getDebitTextX(), textY, totalPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.2f", credit),
                cols.getCreditTextX(), textY, totalPaint);

        return bottom;
    }

    private static int drawGrandTotal(Canvas canvas, TableColumns cols, int startY, int height,
                                      double totalDebit, double totalCredit, double netBalance,
                                      Paint textPaint, Paint bgPaint, Paint borderPaint,
                                      int colorRed, int colorGreen) {
        int top = startY;
        int bottom = top + height;

        // Draw background
        canvas.drawRect(cols.tableLeft, top, cols.tableRight, bottom, bgPaint);

        // Draw borders
        drawTableRowBorders(canvas, cols, top, bottom, borderPaint);

        // Text with proper padding
        Paint grandPaint = new Paint(textPaint);
        grandPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        grandPaint.setTextSize(11f);
        int textY = top + (height / 2) + 4;

        canvas.drawText("Grand Total", cols.getDateTextX(), textY, grandPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.2f", totalDebit),
                cols.getDebitTextX(), textY, grandPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.2f", totalCredit),
                cols.getCreditTextX(), textY, grandPaint);

        // Net balance with color and proper padding
        Paint netPaint = new Paint(grandPaint);
        netPaint.setColor(netBalance < 0 ? colorRed : colorGreen);
        String netText = String.format(Locale.getDefault(), "%.2f", Math.abs(netBalance));
        if (netBalance < 0) netText += " Dr";
        canvas.drawText(netText, cols.getBalanceTextX(), textY, netPaint);

        return bottom;
    }

    private static void drawTableRowBorders(Canvas canvas, TableColumns cols, int top, int bottom,
                                            Paint borderPaint) {
        // Draw horizontal lines
        canvas.drawLine(cols.tableLeft, top, cols.tableRight, top, borderPaint);
        canvas.drawLine(cols.tableLeft, bottom, cols.tableRight, bottom, borderPaint);

        // Draw vertical lines
        canvas.drawLine(cols.tableLeft, top, cols.tableLeft, bottom, borderPaint);
        canvas.drawLine(cols.noteX, top, cols.noteX, bottom, borderPaint);
        canvas.drawLine(cols.debitX, top, cols.debitX, bottom, borderPaint);
        canvas.drawLine(cols.creditX, top, cols.creditX, bottom, borderPaint);
        canvas.drawLine(cols.balanceX, top, cols.balanceX, bottom, borderPaint);
        canvas.drawLine(cols.tableRight, top, cols.tableRight, bottom, borderPaint);
    }

    private static void drawSummaryCell(Canvas canvas, String label, String value, String subText,
                                        int centerX, int labelY, int valueY, int subY,
                                        Paint labelPaint, Paint valuePaint, Paint subPaint) {
        // Draw centered text
        drawCenteredText(canvas, label, centerX, labelY, labelPaint);
        drawCenteredText(canvas, value, centerX, valueY, valuePaint);
        if (subText != null && !subText.isEmpty()) {
            drawCenteredText(canvas, subText, centerX, subY, subPaint);
        }
    }

    private static void drawFooter(Canvas canvas, int pageNum, int pageHeight) {
        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setColor(Color.parseColor("#757575"));
        footerPaint.setTextSize(8.5f);

        String pageText = "Page " + pageNum + " of " + pageNum;
        float textWidth = footerPaint.measureText(pageText);
        canvas.drawText(pageText, (595 / 2f) - (textWidth / 2f), pageHeight - 20, footerPaint);
    }

    private static String extractStartDate(String dateRange) {
        if (dateRange == null) return "";
        if (dateRange.contains("-")) {
            String[] parts = dateRange.split("-");
            return parts[0].trim();
        }
        return dateRange;
    }

    private static Map<String, List<Transaction>> groupByMonth(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();
        if (transactions == null) return grouped;

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        DateFormat[] parseFormats = getDateFormats();

        for (Transaction t : transactions) {
            String raw = t == null ? null : t.getDate();
            Date parsed = parseDate(raw, parseFormats);

            String monthKey = parsed != null ? monthFormat.format(parsed) : "Unknown";
            List<Transaction> list = grouped.get(monthKey);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(monthKey, list);
            }
            list.add(t);
        }
        return grouped;
    }

    private static String formatShortDate(String rawDate) {
        if (rawDate == null) return "-";
        Date parsed = parseDate(rawDate, getDateFormats());
        if (parsed != null) {
            return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(parsed);
        }
        return rawDate.length() > 10 ? rawDate.substring(0, 10) : rawDate;
    }

    private static DateFormat[] getDateFormats() {
        return new DateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        };
    }

    private static Date parseDate(String dateStr, DateFormat[] formats) {
        if (dateStr == null) return null;
        dateStr = dateStr.trim();
        for (DateFormat df : formats) {
            try {
                return df.parse(dateStr);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    private static void sortTransactionsByDate(List<Transaction> tx) {
        if (tx == null) return;
        final DateFormat[] formats = getDateFormats();
        Collections.sort(tx, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                Date d1 = parseDate(o1 == null ? null : o1.getDate(), formats);
                Date d2 = parseDate(o2 == null ? null : o2.getDate(), formats);
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return -1;
                if (d2 == null) return 1;
                return d1.compareTo(d2);
            }
        });
    }

    private static String safeFileName(String name) {
        if (name == null) name = "unknown";
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static String truncateText(String text, int maxWidth, Paint paint) {
        if (text == null || text.isEmpty()) return "";

        text = text.trim();
        float textWidth = paint.measureText(text);

        if (textWidth <= maxWidth) {
            return text;
        }

        // Truncate with ellipsis
        String ellipsis = "...";
        float ellipsisWidth = paint.measureText(ellipsis);

        int len = text.length();
        while (len > 0) {
            String truncated = text.substring(0, len);
            if (paint.measureText(truncated) + ellipsisWidth <= maxWidth) {
                return truncated + ellipsis;
            }
            len--;
        }

        return ellipsis;
    }

    private static void drawCenteredText(Canvas canvas, String text, float cx, int y, Paint paint) {
        float w = paint.measureText(text);
        canvas.drawText(text, cx - (w / 2f), y, paint);
    }
}