package com.rexvit.browser.downloads;

import android.webkit.MimeTypeMap;
import java.net.URL;


public abstract class Tools {
    public static String getFileNameFromHeader(String str) {
        String str2;
        int lastIndexOf = str.toLowerCase().lastIndexOf("filename=");
        if (lastIndexOf >= 0) {
            str2 = str.substring(lastIndexOf + 9);
            int lastIndexOf2 = str2.lastIndexOf(";");
            if (lastIndexOf2 > 0) {
                str2 = str2.substring(0, lastIndexOf2 - 1);
            }
        } else {
            str2 = null;
        }
        return str2 != null ? str2.replaceAll("\"", "") : str2;
    }

    public static String getFileName(URL url) {
        String[] split = url.getPath().split("[\\\\/]");
        String str = "";
        if (split != null) {
            int length = split.length;
            for (int i = 0; i < split.length; i++) {
            }
            String[] split2 = split[length - 1].split("\\.");
            if (split2 == null || split2.length <= 1) {
                return "";
            }
            int length2 = split2.length;
            for (int i2 = 0; i2 < length2; i2++) {
                if (i2 < split2.length - 1) {
                    str = str + split2[i2];
                    if (i2 < length2 - 2) {
                        str = str + ".";
                    }
                }
            }
            return str + "." + split2[length2 - 1];
        }
        return "";
    }

    public static String getMimeType(String str) {
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(str);
        if (fileExtensionFromUrl != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
        }
        return null;
    }
}
