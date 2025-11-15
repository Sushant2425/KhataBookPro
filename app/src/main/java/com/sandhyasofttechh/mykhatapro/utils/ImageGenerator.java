package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.core.content.ContextCompat;
import com.sandhyasofttechh.mykhatapro.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class ImageGenerator {

    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 600;
    private static final int PADDING = 50;

    public static File generateShareableImage(Context context, String customerName, double netBalance, String appName) {
        Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // --- Styles ---
        TextPaint titlePaint = createTextPaint(38, Color.WHITE, true);
        TextPaint customerPaint = createTextPaint(28, Color.DKGRAY, false);
        TextPaint balanceLabelPaint = createTextPaint(32, Color.GRAY, false);
        TextPaint balanceAmountPaint = createTextPaint(72, Color.BLACK, true);
        TextPaint footerPaint = createTextPaint(22, Color.GRAY, false);
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.LTGRAY);
        borderPaint.setStrokeWidth(10);
        
        int primaryColor = ContextCompat.getColor(context, R.color.primary);

        // --- Draw Layout ---
        // Background
        canvas.drawColor(Color.WHITE);

        // Header
        canvas.drawRect(0, 0, IMAGE_WIDTH, 120, new Paint(){{setColor(primaryColor);}});
        drawCenteredText(canvas, appName, IMAGE_WIDTH / 2, 75, titlePaint);

        // Customer Name
        drawCenteredText(canvas, "Reminder for: " + customerName, IMAGE_WIDTH / 2, 200, customerPaint);

        // Balance Details
        String balanceLabel;
        int balanceColor;
        if (netBalance > 0) {
            balanceLabel = "You Will Get";
            balanceColor = ContextCompat.getColor(context, R.color.green);
        } else if (netBalance < 0) {
            balanceLabel = "You Will Give";
            balanceColor = ContextCompat.getColor(context, R.color.error);
        } else {
            balanceLabel = "Account is Settled";
            balanceColor = Color.BLACK;
        }
        balanceAmountPaint.setColor(balanceColor);
        
        drawCenteredText(canvas, balanceLabel, IMAGE_WIDTH / 2, 300, balanceLabelPaint);
        String balanceText = String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance));
        drawCenteredText(canvas, balanceText, IMAGE_WIDTH / 2, 380, balanceAmountPaint);
        
        // Footer & Border
        canvas.drawRect(0, IMAGE_HEIGHT - 60, IMAGE_WIDTH, IMAGE_HEIGHT, new Paint(){{setColor(Color.parseColor("#F5F5F5"));}});
        drawCenteredText(canvas, "Thank you for your business!", IMAGE_WIDTH / 2, IMAGE_HEIGHT - 25, footerPaint);
        canvas.drawRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, borderPaint);

        // --- Save the Bitmap ---
        return saveBitmapToFile(context, bitmap);
    }

    private static TextPaint createTextPaint(int size, int color, boolean isBold) {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(size);
        paint.setColor(color);
        paint.setFakeBoldText(isBold);
        return paint;
    }

    private static void drawCenteredText(Canvas canvas, String text, int centerX, int y, TextPaint paint) {
        float textWidth = paint.measureText(text);
        canvas.drawText(text, centerX - (textWidth / 2), y, paint);
    }

    private static File saveBitmapToFile(Context context, Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "reminder.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}