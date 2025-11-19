package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppLockPref {

    private static final String PREF = "AppLock";
    private static final String KEY_PATTERN = "pattern";
    private static final String KEY_PIN = "pin";

    public static void savePattern(Context context, String pattern) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PATTERN, pattern).apply();
    }

    public static String getPattern(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY_PATTERN, "");
    }

    public static void savePin(Context context, String pin) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PIN, pin).apply();
    }

    public static String getPin(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY_PIN, "");
    }
}
