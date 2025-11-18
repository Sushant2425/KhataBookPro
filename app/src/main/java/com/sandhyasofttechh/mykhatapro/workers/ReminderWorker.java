package com.sandhyasofttechh.mykhatapro.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.sandhyasofttechh.mykhatapro.utils.ReminderHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }


    @NonNull
    @Override
    public Result doWork() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(uid);

        rootRef.child("autoReminder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Boolean enabled = snap.getValue(Boolean.class);
                if (enabled != null && enabled) {
                    loadSenderAndCheck(rootRef);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return Result.success();
    }

    private void loadSenderAndCheck(DatabaseReference rootRef) {
        rootRef.child("smsSender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String sender = snap.getValue(String.class);
                if (sender != null && sender.length() >= 3 && sender.length() <= 6) {
                    ReminderHelper.updateSenderId(sender);
                }
                checkCustomers(rootRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkCustomers(DatabaseReference rootRef) {
        rootRef.child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot customers) {
                for (DataSnapshot cust : customers.getChildren()) {
                    Double balance = cust.child("balance").getValue(Double.class);
                    String lastSent = cust.child("lastReminderSent").getValue(String.class);
                    String name = cust.child("name").getValue(String.class);
                    String phone = cust.getKey();

                    if (balance != null && balance > 0 && shouldSendReminder(lastSent)) {
                        String customerName = name != null ? name : "Customer";
                        ReminderHelper.sendReminderSms(phone, customerName, balance);

                        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault()).format(new Date());
                        cust.getRef().child("lastReminderSent").setValue(now);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean shouldSendReminder(String lastSent) {
        if (lastSent == null) return true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault());
            Date last = sdf.parse(lastSent);
            Date now = new Date();
            long diff = now.getTime() - last.getTime();
            return diff >= 7L * 24 * 60 * 60 * 1000; // 7 days
        } catch (Exception e) {
            return true;
        }
    }
}