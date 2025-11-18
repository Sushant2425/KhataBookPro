package com.sandhyasofttechh.mykhatapro.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.sandhyasofttechh.mykhatapro.fragments.CustomerReportFragment.Transaction;
import com.sandhyasofttechh.mykhatapro.fragments.CustomerListFragment.CustomerSummary; // ensure correct import or package

import java.util.List;

public class TableDrawer {

    private final Canvas canvas;
    private final PdfUtils utils;


    // X-coordinates for columns
    private final float[] colX = {40, 130, 260, 370, 470};
    private final float tableWidth = 515;
    private final float rowHeight = 28;
    private final float headerHeight = 34;

    public TableDrawer(Canvas canvas, PdfUtils utils) {
        this.canvas = canvas;
        this.utils = utils;
    }

    /**
     * Draw customer header info (name, phone, balance)
     */
    public void drawHeader(String name, String phone, String balance) {
        float y = 120;
        canvas.drawText("Customer: " + name, colX[0], y, utils.getHeaderPaint());
        y += 22;
        canvas.drawText("Phone: " + phone, colX[0], y, utils.getTextPaint());
        y += 22;
        canvas.drawText("Balance: " + balance, colX[0], y, utils.getHeaderPaint());
    }

    /**
     * Draw transaction table (for individual transactions)
     */
    public void drawTransactionTable(List<Transaction> list) {
        float startY = 190;
        float maxY = 780;
        drawTableRow(startY, true, "Date", "Type", "Amount", "Note");
        float curY = startY + headerHeight;

        Paint rowBg = new Paint();
        rowBg.setColor(0xFFF5F5F5); // light gray

        for (int i = 0; i < list.size(); i++) {
            if (curY + rowHeight > maxY) break;
            Transaction t = list.get(i);
            if (i % 2 == 1) {
                RectF rect = new RectF(colX[0], curY, colX[0] + tableWidth, curY + rowHeight);
                canvas.drawRect(rect, rowBg);
            }
            String date = t.date != null ? t.date : "-";
            String type = t.type != null ? capitalize(t.type) : "-";
            String amount = String.format("₹%.2f", t.amount);
            String note = t.note != null ? t.note : "-";
            if (utils.getTextPaint().measureText(note) > (colX[4] - colX[3] - 10)) {
                note = note.substring(0, Math.min(30, note.length())) + "...";
            }
            drawTableRow(curY + rowHeight - 6, false, date, type, amount, note);
            curY += rowHeight;
        }
        // bottom line
        canvas.drawLine(colX[0], curY, colX[0] + tableWidth, curY, utils.getLinePaint());
    }

    /**
     * Draw Customers List Table (for all customer summaries)
     */
    public void drawCustomerListTable(List<CustomerSummary> list) {
        float startY = 190;
        float maxY = 780;

        // Draw header background
        Paint headerBg = new Paint();
        headerBg.setColor(0xFF1565C0); // brand blue
        canvas.drawRect(colX[0], startY, colX[3] + 100, startY + headerHeight, headerBg);

        // Header text
        Paint headerText = new Paint(utils.getHeaderPaint());
        headerText.setColor(0xFFFFFFFF); // white
        drawCustomerListRow(startY + headerHeight - 10, true, "Name", "Phone", "Balance", headerText);

        float curY = startY + headerHeight;

        Paint rowBg = new Paint();
        rowBg.setColor(0xFFF9F9F9); // light background

        for (int i = 0; i < list.size(); i++) {
            if (curY + rowHeight > maxY) break;
            if (i % 2 == 1) {
                // alternate shading
                RectF rect = new RectF(colX[0], curY, colX[3] + 100, curY + rowHeight);
                canvas.drawRect(rect, rowBg);
            }

            CustomerSummary c = list.get(i);
            String name = c.name != null ? c.name : "-";
            String phone = c.phone != null ? c.phone : "-";
            String balance = String.format("₹%.2f", c.balance);
            drawCustomerListRow(curY + rowHeight - 10, false, name, phone, balance, null);
            curY += rowHeight;
        }
        // bottom line
        canvas.drawLine(colX[0], curY, colX[3] + 100, curY, utils.getLinePaint());
    }

    /**
     * Draw a customer list row with 3 columns: Name, Phone, Balance
     */
    private void drawCustomerListRow(float y, boolean isHeader, String name, String phone, String balance, Paint headerText) {
        Paint paint = isHeader ? (headerText != null ? headerText : utils.getHeaderPaint()) : utils.getTextPaint();
        Paint linePaint = utils.getLinePaint();

        float[] columns = {colX[0], colX[1], colX[3]};
        float tableRight = colX[3] + 100;

        // Vertical grid lines
        for (int i = 0; i <= 3; i++) {
            float x = (i < 3) ? columns[i] : tableRight;
            canvas.drawLine(x, y - (isHeader ? headerHeight : rowHeight), x, y, linePaint);
        }

        // Horizontal grid line
        canvas.drawLine(colX[0], y - (isHeader ? headerHeight : rowHeight), tableRight, y - (isHeader ? headerHeight : rowHeight), linePaint);
        canvas.drawLine(colX[0], y, tableRight, y, linePaint);

        float padding = 4;
        // Draw the columns
        canvas.drawText(name, columns[0] + padding, y - (isHeader ? 12 : 8), paint);
        canvas.drawText(phone, columns[1] + padding, y - (isHeader ? 12 : 8), paint);

        // Right align balance
        float balanceWidth = paint.measureText(balance);
        float balanceX = columns[2] + (tableRight - columns[2]) - balanceWidth - padding;
        canvas.drawText(balance, balanceX, y - (isHeader ? 12 : 8), paint);
    }

    /**
     * Draw a table row with given cells (max 4).
     */
    private void drawTableRow(float y, boolean isHeader, String... cells) {
        Paint paint = isHeader ? utils.getHeaderPaint() : utils.getTextPaint();

        // Vertical lines for columns
        for (int i = 0; i <= 4; i++) {
            canvas.drawLine(colX[i], y, colX[i], y + (isHeader ? headerHeight : rowHeight), utils.getLinePaint());
        }

        // Horizontal line for row top
        canvas.drawLine(colX[0], y, colX[0] + tableWidth, y, utils.getLinePaint());
        // Bottom line
        canvas.drawLine(colX[0], y + (isHeader ? headerHeight : rowHeight),
                colX[0] + tableWidth, y + (isHeader ? headerHeight : rowHeight), utils.getLinePaint());

        // Draw cell texts
        for (int i = 0; i < cells.length && i < 4; i++) {
            // padding of 4px
            canvas.drawText(cells[i], colX[i] + 4, y + (isHeader ? 24 : 20), paint);
        }
    }

    /**
     * Utility to capitalize first letter.
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
