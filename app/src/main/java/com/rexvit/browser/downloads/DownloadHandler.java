package com.rexvit.browser.downloads;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.BuildConfig;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.rexvit.browser.R;
import com.rexvit.browser.activities.MainActivity;
import com.rexvit.browser.constant.Constants;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.utils.Utils;

import java.io.File;
import java.io.IOException;


public class DownloadHandler {
    private static final String COOKIE_REQUEST_HEADER = "Cookie";

    public static String encodePath(String str) {
        boolean z;
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (c == '[' || c == ']' || c == '|') {
                z = true;
                break;
            }
        }
        z = false;
        if (z) {
            StringBuilder sb = new StringBuilder();
            for (char c2 : charArray) {
                if (c2 == '[' || c2 == ']' || c2 == '|') {
                    sb.append('%');
                    sb.append(Integer.toHexString(c2));
                } else {
                    sb.append(c2);
                }
            }
            return sb.toString();
        }
        return str;
    }

    public static void onDownloadStart(@NonNull Activity activity, String str, String str2, @Nullable String str3, String str4, @NonNull String str5) {
        if (str3 == null || !str3.regionMatches(true, 0, "attachment", 0, 10)) {

            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.parse(str), str4);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.setComponent(null);
            if (Build.VERSION.SDK_INT >= 15) {
                intent.setSelector(null);
            }
            ResolveInfo resolveActivity = activity.getPackageManager().resolveActivity(intent, 65536);
            if (resolveActivity != null && (BuildConfig.APPLICATION_ID.equals(resolveActivity.activityInfo.packageName) || MainActivity.class.getName().equals(resolveActivity.activityInfo.name))) {
                try {
                    activity.startActivity(intent);
                    return;
                } catch (ActivityNotFoundException unused) {
                }
            }
        }
        onDownloadStartNoStream(activity, str, str2, str3, str4, str5);
    }

    private static void onDownloadStartNoStream(@NonNull final Activity activity, String str, String str2, String str3, @Nullable String str4, @NonNull String str5) {
        String string;
        int i;
        Uri parse = Uri.parse(str);
        String lastPathSegment = parse.getLastPathSegment();
        String externalStorageState = Environment.getExternalStorageState();
        if (!externalStorageState.equals("mounted")) {
            if (externalStorageState.equals("shared")) {
                string = activity.getString(R.string.download_sdcard_busy_dlg_msg);
                i = R.string.download_sdcard_busy_dlg_title;
            } else {
                string = activity.getString(R.string.download_no_sdcard_dlg_msg);
                i = R.string.download_no_sdcard_dlg_title;
            }
            new AlertDialog.Builder(activity).setTitle(i).setMessage(string).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).show();
            return;
        }
        String fileNameFromHeader = Tools.getFileNameFromHeader(str3);
        if (fileNameFromHeader != null) {
            lastPathSegment = fileNameFromHeader;
        }
        try {
            WebAddress webAddress = new WebAddress(str);
            webAddress.setPath(encodePath(webAddress.getPath()));
            String webAddress2 = webAddress.toString();
            try {
                final DownloadManager.Request request = new DownloadManager.Request(parse);
                String addNecessarySlashes = FileUtils.addNecessarySlashes(FileUtils.getDownloadDirectory());
                if (!isWriteAccessAvailable(Uri.parse(addNecessarySlashes))) {
                    Toast.makeText(activity, (int) R.string.cannot_download_loc, Toast.LENGTH_SHORT).show();
                    return;
                }
                request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(Utils.guessFileExtension(lastPathSegment)));
                request.setDestinationUri(Uri.parse(Constants.FILE + addNecessarySlashes + lastPathSegment));
                request.setVisibleInDownloadsUi(true);
                request.allowScanningByMediaScanner();
                request.setDescription(webAddress.getHost());
                String cookie = CookieManager.getInstance().getCookie(str);
                request.addRequestHeader(COOKIE_REQUEST_HEADER, cookie);
                request.setNotificationVisibility(1);
                if (str4 == null) {
                    if (TextUtils.isEmpty(webAddress2)) {
                        return;
                    }
                    new FetchUrlMimeType(activity, request, webAddress2, cookie, str2).start();
                    return;
                }
                String finalLastPathSegment = lastPathSegment;

                new AlertDialog.Builder(activity).setCancelable(false).setMessage("Continue download " + lastPathSegment + "?\nSize: " + str5).setPositiveButton(R.string.yes, (dialogInterface, i2) -> DownloadHandler.startDownloadBegin(activity, request, finalLastPathSegment)).setNegativeButton(R.string.no, (DialogInterface.OnClickListener) null).show();
            } catch (IllegalArgumentException unused) {
                Toast.makeText(activity, (int) R.string.only_http, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception unused2) {
            Toast.makeText(activity, (int) R.string.invalid_url, Toast.LENGTH_SHORT).show();
        }
    }

    public static void startDownloadBegin(@NonNull Activity activity, DownloadManager.Request request, String str) {
        try {
            ((DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
        } catch (IllegalArgumentException unused) {
            Toast.makeText(activity, (int) R.string.only_http, Toast.LENGTH_SHORT).show();
        } catch (SecurityException unused2) {
            Toast.makeText(activity, (int) R.string.cannot_download_loc, Toast.LENGTH_SHORT).show();
        }
        if (str.startsWith(".hosts.txt")) {
            return;
        }
        Toast.makeText(activity, activity.getString(R.string.starting_download) + " " + str, Toast.LENGTH_SHORT).show();
    }

    private static boolean isWriteAccessAvailable(@NonNull Uri uri) {
        File file = new File(uri.getPath());
        if (file.isDirectory() || file.mkdirs()) {
            try {
                if (file.createNewFile()) {
                    file.delete();
                    return true;
                }
                return true;
            } catch (IOException unused) {
                return false;
            }
        }
        return false;
    }





}
