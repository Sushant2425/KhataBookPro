package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.sandhyasofttechh.mykhatapro.workers.ReminderWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderHelper {

    private static final String MSG91_AUTH_KEY = "YOUR_MSG91_AUTH_KEY"; // CHANGE THIS
    private static String SENDER_ID = "SANDHYA";


    public static void updateSenderId(String sender) {
        SENDER_ID = sender;
    }

    public static void scheduleDailyReminder(Context context) {
        long delay = calculateDelayTo10AM();

        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(ReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "auto_reminder_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                work
        );

        Toast.makeText(context, "Auto Reminder ON (Daily 10 AM)", Toast.LENGTH_SHORT).show();
    }

    public static void cancelReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork("auto_reminder_work");
        Toast.makeText(context, "Auto Reminder OFF", Toast.LENGTH_SHORT).show();
    }

    private static long calculateDelayTo10AM() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTimeInMillis() - System.currentTimeMillis();
    }

    public static void sendReminderSms(String phone, String name, double balance) {
        String message = "Hi " + name + ", your balance is ₹" + (int)balance + ". Please clear soon. - " + SENDER_ID;
        String encodedMsg = Uri.encode(message);
        String url = "https://api.msg91.com/api/sendhttp.php?" +
                "mobiles=" + phone +
                "&authkey=" + MSG91_AUTH_KEY +
                "&route=4" +
                "&sender=" + SENDER_ID +
                "&message=" + encodedMsg;

        new Thread(() -> {
            try {
                java.net.URL obj = new java.net.URL(url);
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.getResponseCode(); // Trigger request
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}