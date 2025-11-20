package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String KEY_APP_LOCK = "app_lock_enabled";

    private static final String PREF_NAME = "mykhata_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";

    // New keys for App Lock
    private static final String KEY_APP_LOCK_ENABLED = "app_lock_enabled";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save login state
    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Save user email
    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // App Lock getters/setters
    public void setAppLockEnabled(boolean enabled) {
        editor.putBoolean(KEY_APP_LOCK_ENABLED, enabled);
        editor.apply();
    }

//    public boolean isAppLockEnabled() {
//        return sharedPreferences.getBoolean(KEY_APP_LOCK_ENABLED, false);
//    }

    // Clear all preferences (on logout)
    public void clear() {
        editor.clear();
        editor.apply();
    }

    // Optional: Save other data, e.g., username, theme preference
    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
    public void setUserName(String name) { editor.putString("user_name", name).apply(); }
    public void setUserPhone(String phone) { editor.putString("user_phone", phone).apply(); }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }


    public void setAppLock(boolean enabled) {
        editor.putBoolean(KEY_APP_LOCK, enabled);
        editor.apply();
    }

    public boolean isAppLockEnabled() {
        return sharedPreferences.getBoolean(KEY_APP_LOCK, false);
    }


}
