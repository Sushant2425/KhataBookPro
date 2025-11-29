package com.sandhyasofttechh.mykhatapro.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.activities.CollectionActivity;

public class CollectionReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "collection_reminders_channel";
    private static String name = "Customer";
    private double amount = 0.0;
    private String phone = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Agar boot complete hua ya app update hua → sirf CollectionActivity khol do (reminders wapas set ho jayenge)
        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action) ||
                "android.intent.action.MY_PACKAGE_REPLACED".equals(action)) {

            Intent launch = new Intent(context, CollectionActivity.class);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
            return;
        }

        // Normal reminder (Today / Incoming)
        name = intent.getStringExtra("name");
        amount = intent.getDoubleExtra("amount", 0.0);
        phone = intent.getStringExtra("phone");

        if (name == null) name = "Customer";
        if (phone == null) phone = "";

        createNotificationChannel(context);

        // Click karne par CollectionActivity khulega aur direct "Today" tab pe jayega
        Intent activityIntent = new Intent(context, CollectionActivity.class);
        activityIntent.putExtra("open_today_tab", true);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                phone.hashCode(),
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // ← apna icon laga dena
                .setContentTitle("Payment Reminder")
                .setContentText(String.format("%s कडे ₹%.0f घ्यायचे आहेत आज!", name, amount))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("%s\nरक्कम: ₹%.2f\nमोबाईल: %s", name, amount, phone)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.colorPrimary));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Har customer ke liye alag notification ID taaki ek dusre ko cancel na kare
        int notificationId = 1000 + Math.abs(phone.hashCode());
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "दैनिक वसुली स्मरण",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("आज आणि उद्या येणाऱ्या ग्राहकांची रोज सकाळी ९ वाजता आठवण");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}