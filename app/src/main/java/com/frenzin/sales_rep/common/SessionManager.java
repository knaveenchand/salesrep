package com.frenzin.sales_rep.common;

import android.content.Context;
import android.content.SharedPreferences;


public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "sales_rep";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ROLE = "role";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String id, String name, String role) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_ID, id);

        editor.putString(KEY_NAME, name);

        editor.putString(KEY_ROLE, role);
        // commit changes
        editor.commit();
    }


    public String getId() {
        return pref.getString(KEY_ID, null);
    }

    public String getName() {
        return pref.getString(KEY_NAME, null);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }


    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }


}
