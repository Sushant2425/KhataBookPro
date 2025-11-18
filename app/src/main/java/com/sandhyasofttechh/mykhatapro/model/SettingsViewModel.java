package com.sandhyasofttechh.mykhatapro.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsViewModel extends AndroidViewModel {
    private FirebaseAuth mAuth;

    public SettingsViewModel(@NonNull Application app) {
        super(app);
        mAuth = FirebaseAuth.getInstance();
    }


    public void triggerBackup() {
        String uid = mAuth.getCurrentUser().getUid();
        String time = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());
        FirebaseDatabase.getInstance().getReference(uid)
                .child("lastBackup").setValue(time);
    }
}