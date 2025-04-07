package com.rexvit.browser.utils;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebIconDatabase;
import android.webkit.WebStorage;
import android.webkit.WebViewDatabase;


public class ClearingData {
    public static void removePasswords(Context context) {
        WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(context);
        webViewDatabase.clearFormData();
        webViewDatabase.clearHttpAuthUsernamePassword();
        if (Build.VERSION.SDK_INT < 18) {
            webViewDatabase.clearUsernamePassword();
            WebIconDatabase.getInstance().removeAllIcons();
        }
    }

    public static void removeWebStorage() {
        WebStorage.getInstance().deleteAllData();
    }

    public static void removeCookies(@NonNull Context context) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= 21) {
            cookieManager.removeAllCookies(null);
            return;
        }
        CookieSyncManager.createInstance(context);
        cookieManager.removeAllCookie();
    }

    public static void clearCache(Context context) {
        context.deleteDatabase("webview.db");
        context.deleteDatabase("webViewCache.db");
    }
}
