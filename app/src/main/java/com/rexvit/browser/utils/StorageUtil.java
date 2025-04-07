package com.rexvit.browser.utils;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.HttpUrl;


public class StorageUtil {
    private static final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");
    private static final String SECONDARY_STORAGES = System.getenv("SECONDARY_STORAGE");
    private static final String EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET");
    private static final String[] KNOWN_PHYSICAL_PATHS = {"/storage/sdcard0", "/storage/sdcard1", "/storage/extsdcard", "/storage/sdcard0/external_sdcard", "/mnt/extsdcard", "/mnt/sdcard/external_sd", "/mnt/sdcard/ext_sd", "/mnt/external_sd", "/mnt/media_rw/sdcard1", "/removable/microsd", "/mnt/emmc", "/storage/external_SD", "/storage/ext_sd", "/storage/removable/sdcard1", "/data/sdext", "/data/sdext2", "/data/sdext3", "/data/sdext4", "/sdcard1", "/sdcard2", "/storage/microsd"};

    public static List<String> getStorageDirectories(Context context) {
        ArrayList arrayList = new ArrayList();
        if (!TextUtils.isEmpty(EMULATED_STORAGE_TARGET)) {
            arrayList.add(getEmulatedStorageTarget());
        } else {
            arrayList.addAll(getExternalStorage(context));
        }
        Collections.addAll(arrayList, getAllSecondaryStorages());
        File usbDrive = getUsbDrive();
        if (usbDrive != null && !arrayList.contains(usbDrive.getPath())) {
            arrayList.add(usbDrive.getPath());
        }
        if (Build.VERSION.SDK_INT >= 19 && isUsbDeviceConnected(context)) {
            arrayList.add("otg://");
        }
        return arrayList;
    }

    private static Set<String> getExternalStorage(Context context) {
        File[] externalFilesDirs;
        HashSet hashSet = new HashSet();
        if (Build.VERSION.SDK_INT >= 23) {
            for (File file : getExternalFilesDirs(context, null)) {
                if (file != null) {
                    String absolutePath = file.getAbsolutePath();
                    hashSet.add(absolutePath.substring(0, absolutePath.indexOf("Android/data")));
                }
            }
        } else {
            String str = EXTERNAL_STORAGE;
            if (TextUtils.isEmpty(str)) {
                hashSet.addAll(getAvailablePhysicalPaths());
            } else {
                hashSet.add(str);
            }
        }
        return hashSet;
    }

    private static String getEmulatedStorageTarget() {
        Object obj;
        if (Build.VERSION.SDK_INT >= 17) {
            String[] split = File.separator.split(Environment.getExternalStorageDirectory().getAbsolutePath());
            obj = split[split.length - 1];
            if (!TextUtils.isEmpty((CharSequence) obj) && TextUtils.isDigitsOnly((CharSequence) obj)) {
                if (TextUtils.isEmpty((CharSequence) obj)) {
                    return EMULATED_STORAGE_TARGET;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(EMULATED_STORAGE_TARGET);
                stringBuilder.append(File.separator);
                stringBuilder.append(obj);
                return stringBuilder.toString();
            }
        }
        obj = HttpUrl.FRAGMENT_ENCODE_SET;
        if (TextUtils.isEmpty((CharSequence) obj)) {
            return EMULATED_STORAGE_TARGET;
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(EMULATED_STORAGE_TARGET);
        stringBuilder2.append(File.separator);
        stringBuilder2.append(obj);
        return stringBuilder2.toString();
    }

    private static String[] getAllSecondaryStorages() {
        String str = SECONDARY_STORAGES;
        return !TextUtils.isEmpty(str) ? str.split(File.pathSeparator) : new String[0];
    }

    private static List<String> getAvailablePhysicalPaths() {
        String[] strArr;
        ArrayList arrayList = new ArrayList();
        for (String str : KNOWN_PHYSICAL_PATHS) {
            if (new File(str).exists()) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    private static File[] getExternalFilesDirs(Context context, String str) {
        return Build.VERSION.SDK_INT >= 19 ? context.getExternalFilesDirs(str) : new File[]{context.getExternalFilesDir(str)};
    }

    private static boolean isUsbDeviceConnected(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        return (usbManager != null ? usbManager.getDeviceList().size() : 0) != 0;
    }

    public static File getUsbDrive() {
        File[] listFiles;
        try {
            for (File file : new File("/storage").listFiles()) {
                if (file.exists() && file.getName().toLowerCase().contains("usb") && file.canExecute()) {
                    return file;
                }
            }
        } catch (Exception unused) {
        }
        File file2 = new File("/mnt/sdcard/usbStorage");
        if (file2.exists() && file2.canExecute()) {
            return file2;
        }
        File file3 = new File("/mnt/sdcard/usb_storage");
        if (file3.exists() && file3.canExecute()) {
            return file3;
        }
        return null;
    }
}
