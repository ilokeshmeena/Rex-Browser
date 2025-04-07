package com.rexvit.browser.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import com.rexvit.browser.constant.Constants;
import java.io.File;
import java.io.FileOutputStream;


public class ViewUnit {

    public static Bitmap capture(View view, float f, float f2, boolean z, Bitmap.Config config) {
        if (!view.isDrawingCacheEnabled()) {
            view.setDrawingCacheEnabled(true);
        }
        Bitmap createBitmap = Bitmap.createBitmap((int) f, (int) f2, config);
        createBitmap.eraseColor(-1);
        Canvas canvas = new Canvas(createBitmap);
        int left = view.getLeft();
        int top = view.getTop();
        if (z) {
            left = view.getScrollX();
            top = view.getScrollY();
        }
        int save = canvas.save();
        canvas.translate(-left, -top);
        float width = f / view.getWidth();
        canvas.scale(width, width, left, top);
        view.draw(canvas);
        canvas.restoreToCount(save);
        Paint paint = new Paint();
        paint.setColor(0);
        canvas.drawRect(0.0f, 0.0f, 1.0f, f2, paint);
        canvas.drawRect(f - 1.0f, 0.0f, f, f2, paint);
        canvas.drawRect(0.0f, 0.0f, f, 1.0f, paint);
        canvas.drawRect(0.0f, f2 - 1.0f, f, f2, paint);
        canvas.setBitmap(null);
        return createBitmap;
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static boolean isExternalStorageIsAvailable() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static int getWindowWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static String screenshot(Context context, Bitmap bitmap, String str) {
        if (bitmap == null) {
            return null;
        }
        File externalStoragePublicDirectory;

        if (Build.VERSION.SDK_INT >= 28) {
            externalStoragePublicDirectory=  Environment.getExternalStoragePublicDirectory("DCIM/Screenshots");
        }else {
            externalStoragePublicDirectory=  Environment.getExternalStoragePublicDirectory("Screen captures");
        }
        if (!externalStoragePublicDirectory.exists()) {
            externalStoragePublicDirectory.mkdirs();
        }
        if (str == null || str.trim().isEmpty()) {
            str = String.valueOf(System.currentTimeMillis());
        }
        String trim = str.trim();
        int i = 0;
        File file = new File(externalStoragePublicDirectory, trim + Constants.SUFFIX_PNG);
        while (file.exists()) {
            i++;
            file = new File(externalStoragePublicDirectory, trim + "." + i + Constants.SUFFIX_PNG);
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
            return file.getAbsolutePath();
        } catch (Exception unused) {
            return null;
        }
    }
}
