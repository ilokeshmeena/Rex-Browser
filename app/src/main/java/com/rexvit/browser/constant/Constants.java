package com.rexvit.browser.constant;

import android.os.Environment;


public final class Constants {
    public static final String ABOUT = "about:";
    public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    public static final String DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    public static final String FILE = "file://";
    public static final String HTTPS = "https://";
    public static final String INTENT_ORIGIN = "URL_INTENT_ORIGIN";
    public static final String INTENT_PANIC_TRIGGER = "info.guardianproject.panic.action.TRIGGER";
    public static final String JAVASCRIPT_TEXT_REFLOW = "javascript:document.getElementsByTagName('body')[0].style.width=window.innerWidth+'px';";
    public static final String SUFFIX_PNG = ".png";
    public static final String TAG = "privateprowserpro";

    private Constants() {
    }
}
