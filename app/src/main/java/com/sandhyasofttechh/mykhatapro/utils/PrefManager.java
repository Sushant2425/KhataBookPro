package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "MyKhataPro";

    // User Authentication Keys
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";

    // Profile Keys
    private static final String KEY_BUSINESS_NAME = "business_name";
    private static final String KEY_LOGO_URL = "logo_url";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_GSTIN = "gstin";

    // App Settings Keys
    private static final String KEY_APP_LOCK_ENABLED = "app_lock_enabled";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public PrefManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // ==================== LOGIN METHODS ====================

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // ==================== USER EMAIL ====================

    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    // ==================== USER ID ====================

    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    // ==================== BUSINESS NAME ====================

    public void saveBusinessName(String businessName) {
        editor.putString(KEY_BUSINESS_NAME, businessName);
        editor.apply();
    }

    public String getBusinessName() {
        return pref.getString(KEY_BUSINESS_NAME, "");
    }

    // ==================== LOGO URL ====================

    public void saveLogoUrl(String logoUrl) {
        editor.putString(KEY_LOGO_URL, logoUrl);
        editor.apply();
    }

    public String getLogoUrl() {
        return pref.getString(KEY_LOGO_URL, "");
    }

    // ==================== USER NAME ====================

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public void saveUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    // ==================== USER PHONE ====================

    public void setUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    public void saveMobile(String mobile) {
        editor.putString(KEY_MOBILE, mobile);
        editor.apply();
    }

    public String getMobile() {
        return pref.getString(KEY_MOBILE, "");
    }

    // ==================== ADDRESS ====================

    public void saveAddress(String address) {
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    public String getAddress() {
        return pref.getString(KEY_ADDRESS, "");
    }

    // ==================== GSTIN ====================

    public void saveGstin(String gstin) {
        editor.putString(KEY_GSTIN, gstin);
        editor.apply();
    }

    public String getGstin() {
        return pref.getString(KEY_GSTIN, "");
    }

    // ==================== APP LOCK ====================

    public void setAppLockEnabled(boolean enabled) {
        editor.putBoolean(KEY_APP_LOCK_ENABLED, enabled);
        editor.apply();
    }

    public void setAppLock(boolean enabled) {
        editor.putBoolean(KEY_APP_LOCK_ENABLED, enabled);
        editor.apply();
    }

    public boolean isAppLockEnabled() {
        return pref.getBoolean(KEY_APP_LOCK_ENABLED, false);
    }

    // ==================== SAVE COMPLETE PROFILE ====================

    public void saveCompleteProfile(String email, String businessName, String logoUrl,
                                    String userName, String mobile, String address,
                                    String gstin, String userId) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_BUSINESS_NAME, businessName);
        editor.putString(KEY_LOGO_URL, logoUrl);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_GSTIN, gstin);
        editor.putString(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // ==================== GENERIC METHODS ====================

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return pref.getString(key, "");
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    // ==================== CLEAR METHODS ====================

    // Clear all data (for logout)
    public void clear() {
        editor.clear();
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // Clear specific profile data
    public void clearProfileData() {
        editor.remove(KEY_BUSINESS_NAME);
        editor.remove(KEY_LOGO_URL);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_MOBILE);
        editor.remove(KEY_ADDRESS);
        editor.remove(KEY_GSTIN);
        editor.apply();
    }

    public void clearAll() {

    }
}