package com.safepayu.wallet.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.safepayu.wallet.R;


public class U {

    public static void setTheme(Context context, int theme) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(context.getString(R.string.prefs_theme_key), theme).apply();
    }

    public static int getTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.prefs_theme_key), -1);
    }
}
