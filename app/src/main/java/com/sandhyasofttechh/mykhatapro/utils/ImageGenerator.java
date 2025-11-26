//package com.sandhyasofttechh.mykhatapro.utils;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.text.Layout;
//import android.text.StaticLayout;
//import android.text.TextPaint;
//import androidx.core.content.ContextCompat;
//import com.sandhyasofttechh.mykhatapro.R;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Locale;
//
//public class ImageGenerator {
//
//    private static final int IMAGE_WIDTH = 800;
//    private static final int IMAGE_HEIGHT = 600;
//
//    private static final int PADDING = 50;
//
//    public static File generateShareableImage(Context context, String customerName, double netBalance, String appName) {
//        Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//
//        // --- Styles ---
//        TextPaint titlePaint = createTextPaint(38, Color.WHITE, true);
//        TextPaint customerPaint = createTextPaint(28, Color.DKGRAY, false);
//        TextPaint balanceLabelPaint = createTextPaint(32, Color.GRAY, false);
//        TextPaint balanceAmountPaint = createTextPaint(72, Color.BLACK, true);
//        TextPaint footerPaint = createTextPaint(22, Color.GRAY, false);
//        Paint borderPaint = new Paint();
//        borderPaint.setStyle(Paint.Style.STROKE);
//        borderPaint.setColor(Color.LTGRAY);
//        borderPaint.setStrokeWidth(10);
//
//        int primaryColor = ContextCompat.getColor(context, R.color.primary);
//
//        // --- Draw Layout ---
//        // Background
//        canvas.drawColor(Color.WHITE);
//
//        // Header
//        canvas.drawRect(0, 0, IMAGE_WIDTH, 120, new Paint(){{setColor(primaryColor);}});
//        drawCenteredText(canvas, appName, IMAGE_WIDTH / 2, 75, titlePaint);
//
//        // Customer Name
//        drawCenteredText(canvas, "Reminder for: " + customerName, IMAGE_WIDTH / 2, 200, customerPaint);
//
//        // Balance Details
//        String balanceLabel;
//        int balanceColor;
//        if (netBalance > 0) {
//            balanceLabel = "You Will Get";
//            balanceColor = ContextCompat.getColor(context, R.color.green);
//        } else if (netBalance < 0) {
//            balanceLabel = "You Will Give";
//            balanceColor = ContextCompat.getColor(context, R.color.error);
//        } else {
//            balanceLabel = "Account is Settled";
//            balanceColor = Color.BLACK;
//        }
//        balanceAmountPaint.setColor(balanceColor);
//
//        drawCenteredText(canvas, balanceLabel, IMAGE_WIDTH / 2, 300, balanceLabelPaint);
//        String balanceText = String.format(Locale.getDefault(), "₹%.2f", Math.abs(netBalance));
//        drawCenteredText(canvas, balanceText, IMAGE_WIDTH / 2, 380, balanceAmountPaint);
//
//        // Footer & Border
//        canvas.drawRect(0, IMAGE_HEIGHT - 60, IMAGE_WIDTH, IMAGE_HEIGHT, new Paint(){{setColor(Color.parseColor("#F5F5F5"));}});
//        drawCenteredText(canvas, "Thank you for your business!", IMAGE_WIDTH / 2, IMAGE_HEIGHT - 25, footerPaint);
//        canvas.drawRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, borderPaint);
//
//        // --- Save the Bitmap ---
//        return saveBitmapToFile(context, bitmap);
//    }
//
//    private static TextPaint createTextPaint(int size, int color, boolean isBold) {
//        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//        paint.setTextSize(size);
//        paint.setColor(color);
//        paint.setFakeBoldText(isBold);
//        return paint;
//    }
//
//    private static void drawCenteredText(Canvas canvas, String text, int centerX, int y, TextPaint paint) {
//        float textWidth = paint.measureText(text);
//        canvas.drawText(text, centerX - (textWidth / 2), y, paint);
//    }
//
//    private static File saveBitmapToFile(Context context, Bitmap bitmap) {
//        try {
//            File cachePath = new File(context.getCacheDir(), "images");
//            cachePath.mkdirs();
//            File imageFile = new File(cachePath, "reminder.png");
//            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            }
//            return imageFile;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}




package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import androidx.core.content.ContextCompat;
import com.sandhyasofttechh.mykhatapro.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class ImageGenerator {

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1350;
    private static final int CARD_MARGIN = 80;
    private static final int CORNER_RADIUS = 40;

    public static File generateShareableImage(Context context, String customerName, double netBalance, String appName) {
        Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // --- Paint Objects ---
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint iconBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TextPaint appNamePaint = createTextPaint(48, Color.WHITE, true);
        TextPaint taglinePaint = createTextPaint(28, Color.parseColor("#E0E0E0"), false);
        TextPaint labelPaint = createTextPaint(32, Color.parseColor("#757575"), false);
        TextPaint customerPaint = createTextPaint(52, Color.parseColor("#212121"), true);
        TextPaint statusLabelPaint = createTextPaint(36, Color.parseColor("#616161"), false);
        TextPaint amountPaint = createTextPaint(96, Color.BLACK, true);
        TextPaint footerPaint = createTextPaint(28, Color.parseColor("#9E9E9E"), false);
        TextPaint footerBoldPaint = createTextPaint(30, Color.parseColor("#757575"), true);

        int primaryColor = ContextCompat.getColor(context, R.color.primary);
        int primaryDark = darkenColor(primaryColor, 0.3f);

        // --- Background Gradient ---
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, IMAGE_HEIGHT,
                primaryColor, primaryDark,
                Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, bgPaint);

        // --- Header Section ---
        drawCenteredText(canvas, appName, IMAGE_WIDTH / 2, 120, appNamePaint);
        drawCenteredText(canvas, "Payment Reminder", IMAGE_WIDTH / 2, 180, taglinePaint);

        // --- Main Card with Shadow ---
        RectF shadowRect = new RectF(CARD_MARGIN + 10, 280, IMAGE_WIDTH - CARD_MARGIN + 10, IMAGE_HEIGHT - 200);
        shadowPaint.setColor(Color.parseColor("#40000000"));
        shadowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(30, android.graphics.BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(shadowRect, CORNER_RADIUS, CORNER_RADIUS, shadowPaint);

        // Main Card
        RectF cardRect = new RectF(CARD_MARGIN, 270, IMAGE_WIDTH - CARD_MARGIN, IMAGE_HEIGHT - 210);
        cardPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(cardRect, CORNER_RADIUS, CORNER_RADIUS, cardPaint);

        // --- Card Content ---
        int cardContentY = 380;

        // Customer Label
        drawCenteredText(canvas, "CUSTOMER", IMAGE_WIDTH / 2, cardContentY, labelPaint);

        // Customer Name
        drawCenteredText(canvas, customerName, IMAGE_WIDTH / 2, cardContentY + 90, customerPaint);

        // Divider Line
        Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.parseColor("#E0E0E0"));
        dividerPaint.setStrokeWidth(3);
        canvas.drawLine(CARD_MARGIN + 120, cardContentY + 140, IMAGE_WIDTH - CARD_MARGIN - 120, cardContentY + 140, dividerPaint);

        // Balance Section
        String statusLabel;
        int statusColor;
        String statusIcon;

        if (netBalance > 0) {
            statusLabel = "YOU WILL RECEIVE";
            statusColor = Color.parseColor("#4CAF50");
            statusIcon = "↓";
        } else if (netBalance < 0) {
            statusLabel = "YOU WILL PAY";
            statusColor = Color.parseColor("#F44336");
            statusIcon = "↑";
        } else {
            statusLabel = "SETTLED";
            statusColor = Color.parseColor("#2196F3");
            statusIcon = "✓";
        }

        // Status Icon Circle
        int iconY = cardContentY + 240;
        iconBgPaint.setColor(statusColor);
        iconBgPaint.setAlpha(30);
        canvas.drawCircle(IMAGE_WIDTH / 2, iconY, 80, iconBgPaint);

        TextPaint iconPaint = createTextPaint(72, statusColor, true);
        drawCenteredText(canvas, statusIcon, IMAGE_WIDTH / 2, iconY + 25, iconPaint);

        // Status Label
        statusLabelPaint.setColor(statusColor);
        drawCenteredText(canvas, statusLabel, IMAGE_WIDTH / 2, iconY + 130, statusLabelPaint);

        // Amount with Currency
        amountPaint.setColor(statusColor);
        String balanceText = String.format(Locale.getDefault(), "₹ %,.2f", Math.abs(netBalance));
        drawCenteredText(canvas, balanceText, IMAGE_WIDTH / 2, iconY + 240, amountPaint);

        // Decorative Elements - Corner Accents
        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(statusColor);
        accentPaint.setAlpha(50);

        // Top-left accent
        Path topLeftPath = new Path();
        topLeftPath.moveTo(CARD_MARGIN, 270 + CORNER_RADIUS);
        topLeftPath.lineTo(CARD_MARGIN, 270 + CORNER_RADIUS + 150);
        topLeftPath.lineTo(CARD_MARGIN + 150, 270 + CORNER_RADIUS);
        topLeftPath.close();
        canvas.drawPath(topLeftPath, accentPaint);

        // Bottom-right accent
        Path bottomRightPath = new Path();
        bottomRightPath.moveTo(IMAGE_WIDTH - CARD_MARGIN, IMAGE_HEIGHT - 210 - CORNER_RADIUS);
        bottomRightPath.lineTo(IMAGE_WIDTH - CARD_MARGIN, IMAGE_HEIGHT - 210 - CORNER_RADIUS - 150);
        bottomRightPath.lineTo(IMAGE_WIDTH - CARD_MARGIN - 150, IMAGE_HEIGHT - 210 - CORNER_RADIUS);
        bottomRightPath.close();
        canvas.drawPath(bottomRightPath, accentPaint);

        // --- Footer Section ---
        int footerY = IMAGE_HEIGHT - 120;
        drawCenteredText(canvas, "Thank you for your business!", IMAGE_WIDTH / 2, footerY, footerPaint);
        drawCenteredText(canvas, "Powered by " + appName, IMAGE_WIDTH / 2, footerY + 55, footerBoldPaint);

        // Save and return
        return saveBitmapToFile(context, bitmap);
    }

    private static TextPaint createTextPaint(int size, int color, boolean isBold) {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(size);
        paint.setColor(color);
        if (isBold) {
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        paint.setTextAlign(Paint.Align.CENTER);
        return paint;
    }

    private static void drawCenteredText(Canvas canvas, String text, int centerX, int y, TextPaint paint) {
        canvas.drawText(text, centerX, y, paint);
    }

    private static int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= (1f - factor);
        return Color.HSVToColor(hsv);
    }

    private static File saveBitmapToFile(Context context, Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "payment_reminder_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos);
            }
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}