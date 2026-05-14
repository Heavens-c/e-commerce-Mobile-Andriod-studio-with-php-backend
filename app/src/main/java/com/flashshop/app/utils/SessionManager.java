package com.flashshop.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.flashshop.app.models.AdminBrief;
import com.flashshop.app.models.User;
import com.google.gson.Gson;

public class SessionManager {

    public static final String ACCOUNT_USER = "user";
    public static final String ACCOUNT_ADMIN = "admin";

    private static final String PREF_NAME = "FlashShopSession";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "user_data";
    private static final String KEY_ADMIN = "admin_data";
    private static final String KEY_ACCOUNT_TYPE = "account_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    /** @deprecated use {@link #saveUserSession(String, User)} */
    public void saveLogin(String token, User user) {
        saveUserSession(token, user);
    }

    public void saveUserSession(String token, User user) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ACCOUNT_TYPE, ACCOUNT_USER);
        editor.remove(KEY_ADMIN);
        editor.apply();
    }

    public void saveAdminSession(String token, AdminBrief admin) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ADMIN, gson.toJson(admin));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ACCOUNT_TYPE, ACCOUNT_ADMIN);
        editor.remove(KEY_USER);
        editor.apply();
    }

    /** Customer profile; null for admin-only sessions. */
    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return gson.fromJson(json, User.class);
    }

    public AdminBrief getAdmin() {
        String json = prefs.getString(KEY_ADMIN, null);
        if (json == null) return null;
        return gson.fromJson(json, AdminBrief.class);
    }

    public String getAccountType() {
        String t = prefs.getString(KEY_ACCOUNT_TYPE, null);
        if (t != null) return t;
        // Legacy installs: infer from stored profile
        if (prefs.getString(KEY_USER, null) != null) return ACCOUNT_USER;
        if (prefs.getString(KEY_ADMIN, null) != null) return ACCOUNT_ADMIN;
        return ACCOUNT_USER;
    }

    public boolean isAdminAccount() {
        return ACCOUNT_ADMIN.equals(getAccountType());
    }

    public String getToken() {
        return "Bearer " + prefs.getString(KEY_TOKEN, "");
    }

    public String getRawToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void updateUser(User user) {
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }
}
