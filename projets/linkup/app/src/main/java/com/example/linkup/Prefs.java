package com.example.linkup;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREF_NAME = "USER_DATA";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";

    public static void saveUser(Context context, String name, String email) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public static String getName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_NAME, null);
    }

    public static String getEmail(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_EMAIL, null);
    }

    public static boolean isComplete(Context context) {
        return getName(context) != null && getEmail(context) != null;
    }
}
