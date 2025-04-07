package com.rexvit.browser.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;

import com.rexvit.browser.R;

import java.io.File;
import java.util.HashMap;


public class Icons {

    private static HashMap<String, Integer> sMimeIconIds = new HashMap<>(153);
    private static SparseArray<Bitmap> sMimeIcons = new SparseArray<>();

    static {
        add((int) R.drawable.ic_doc_apk, "application/vnd.android.package-archive");
        add((int) R.drawable.ic_doc_audio_am, "application/ogg", "application/x-flac", "audio/mpeg", "audio/mp4", "audio/x-wav", "audio/amr", "audio/amr-wb", "audio/x-ms-wma", "audio/ogg", "audio/aac", "audio/aac-adts", "audio/x-matroska", "audio/sp-midi", "audio/imelody", "audio/midi");
        add((int) R.drawable.ic_doc_certificate, "application/pgp-keys", "application/pgp-signature", "application/x-pkcs12", "application/x-pkcs7-certreqresp", "application/x-pkcs7-crl", "application/x-x509-ca-cert", "application/x-x509-user-cert", "application/x-pkcs7-certificates", "application/x-pkcs7-mime", "application/x-pkcs7-signature");
        add((int) R.drawable.ic_doc_codes, "application/rdf+xml", "application/rss+xml", "application/x-object", "application/xhtml+xml", "text/css", "text/html", "text/xml", "text/x-c++hdr", "text/x-c++src", "text/x-chdr", "text/x-csrc", "text/x-dsrc", "text/x-csh", "text/x-haskell", "text/x-java", "text/x-literate-haskell", "text/x-pascal", "text/x-tcl", "text/x-tex", "application/x-latex", "application/x-texinfo", "application/atom+xml", "application/ecmascript", "application/json", "application/javascript", "application/xml", "text/javascript", "application/x-javascript");
        add((int) R.drawable.ic_zip_box_white_36dp, "application/mac-binhex40", "application/rar", "application/zip", "application/java-archive", "application/x-apple-diskimage", "application/x-debian-package", "application/x-gtar", "application/x-iso9660-image", "application/x-lha", "application/x-lzh", "application/x-lzx", "application/x-stuffit", "application/x-tar", "application/x-webarchive", "application/x-webarchive-xml", "application/gzip", "application/x-7z-compressed", "application/x-deb", "application/x-rar-compressed");
        add((int) R.drawable.ic_doc_contact_am, "text/x-vcard", "text/vcard");
        add((int) R.drawable.ic_doc_event_am, "text/calendar", "text/x-vcalendar");
        add((int) R.drawable.ic_doc_font, "application/x-font", "application/font-woff", "application/x-font-woff", "application/x-font-ttf");
        add((int) R.drawable.ic_doc_image, "application/vnd.oasis.opendocument.graphics", "application/vnd.oasis.opendocument.graphics-template", "application/vnd.oasis.opendocument.image", "application/vnd.stardivision.draw", "application/vnd.sun.xml.draw", "application/vnd.sun.xml.draw.template", "image/jpeg", "image/png", "image/svg+xml");
        add((int) R.drawable.ic_doc_pdf, "application/pdf");
        add((int) R.drawable.ic_doc_presentation, "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.openxmlformats-officedocument.presentationml.template", "application/vnd.openxmlformats-officedocument.presentationml.slideshow", "application/vnd.stardivision.impress", "application/vnd.sun.xml.impress", "application/vnd.sun.xml.impress.template", "application/x-kpresenter", "application/vnd.oasis.opendocument.presentation");
        add((int) R.drawable.ic_doc_spreadsheet_am, "application/vnd.oasis.opendocument.spreadsheet", "application/vnd.oasis.opendocument.spreadsheet-template", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.template", "application/vnd.stardivision.calc", "application/vnd.sun.xml.calc", "application/vnd.sun.xml.calc.template", "application/x-kspread", "text/comma-separated-values");
        add((int) R.drawable.ic_doc_doc_am, "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.openxmlformats-officedocument.wordprocessingml.template", "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.text-master", "application/vnd.oasis.opendocument.text-template", "application/vnd.oasis.opendocument.text-web", "application/vnd.stardivision.writer", "application/vnd.stardivision.writer-global", "application/vnd.sun.xml.writer", "application/vnd.sun.xml.writer.global", "application/vnd.sun.xml.writer.template", "application/x-abiword", "application/x-kword", "text/markdown");
        add((int) R.drawable.ic_doc_text_am, "text/plain");
        add((int) R.drawable.ic_doc_video_am, "application/x-quicktimeplayer", "application/x-shockwave-flash", "video/mpeg", "video/mp4", "video/3gpp", "video/3gpp2", "video/x-matroska", "video/webm", "video/mp2ts", "video/avi", "video/x-ms-wmv", "video/x-ms-asf");
    }

    private static void add(String str, int i) {
        if (sMimeIconIds.put(str, Integer.valueOf(i)) == null) {
            return;
        }
        throw new RuntimeException(str + " already registered!");
    }

    private static void add(int i, String... strArr) {
        for (String str : strArr) {
            add(str, i);
        }
    }

    public static Integer getIconDrawableId(String str) {
        Integer num = sMimeIconIds.get(MimeTypes.getMimeType(new File(str)));
        return num != null ? num : Integer.valueOf((int) R.drawable.ic_doc_unknown);
    }

    public static boolean isText(String str) {
        String mimeType = MimeTypes.getMimeType(new File(str));
        Integer num = sMimeIconIds.get(mimeType);
        if (num == null || num.intValue() != R.drawable.ic_doc_text_am) {
            if (mimeType == null || !mimeType.contains("/")) {
                return false;
            }
            return "text".equals(mimeType.split("/")[0]);
        }
        return true;
    }

    public static boolean isVideo(String str) {
        String mimeType = MimeTypes.getMimeType(new File(str));
        Integer num = sMimeIconIds.get(mimeType);
        if (num == null || num.intValue() != R.drawable.ic_doc_video_am) {
            return mimeType != null && mimeType.contains("/") && "video".equals(mimeType.split("/")[0]);
        }
        return true;
    }

    public static boolean isAudio(String str) {
        String mimeType = MimeTypes.getMimeType(new File(str));
        Integer num = sMimeIconIds.get(mimeType);
        if (num == null || num.intValue() != R.drawable.ic_doc_audio_am) {
            return mimeType != null && mimeType.contains("/") && "audio".equals(mimeType.split("/")[0]);
        }
        return true;
    }

    public static boolean isCode(String str) {
        Integer num = sMimeIconIds.get(MimeTypes.getMimeType(new File(str)));
        return num != null && num.intValue() == R.drawable.ic_doc_codes;
    }

    public static boolean isArchive(String str) {
        Integer num = sMimeIconIds.get(MimeTypes.getMimeType(new File(str)));
        return num != null && num.intValue() == R.drawable.ic_zip_box_white_36dp;
    }

    public static boolean isApk(String str) {
        Integer num = sMimeIconIds.get(MimeTypes.getMimeType(new File(str)));
        return num != null && num.intValue() == R.drawable.ic_doc_apk;
    }

    public static boolean isPdf(String str) {
        Integer num = sMimeIconIds.get(MimeTypes.getMimeType(new File(str)));
        return num != null && num.intValue() == R.drawable.ic_doc_pdf;
    }

    public static boolean isPicture(String str) {
        Integer num = sMimeIconIds.get(MimeTypes.getMimeType(new File(str)));
        return num != null && num.intValue() == R.drawable.ic_doc_image;
    }

    public static boolean isGeneric(String str) {
        String mimeType = MimeTypes.getMimeType(new File(str));
        return mimeType == null || sMimeIconIds.get(mimeType) == null;
    }

    public static int getTypeOfFile(String str) {
        if (isVideo(str)) {
            return 0;
        }
        if (isAudio(str)) {
            return 1;
        }
        if (isPdf(str)) {
            return 2;
        }
        if (isCode(str)) {
            return 3;
        }
        if (isText(str)) {
            return 4;
        }
        if (isArchive(str)) {
            return 5;
        }
        if (isGeneric(str)) {
            return 6;
        }
        if (isApk(str)) {
            return 7;
        }
        return isPicture(str) ? 8 : -1;
    }

    public static BitmapDrawable loadMimeIcon(String str, boolean z, Resources resources) {
        String mimeType = MimeTypes.getMimeType(new File(str));
        if (mimeType == null) {
            return loadBitmapDrawableById(resources, R.drawable.ic_doc_generic_am);
        }
        Integer num = sMimeIconIds.get(mimeType);
        if (num != null) {
            int intValue = num.intValue();
            if (intValue != R.drawable.ic_doc_apk) {
                if (intValue == R.drawable.ic_doc_image && z) {
                    num = Integer.valueOf((int) R.drawable.ic_doc_image_grid);
                }
            } else if (z) {
                num = Integer.valueOf((int) R.drawable.ic_doc_apk_grid);
            }
            return loadBitmapDrawableById(resources, num.intValue());
        }
        String str2 = mimeType.split("/")[0];
        if ("audio".equals(str2)) {
            num = Integer.valueOf((int) R.drawable.ic_doc_audio_am);
        } else if ("image".equals(str2)) {
            if (z) {
                num = Integer.valueOf((int) R.drawable.ic_doc_image_grid);
            } else {
                num = Integer.valueOf((int) R.drawable.ic_doc_image);
            }
        } else if ("text".equals(str2)) {
            num = Integer.valueOf((int) R.drawable.ic_doc_text_am);
        } else if ("video".equals(str2)) {
            num = Integer.valueOf((int) R.drawable.ic_doc_video_am);
        }
        if (num == null) {
            num = Integer.valueOf((int) R.drawable.ic_doc_generic_am);
        }
        return loadBitmapDrawableById(resources, num.intValue());
    }

    private static BitmapDrawable loadBitmapDrawableById(Resources resources, int i) {
        Bitmap bitmap = sMimeIcons.get(i);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, i);
            sMimeIcons.put(i, bitmap);
        }
        return new BitmapDrawable(resources, bitmap);
    }
}
