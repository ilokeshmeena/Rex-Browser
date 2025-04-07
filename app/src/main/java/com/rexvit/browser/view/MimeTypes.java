package com.rexvit.browser.view;

import android.webkit.MimeTypeMap;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;


public final class MimeTypes {
    public static final String ALL_MIME_TYPES = "*/*";
    private static final HashMap<String, String> MIME_TYPES;

    static {
        HashMap<String, String> hashMap = new HashMap<>(89);
        MIME_TYPES = hashMap;
        hashMap.put("asm", "text/x-asm");
        hashMap.put("json", "application/json");
        hashMap.put("js", "application/javascript");
        hashMap.put("def", "text/plain");
        hashMap.put("in", "text/plain");
        hashMap.put("list", "text/plain");
        hashMap.put("log", "text/plain");
        hashMap.put("pl", "text/plain");
        hashMap.put("prop", "text/plain");
        hashMap.put("properties", "text/plain");
        hashMap.put("rc", "text/plain");
        hashMap.put("ini", "text/plain");
        hashMap.put("md", "text/markdown");
        hashMap.put("epub", "application/epub+zip");
        hashMap.put("ibooks", "application/x-ibooks+zip");
        hashMap.put("ifb", "text/calendar");
        hashMap.put("eml", "message/rfc822");
        hashMap.put(NotificationCompat.CATEGORY_MESSAGE, "application/vnd.ms-outlook");
        hashMap.put("ace", "application/x-ace-compressed");
        hashMap.put("bz", "application/x-bzip");
        hashMap.put("bz2", "application/x-bzip2");
        hashMap.put("cab", "application/vnd.ms-cab-compressed");
        hashMap.put("gz", "application/x-gzip");
        hashMap.put("lrf", "application/octet-stream");
        hashMap.put("jar", "application/java-archive");
        hashMap.put("xz", "application/x-xz");
        hashMap.put("Z", "application/x-compress");
        hashMap.put("bat", "application/x-msdownload");
        hashMap.put("ksh", "text/plain");
        hashMap.put("sh", "application/x-sh");
        hashMap.put("db", "application/octet-stream");
        hashMap.put("db3", "application/octet-stream");
        hashMap.put("otf", "application/x-font-otf");
        hashMap.put("ttf", "application/x-font-ttf");
        hashMap.put("psf", "application/x-font-linux-psf");
        hashMap.put("cgm", "image/cgm");
        hashMap.put("btif", "image/prs.btif");
        hashMap.put("dwg", "image/vnd.dwg");
        hashMap.put("dxf", "image/vnd.dxf");
        hashMap.put("fbs", "image/vnd.fastbidsheet");
        hashMap.put("fpx", "image/vnd.fpx");
        hashMap.put("fst", "image/vnd.fst");
        hashMap.put("mdi", "image/vnd.ms-mdi");
        hashMap.put("npx", "image/vnd.net-fpx");
        hashMap.put("xif", "image/vnd.xiff");
        hashMap.put("pct", "image/x-pict");
        hashMap.put("pic", "image/x-pict");
        hashMap.put("adp", "audio/adpcm");
        hashMap.put("au", "audio/basic");
        hashMap.put("snd", "audio/basic");
        hashMap.put("m2a", "audio/mpeg");
        hashMap.put("m3a", "audio/mpeg");
        hashMap.put("oga", "audio/ogg");
        hashMap.put("spx", "audio/ogg");
        hashMap.put("aac", "audio/x-aac");
        hashMap.put("mka", "audio/x-matroska");
        hashMap.put("jpgv", "video/jpeg");
        hashMap.put("jpgm", "video/jpm");
        hashMap.put("jpm", "video/jpm");
        hashMap.put("mj2", "video/mj2");
        hashMap.put("mjp2", "video/mj2");
        hashMap.put("mpa", "video/mpeg");
        hashMap.put("ogv", "video/ogg");
        hashMap.put("flv", "video/x-flv");
        hashMap.put("mkv", "video/x-matroska");
    }

    public static String getMimeType(File file) {
        String str;
        if (file.isDirectory()) {
            return null;
        }
        String extension = getExtension(file.getName());
        if (extension == null || extension.isEmpty()) {
            str = ALL_MIME_TYPES;
        } else {
            String lowerCase = extension.toLowerCase(Locale.getDefault());
            str = MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowerCase);
            if (str == null) {
                str = MIME_TYPES.get(lowerCase);
            }
        }
        return str == null ? ALL_MIME_TYPES : str;
    }

    public static boolean mimeTypeMatch(String str, String str2) {
        return Pattern.matches(str.replace("*", ".*"), str2);
    }

    public static String getExtension(String str) {
        return str.contains(".") ? str.substring(str.lastIndexOf(".") + 1).toLowerCase() : "";
    }
}
