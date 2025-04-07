package com.rexvit.browser.dialog;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.rexvit.browser.constant.SettingsKeys;


public class AppSettings {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private static AppSettings instance;


    public AppSettings(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
        this.editor = this.preferences.edit();

    }

    public static AppSettings getInstance(Context context) {
        if (instance == null) {
            instance = new AppSettings(context);
        }
        return instance;
    }


    public SharedPreferences getPreferences() {
        return this.preferences;
    }

    public String getString(@StringRes int resId) {
        return this.mContext.getString(resId);
    }

    public String getString(String resId) {
        return getString(resId);
    }


    public void setDefaultSettingsSharedPref() {
        this.editor.putBoolean(SettingsKeys.KEY_DOWNLOAD_VIA_WIFI_ONLY, false);
        this.editor.putBoolean(SettingsKeys.KEY_SMART_DOWNLOAD, false);
        this.editor.putBoolean(SettingsKeys.ENABLE_INCOGNITO_KEY, false);
        this.editor.putBoolean(SettingsKeys.KEY_AUTO_RESUME, true);
        this.editor.putBoolean(SettingsKeys.KEY_AUTO_URL_FILTER, false);
        this.editor.putBoolean(SettingsKeys.ENABLE_JAVA_SCRIPT_KEY, true);
        this.editor.putBoolean(SettingsKeys.ENABLE_IMAGE_LOADING_KEY, true);
        this.editor.putBoolean(SettingsKeys.KEY_CLIPBOARD_MONITOR, true);
        this.editor.putBoolean(SettingsKeys.KEY_PAUSE_AFTER_ERROR_SWITCH, true);
        this.editor.putBoolean(SettingsKeys.KEY_SINGLE_DOWNLOAD_PROGRESS, false);
        this.editor.putBoolean(SettingsKeys.KEY_FILE_CATALOG, true);
        this.editor.putInt(SettingsKeys.KEY_BUFFER, 2048);
        this.editor.putInt(SettingsKeys.KEY_NUMBER_OF_THREADS, 1);
        this.editor.putInt(SettingsKeys.KEY_MAX_DOWNLOADS, 2);
        this.editor.putInt(SettingsKeys.KEY_PAUSE_AFTER_ERROR_SWITCH, 1000);
        this.editor.putInt(SettingsKeys.KEY_MAX_ERROR_COUNT, 1000);
        this.editor.commit();
    }
}
