package com.rexvit.browser.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;


public class TabsImageView {
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
}
