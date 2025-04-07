package com.rexvit.browser.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.rexvit.browser.R;
import com.rexvit.browser.activities.MainActivity;
import com.rexvit.browser.constant.Constants;
import com.rexvit.browser.constant.Pref;
import com.rexvit.browser.database.HistoryItem;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public final class Utils {
    public static final String DIMONVIDEO = "dimonvideo.ru";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    public static String[] PattHtml = {".htm", ".html", ".shtm", ".xhtml", ".shtml", ".phtml", ".yhtml", ".php", ".mht", ".nhn", ".cms", ".cgi", ".mcgi", ".axdx", ".ashx", ".aspx", ".asp", ".ash", ".axd", ".gne", ".pwml", ".wiml", ".wml", ".jsf", ".jsp", ".js", ".do", ".pl"};
    public static final String UTF8 = "UTF-8";
    public static String[] PattDivid = {"\\?", "&", "#", ";", Pref.CLN, "="};
    public static final String N1 = "\n";
    public static final String R1 = "\r";

    public static String[] PattName = {"&filename=", "&title=", "&file=", "&name=", "&f="};
    public static String[] PattBig = {Pref.DIV, "|", "\\", "<", ">", "\"", R1, N1, "\t", Pref.CLN, ";", ",", "+", "=", "~", "*", "`", "'", "^", "!", "$", "&", "?", "{", "}"};

    public static int[] mSwipeColor = {Color.parseColor("#000000"), Color.parseColor("#5F9FFA"), Color.parseColor("#000000"), Color.parseColor("#5F9FFA")};

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private static DateFormat mDateFormat;
    private static DateFormat mTimeFormat;
    private static final NavigableMap<Long, String> suffixes = new TreeMap();

    static {
        suffixes.put(1000L, "k");
        suffixes.put(1000000L, "M");
        suffixes.put(1000000000L, "G");
        suffixes.put(1000000000000L, "T");
        suffixes.put(1000000000000000L, "P");
        suffixes.put(1000000000000000000L, "E");
        mDateFormat = DateFormat.getDateInstance(3);
        mTimeFormat = DateFormat.getTimeInstance(3);
    }


    public static String format(long j) {
        if (j == Long.MIN_VALUE) {
            return format(Long.MIN_VALUE + 1);
        }

        if (j < 0) {
            return "-" + format(-j);
        } else if (j < 1000) {
            return Long.toString(j);
        } else {
            Map.Entry<Long, String> floorEntry = suffixes.floorEntry(j);
            long divisor = floorEntry.getKey();
            String suffix = floorEntry.getValue();

            double scaledValue = (double) j / (divisor / 10.0);
            long truncatedValue = (long) scaledValue;
            boolean shouldRoundUp = Math.abs(truncatedValue - scaledValue) >= 0.5;

            if (shouldRoundUp) {
                truncatedValue++;
            }

            if (truncatedValue < 100) {
                return truncatedValue / 10.0 + suffix;
            } else {
                return truncatedValue / 10 + suffix;
            }
        }
    }

    @TargetApi(21)
    public static void chanceNotificationBarcolor(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 23) {
            activity.getWindow().getDecorView().setSystemUiVisibility(8192);
            window.setStatusBarColor(-1);
            activity.getWindow().setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK);
        }
    }

    public static boolean doesSupportHeaders() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static boolean isPanicTrigger(@Nullable Intent intent) {
        return intent != null && Constants.INTENT_PANIC_TRIGGER.equals(intent.getAction());
    }

    @Nullable
    public static String guessFileExtension(@NonNull String str) {
        int lastIndexOf = str.lastIndexOf(46) + 1;
        if (lastIndexOf <= 0 || str.length() <= lastIndexOf) {
            return null;
        }
        return str.substring(lastIndexOf);
    }

    public static final boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) == 4 || (context.getResources().getConfiguration().screenLayout & 15) == 3;
    }

    @NonNull
    public static Intent newEmailIntent(String str, String str2, String str3, String str4) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.EMAIL", new String[]{str});
        intent.putExtra("android.intent.extra.TEXT", str3);
        intent.putExtra("android.intent.extra.SUBJECT", str2);
        intent.putExtra("android.intent.extra.CC", str4);
        intent.setType("message/rfc822");
        return intent;
    }

    public static int dpToPx(float f) {
        return (int) ((f * Resources.getSystem().getDisplayMetrics().density) + 0.5f);
    }

    @Nullable
    public static String getDomainName(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        boolean startsWithHttps = str.startsWith(Constants.HTTPS);
        int indexOfSlash = str.indexOf('/', 8);
        if (indexOfSlash != -1) {
            str = str.substring(0, indexOfSlash);
        }

        try {
            URI uri = new URI(str);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                return str;
            }

            if (!startsWithHttps) {
                if (host.startsWith("www.")) {
                    return host.substring(4);
                } else {
                    return host;
                }
            } else {
                return Constants.HTTPS + host;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return str;
        }
    }
    public static Bitmap padFavicon(@NonNull Bitmap bitmap) {
        int dpToPx = dpToPx(4.0f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth() + dpToPx, bitmap.getHeight() + dpToPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        float f = dpToPx / 2;
        canvas.drawBitmap(bitmap, f, f, new Paint(2));
        return createBitmap;
    }

    @SuppressLint({"SimpleDateFormat"})
    public static File createImageFile() throws IOException {
        String format = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return File.createTempFile("JPEG_" + format + '_', ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
    }

    public static void close(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createShortcut(@NonNull Activity activity, @NonNull HistoryItem historyItem) {
        if (TextUtils.isEmpty(historyItem.getUrl())) {
            return;
        }
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setData(Uri.parse(historyItem.getUrl()));
        String string = TextUtils.isEmpty(historyItem.getTitle()) ? activity.getString(R.string.untitled) : historyItem.getTitle();
        Intent intent2 = new Intent();
        intent2.putExtra("android.intent.extra.shortcut.INTENT", intent);
        intent2.putExtra("android.intent.extra.shortcut.NAME", string);
        intent2.putExtra("android.intent.extra.shortcut.ICON", historyItem.getBitmap());
        intent2.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        activity.sendBroadcast(intent2);
        msg(activity.getString(R.string.message_added_to_homescreen), activity);
    }

    public static void msg(String str, Context context) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void msgLong(String str, Context context) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public static String urlWrapper(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith(Constants.HTTPS)) {
            String replace = "<font color='#4CAF50'>{content}</font>".replace("{content}", Constants.HTTPS);
            return replace + str.substring(8);
        } else if (str.startsWith("http://")) {
            String replace2 = "<font color='#9E9E9E'>{content}</font>".replace("{content}", "http://");
            return replace2 + str.substring(7);
        } else {
            return str;
        }
    }

    public static String getIconText(String str, String str2) {
        if (str.contains("https://www.")) {
            return Character.toString(str.charAt(12)).toUpperCase();
        }
        if (str.contains("http://www.")) {
            return Character.toString(str.charAt(11)).toUpperCase();
        }
        if (str.contains("http://m.")) {
            return Character.toString(str.charAt(9)).toUpperCase();
        }
        if (str.contains("https://m.")) {
            return Character.toString(str.charAt(10)).toUpperCase();
        }
        if (str2.isEmpty()) {
            return Character.toString(str.charAt(8)).toUpperCase();
        }
        return Character.toString(str2.charAt(0)).toUpperCase();
    }
    @NonNull
    public static String getIconTextM(String str, String str2) {
        if (str.contains("https://www.")) {
            return Character.toString(str.charAt(12)).toUpperCase();
        }
        if (str.contains("http://www.")) {
            return Character.toString(str.charAt(11)).toUpperCase();
        }
        if (str.contains("http://m.")) {
            return Character.toString(str.charAt(9)).toUpperCase();
        }
        if (str.contains("https://m.")) {
            return Character.toString(str.charAt(10)).toUpperCase();
        }
        if (str2.isEmpty()) {
            return Character.toString(str.charAt(8)).toUpperCase();
        }
        return Character.toString(str2.charAt(0)).toUpperCase();
    }

    public static String getDateString() {
        Date date = new Date();
        if (date.before(getStartOfToday())) {
            return mDateFormat.format(date);
        }
        return mTimeFormat.format(date);
    }

    private static Date getStartOfToday() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(11, 0);
        gregorianCalendar.set(12, 0);
        gregorianCalendar.set(13, 0);
        gregorianCalendar.set(14, 0);
        return gregorianCalendar.getTime();
    }

    public static String getDateTime() {
        return new SimpleDateFormat("MMM dd/yyyy", Locale.getDefault()).format(Long.valueOf(System.currentTimeMillis()));
    }

    public static String getTitleFromUrl(String str) {
        try {
            URL url = new URL(str);
            String host = url.getHost();

            if (host != null && !host.isEmpty()) {
                return url.getProtocol() + "://" + host;
            }

            if (str.startsWith("file:")) {
                String file = url.getFile();
                if (file != null && !file.isEmpty()) {
                    return file;
                }
            }

            return str;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return str;
        }
    }
    public static String getTitleForSearchBar(String str) {
        try {
            URL url = new URL(str);
            String host = url.getHost();

            if (host != null && !host.isEmpty()) {
                return host;
            }

            if (str.startsWith("file:")) {
                String file = url.getFile();
                if (file != null && !file.isEmpty()) {
                    return file;
                }
            }

            return str;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return str;
        }
    }
    public static int randomColors() {
        Random random = new Random();
        return Color.argb(150, random.nextInt(255), random.nextInt(255), random.nextInt(225));
    }

    public static String getName(String link) {
        int pos;
        String html = "";
        String name = "";
        String addr = decode(link);
        int pos2 = addr.indexOf("://");
        if (pos2 != -1 && pos2 < 9) {
            addr = addr.substring(pos2 + 3, addr.length());
        }
        ArrayList<String> texts = new ArrayList<>(Arrays.asList(addr.split(Pref.DIV)));
        if (texts.size() > 1) {
            texts.remove(0);
        }
        Collections.reverse(texts);
        Iterator<String> it = texts.iterator();
        while (it.hasNext()) {
            String text = it.next();
            if (text.length() != 0) {
                for (String divid : PattDivid) {
                    String[] split = text.split(divid);
                    for (String line : split) {
                        if (line.length() != 0) {
                            String line2 = line.trim();
                            if (getExtension(line2) != 0) {
                                int pos3 = line2.indexOf("=");
                                if (pos3 == -1 || pos3 >= line2.length() - 2) {
                                    name = line2;
                                } else {
                                    name = line2.substring(pos3 + 1, line2.length());
                                }
                            } else if (html.length() == 0 && isHtml(line2)) {
                                html = line2;
                            }
                        }
                    }
                }
            }
        }
        if (name.length() == 0) {
            String lower = addr.toLowerCase();
            for (String line3 : PattName) {
                int pos4 = lower.indexOf(line3);
                if (pos4 != -1) {
                    int len = line3.length();
                    int end = addr.indexOf("&", pos4 + len);
                    if (end == -1) {
                        end = addr.length();
                    }
                    if (pos4 + len < end - 2) {
                        name = addr.substring(pos4 + len, end).trim();
                        break;
                    }
                }
            }
        }
        if (name.length() == 0 || (!name.contains(Pref.POI) && !name.contains("-") && !name.contains("_") && !name.contains(Pref.SPA))) {
            if (html.length() == 0) {
                String temp = addr.substring(addr.lastIndexOf(Pref.DIV) + 1).trim();
                if (temp.length() == 0) {
                    return "";
                }
                int pos5 = temp.indexOf("?");
                if (pos5 == -1) {
                    name = temp;
                } else if (pos5 == 0 && temp.length() > 1) {
                    name = temp.substring(1, temp.length());
                } else if (pos5 == 0) {
                    return "";
                } else {
                    name = temp.substring(0, pos5);
                }
            } else {
                name = html;
            }
        }
        if (link.contains(DIMONVIDEO) && (pos = name.indexOf("_")) != -1 && pos < name.length() - 2) {
            try {
                Long.parseLong(name.substring(0, pos));
                name = name.substring(pos + 1, name.length()).trim();
            } catch (Throwable th) {
                
            }
        }
        return replaceBig(name);
    }
    public static int getExtension(String name) {
        int pos = name.lastIndexOf(Pref.POI);
        if (pos != -1 && pos < name.length() - 2) {
            String extension = name.substring(pos + 1);
            if (extension.matches("[a-zA-Z0-9]+")) {
                return extension.length();
            }
        }
        return 0;
    }
    public static String replaceBig(String name) {
        if (name.length() == 0) {
            return name;
        }
        for (String line : PattBig) {
            name = name.replace(line, "");
        }
        if (name.length() <= 96) {
            return name;
        }
        int pos = name.lastIndexOf(Pref.POI);
        if (pos == -1 || name.length() - pos >= 9) {
            return name.substring(0, 96);
        }
        return name.substring(0, 96 - (name.length() - pos)) + name.substring(pos);
    }
    public static boolean isHtml(String info) {
        for (String line : PattHtml) {
            if (info.endsWith(line)) {
                return true;
            }
        }
        return false;
    }

    public static String decode(String info) {
        try {
            info = URLDecoder.decode(URLDecoder.decode(info, UTF8), UTF8);
        } catch (Throwable th) {
        }
        return info.replace("%23", "#").replace("&#39;", "'").replace("&#039;", "'").replace("&quot;", "\"").replace("&amp;", "&");
    }

    public static int Extn(String name) {
        int pos = name.lastIndexOf(Pref.POI);
        if (pos != -1 && pos < name.length() - 2 && Pref.EXTS.size() > 3) {
            String ext = name.substring(pos + 1).toLowerCase();
            for (int ind = 0; ind < Pref.EXTS.size(); ind++) {
                if (Pref.EXTS.get(ind) != null) {
                    for (String line : Pref.EXTS.get(ind)) {
                        if (line.compareTo(ext) == 0) {
                            return ind + 1;
                        }
                    }
                    continue;
                }
            }
        }
        return 0;
    }
    public static boolean FindLink(String text) {
        return text.startsWith(HTTP) || text.startsWith(HTTPS) || (Build.VERSION.SDK_INT >= 8 && Patterns.WEB_URL.matcher(text).matches());
    }



    public static String fixLink(String link) {
        if (!Patterns.WEB_URL.matcher(link).matches()) {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "http://" + link;
            }
            if (link.endsWith(".com") || link.endsWith(".org") || link.endsWith(".net")) {
                link = "www." + link;
            }
        }
        return link;
    }
}
