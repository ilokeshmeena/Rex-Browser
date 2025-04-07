package com.rexvit.browser.utils;

import androidx.annotation.NonNull;

import android.net.Uri;
import android.util.Patterns;
import android.webkit.URLUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UrlUtils {
    public static final String QUERY_PLACE_HOLDER = "%s";
    private static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)((?:http|https|file)://|(?:inline|data|about|javascript):|(?:.*:.*@))(.*)");
    private static final Pattern STRIP_URL_PATTERN = Pattern.compile("^http://(.*?)/?$");

    private static final String QUICKSEARCH_SOGO = "https://www.google.com/search?q=%s";
    static final Pattern ACCEPTED_URI_SCHEMA_DOWN = Pattern.compile("(?i)((?:http|https|file):\\/\\/|(?:inline|data|about|javascript):|(?:.*:.*@))(.*)");


    @NonNull
    public static String smartUrlFilter(@NonNull String str, boolean z, String str2) {
        String trim = str.trim();
        boolean z2 = trim.indexOf(32) != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(trim);
        if (matcher.matches()) {
            String group = matcher.group(1);
            String lowerCase = group.toLowerCase();
            if (!lowerCase.equals(group)) {
                trim = lowerCase + matcher.group(2);
            }
            return (z2 && Patterns.WEB_URL.matcher(trim).matches()) ? trim.replace(" ", "%20") : trim;
        } else if (z2 || !Patterns.WEB_URL.matcher(trim).matches()) {
            return z ? URLUtil.composeSearchUrl(trim, str2, QUERY_PLACE_HOLDER) : "";
        } else {
            return URLUtil.guessUrl(trim);
        }
    }




    private UrlUtils() {
    }

    public static String stripUrl(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = STRIP_URL_PATTERN.matcher(str);
        return matcher.matches() ? matcher.group(1) : str;
    }

    protected static String smartUrlFilter(Uri uri) {
        if (uri != null) {
            return smartUrlFilter(uri.toString());
        }
        return null;
    }

    public static String smartUrlFilter(String str) {
        return smartUrlFilter(str, true);
    }

    public static String smartUrlFilter(String str, boolean z) {
        String trim = str.trim();
        boolean z2 = trim.indexOf(32) != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(trim);
        if (matcher.matches()) {
            String group = matcher.group(1);
            String lowerCase = group.toLowerCase();
            if (!lowerCase.equals(group)) {
                trim = lowerCase + matcher.group(2);
            }
            return (z2 && Patterns.WEB_URL.matcher(trim).matches()) ? trim.replace(" ", "%20") : trim;
        } else if (z2 || !Patterns.WEB_URL.matcher(trim).matches()) {
            if (z) {
                return URLUtil.composeSearchUrl(trim, QUICKSEARCH_SOGO, QUERY_PLACE_HOLDER);
            }
            return null;
        } else {
            return URLUtil.guessUrl(trim);
        }
    }

    static String fixUrl(String str) {
        int indexOf = str.indexOf(58);
        boolean z = true;
        for (int i = 0; i < indexOf; i++) {
            char charAt = str.charAt(i);
            if (!Character.isLetter(charAt)) {
                break;
            }
            z &= Character.isLowerCase(charAt);
            if (i == indexOf - 1 && !z) {
                str = str.substring(0, indexOf).toLowerCase() + str.substring(indexOf);
            }
        }
        if (str.startsWith("http://") || str.startsWith("https://")) {
            return str;
        }
        if (str.startsWith("http:") || str.startsWith("https:")) {
            if (str.startsWith("http:/") || str.startsWith("https:/")) {
                return str.replaceFirst("/", "//");
            }
            return str.replaceFirst(":", "://");
        }
        return str;
    }

    static String filteredUrl(String str) {
        return (str == null || str.startsWith("content:") || str.startsWith("browser:")) ? "" : str;
    }
}
