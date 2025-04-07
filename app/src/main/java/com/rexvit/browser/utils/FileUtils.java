package com.rexvit.browser.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.rexvit.browser.R;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.constant.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.rexvit.browser.view.MimeTypes;
import com.tonyodev.fetch2.Download;


public class FileUtils {

    public static void writeBundleToStorage(@NonNull final Application application, final Bundle bundle, @NonNull final String str) {
        BrowserApp.getIOThread().execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File(application.getFilesDir(), str));
                    Parcel obtain = Parcel.obtain();
                    obtain.writeBundle(bundle);
                    fileOutputStream.write(obtain.marshall());
                    fileOutputStream.flush();
                    obtain.recycle();
                    Utils.close(fileOutputStream);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Unable to write bundle to storage", e);
                    Utils.close(fileOutputStream);
                } finally {
                    Utils.close(fileOutputStream);
                }
            }
        });
    }
    public static void deleteBundleInStorage(@NonNull Application application, @NonNull String str) {
        File file = new File(application.getFilesDir(), str);
        if (file.exists()) {
            file.delete();
        }
    }



    @Nullable
    public static Bundle readBundleFromStorage(@NonNull Application application, @NonNull String str) {
        FileInputStream fileInputStream = null;
        File file = new File(application.getFilesDir(), str);
        try {
            fileInputStream = new FileInputStream(file);
            Parcel obtain = Parcel.obtain();
            byte[] bArr = new byte[(int) file.length()];
            fileInputStream.read(bArr, 0, bArr.length);
            obtain.unmarshall(bArr, 0, bArr.length);
            obtain.setDataPosition(0);
            Bundle readBundle = obtain.readBundle(ClassLoader.getSystemClassLoader());
            obtain.recycle();
            file.delete();
            Utils.close(fileInputStream);
            return readBundle;
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Unable to read bundle from storage", e);
            file.delete();
            Utils.close(fileInputStream);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error reading bundle from storage", e);
            file.delete();
            Utils.close(fileInputStream);
        } finally {
            Utils.close(fileInputStream);
        }
        return null;
    }
    @NonNull
    public static String addNecessarySlashes(@Nullable String str) {
        if (str == null || str.length() == 0) {
            return "/";
        }
        if (str.charAt(str.length() - 1) != '/') {
            str = str + '/';
        }
        if (str.charAt(0) != '/') {
            return '/' + str;
        }
        return str;
    }





    public static String getFileNameFromUri(String str) {
        return Uri.parse(str).getLastPathSegment();
    }

    public static String getFileName(Download download) {
        download.getFile();
        return new File(download.getFile()).getName();
    }



    public static boolean isFileExists(String fileName, String downloadPath) {
        File file = new File(downloadPath, fileName);
        return file.exists();
    }


    public static String getDownloadDirectory() {
        String directory;
        if (Build.VERSION.SDK_INT >= 28) {
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Rex/";
        } else {
            directory = Environment.getExternalStorageDirectory().getPath() + "/Rex/";
        }

        File newDirectory = new File(directory);
        if (!newDirectory.exists()) {
            boolean created = newDirectory.mkdirs();
            if (!created) {
            }
        }

        return directory;
    }

    public static String getMimeType(Context context, Uri uri) {
        String extensionFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        return extensionFromMimeType == null ? MimeTypes.ALL_MIME_TYPES : extensionFromMimeType;
    }

    public static void deleteFileAndContents(File file) {
        File[] listFiles;
        if (file.exists()) {
            if (file.isDirectory() && (listFiles = file.listFiles()) != null) {
                for (File file2 : listFiles) {
                    deleteFileAndContents(file2);
                }
            }
            file.delete();
        }
    }

    public static boolean isFileNameValid(Context context, String str) {
        try {
            new File(context.getCacheDir(), str.trim()).getCanonicalPath();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }
    public static String autoRenameFile(String fileName, String downloadPath) {
        int counter = 1;

        while (isFileExists(fileName, downloadPath)) {
            String[] parts = fileName.split("\\.");
            String baseName = parts[0];
            String extension = parts.length > 1 ? "." + parts[1] : "";

            fileName = baseName + "_" + counter + extension;
            counter++;
        }

        return fileName;
    }
    public static void openFile(Context context, String str) {
        File file = new File(str);
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri fromFile = Build.VERSION.SDK_INT < 24 ? Uri.fromFile(file) : FileProvider.getUriForFile(context, context.getPackageName()+".file-provider", file);
        intent.setDataAndType(fromFile, context.getContentResolver().getType(fromFile));
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
        List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (queryIntentActivities.size() > 0) {
            for (ResolveInfo resolveInfo : queryIntentActivities) {
                context.grantUriPermission(resolveInfo.activityInfo.packageName, fromFile, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            context.startActivity(intent);
            return;
        }
        Toast.makeText(context, (int) R.string.no_app_to_open_file, Toast.LENGTH_LONG).show();
    }

    public static void shareFile(Context context, String str) {
        try {
            File file = new File(str);
            Uri fromFile = Build.VERSION.SDK_INT < 24 ? Uri.fromFile(file) : FileProvider.getUriForFile(context, context.getPackageName()+".file-provider", file);
            Intent intent = ShareCompat.IntentBuilder.from((Activity) context).setType(context.getContentResolver().getType(fromFile)).setStream(fromFile).getIntent();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.share_this_file_via));
            createChooser.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
            List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(createChooser, 0);
            if (queryIntentActivities.size() > 0) {
                for (ResolveInfo resolveInfo : queryIntentActivities) {
                    context.grantUriPermission(resolveInfo.activityInfo.packageName, fromFile, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                context.startActivity(createChooser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, (int) R.string.cant_share_file, Toast.LENGTH_LONG).show();
        }
    }
}
