package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.sandhyasofttechh.mykhatapro.R;
import com.sandhyasofttechh.mykhatapro.fragments.SimSelectionBottomSheet;

public class SmsSettingsActivity extends AppCompatActivity implements SimSelectionBottomSheet.SimSelectionListener {

    private MaterialToolbar toolbar;
    private TextView tvSelectedSim;
    private TextInputEditText inputPhoneNumber;
    private SwitchMaterial switchSmsNotifications;
    private TextView tvSmsSample;
    private Button btnSaveSms;

    private int selectedSubscriptionId = -1;
    private String selectedSimName = "";
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "sms_settings_prefs";
    private static final String KEY_SIM_ID = "selected_sim_id";
    private static final String KEY_SIM_NAME = "selected_sim_name";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_SMS_ENABLED = "sms_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        toolbar = findViewById(R.id.toolbar);
        tvSelectedSim = findViewById(R.id.tv_selected_sim);
        inputPhoneNumber = findViewById(R.id.input_phone_number);
        switchSmsNotifications = findViewById(R.id.switch_sms_notifications);
        tvSmsSample = findViewById(R.id.tv_sms_sample);
        btnSaveSms = findViewById(R.id.btn_save_sms);

        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.sim_selection_area).setOnClickListener(v -> {
            if (checkPermission()) {
                openSimSelectionSheet();
            } else {
                requestReadPhoneStatePermission();
            }
        });

        btnSaveSms.setOnClickListener(v -> saveSettings());

        loadSavedPreferences();
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadPhoneStatePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }

    private void openSimSelectionSheet() {
        SimSelectionBottomSheet sheet = new SimSelectionBottomSheet();
        sheet.show(getSupportFragmentManager(), "sim_selection");
    }

    @Override
    public void onSimSelected(int subscriptionId, String simName) {
        selectedSubscriptionId = subscriptionId;
        selectedSimName = simName;
        tvSelectedSim.setText(simName);
    }

    private void saveSettings() {
        String phoneNumber = inputPhoneNumber.getText() != null ? inputPhoneNumber.getText().toString().trim() : "";
        boolean smsEnabled = switchSmsNotifications.isChecked();

        if (selectedSubscriptionId == -1) {
            Toast.makeText(this, "Please select a SIM", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phoneNumber.isEmpty()) {
            inputPhoneNumber.setError("Please enter phone number");
            inputPhoneNumber.requestFocus();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SIM_ID, selectedSubscriptionId);
        editor.putString(KEY_SIM_NAME, selectedSimName);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.putBoolean(KEY_SMS_ENABLED, smsEnabled);
        editor.apply();

        Toast.makeText(this, "SMS settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadSavedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedSubscriptionId = prefs.getInt(KEY_SIM_ID, -1);
        selectedSimName = prefs.getString(KEY_SIM_NAME, "");
        String phoneNumber = prefs.getString(KEY_PHONE_NUMBER, "");
        boolean smsEnabled = prefs.getBoolean(KEY_SMS_ENABLED, false);

        if (!selectedSimName.isEmpty()) {
            tvSelectedSim.setText(selectedSimName);
        } else {
            tvSelectedSim.setText("Select a SIM");
        }
        inputPhoneNumber.setText(phoneNumber);
        switchSmsNotifications.setChecked(smsEnabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                openSimSelectionSheet();
            } else {
                Toast.makeText(this, "Permission denied to read phone state.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
