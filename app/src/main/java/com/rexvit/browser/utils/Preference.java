package com.rexvit.browser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.rexvit.browser.constant.SettingsConstant;


public class Preference {
    public static synchronized void savePreferences(String str, boolean z, Context context) {
        synchronized (Preference.class) {
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            edit.putBoolean(str, z);
            edit.apply();
        }
    }

    private static synchronized SharedPreferences retainPreference(Context context) {
        SharedPreferences defaultSharedPreferences;
        synchronized (Preference.class) {
            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return defaultSharedPreferences;
    }

    public static boolean vpn(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.VPN, false);
    }

    public static boolean adBlock(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.ADBLOCK, true);
    }

    public static boolean one(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_ONE, true);
    }

    public static boolean two(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_TWO, false);
    }

    public static boolean three(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_THREE, false);
    }

    public static boolean four(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_FOUR, false);
    }

    public static boolean five(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_FIVE, false);
    }

    public static boolean six(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_SIX, false);
    }

    public static boolean seven(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_SEVEN, false);
    }
    public static boolean eight(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.FAB_VALUE_EIGHT, false);
    }

    public static boolean datSaveMode(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.SWITCH_IMAGES, true);
    }

    public static boolean desktopMode(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.DESKTOP, false);
    }

    public static boolean closeTabs(Context context) {
        return retainPreference(context).getBoolean(SettingsConstant.CLOSE_TABS, false);
    }
}
