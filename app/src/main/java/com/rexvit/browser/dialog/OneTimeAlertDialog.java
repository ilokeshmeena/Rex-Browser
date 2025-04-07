package com.rexvit.browser.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;


public class OneTimeAlertDialog extends AlertDialog {
    private String mPrefsKey;

    private OneTimeAlertDialog(Context context) {
        super(context);
    }

    private OneTimeAlertDialog(Context context, int i) {
        super(context, i);
    }

    private OneTimeAlertDialog(Context context, boolean z, DialogInterface.OnCancelListener onCancelListener) {
        super(context, z, onCancelListener);
    }

    protected OneTimeAlertDialog(Context context, String str) {
        super(context);
        this.mPrefsKey = str;
    }

    protected OneTimeAlertDialog(Context context, int i, String str) {
        super(context, i);
        this.mPrefsKey = str;
    }

    protected OneTimeAlertDialog(Context context, boolean z, DialogInterface.OnCancelListener onCancelListener, String str) {
        super(context, z, onCancelListener);
        this.mPrefsKey = str;
    }

    @Override 
    public void show() {
        if (isKeyInPrefs(getContext(), this.mPrefsKey)) {
            return;
        }
        super.show();
        markShown();
    }

    public void markShown() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(this.mPrefsKey, true).apply();
    }

    public void markNotShown() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(this.mPrefsKey, false).apply();
    }

    
    public static boolean isKeyInPrefs(Context context, String str) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(str, false);
    }

    
    public static class Builder extends AlertDialog.Builder {
        private String prefsKey;

        private Builder(Context context) {
            super(context);
        }

        private Builder(Context context, int i) {
            super(context, i);
        }

        public Builder(Context context, String str) {
            super(context);
            this.prefsKey = str;
        }

        public Builder(Context context, int i, String str) {
            super(context, i);
            this.prefsKey = str;
        }

        @Override 
        @Nullable
        public AlertDialog show() {
            if (OneTimeAlertDialog.isKeyInPrefs(getContext(), this.prefsKey)) {
                return null;
            }
            super.show();
            markShown();
            return null;
        }

        private void markShown() {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(this.prefsKey, true).apply();
        }
    }
}
