package com.rexvit.browser.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateUtils {
    public static String formatDateToHumanReadable(Long l) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(l.longValue()));
    }
}
