package com.rexvit.browser.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.graphics.drawable.DrawableCompat;

import android.util.TypedValue;
import com.rexvit.browser.R;


public class ThemeUtils {
    private static final TypedValue sTypedValue = new TypedValue();

    public static int getThemedTextHintColor() {
        return Integer.MIN_VALUE;
    }

    public static int getPrimaryColor(@NonNull Context context) {
        return getColor(context, R.attr.colorPrimary);
    }

    public static int getColor(@NonNull Context context, @AttrRes int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(sTypedValue.data, new int[]{i});
        int color = obtainStyledAttributes.getColor(0, 0);
        obtainStyledAttributes.recycle();
        return color;
    }

    @NonNull
    private static Drawable getVectorDrawable(@NonNull Context context, int i) {
        @SuppressLint("RestrictedApi") Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, i);
        return Build.VERSION.SDK_INT < 21 ? DrawableCompat.wrap(drawable).mutate() : drawable;
    }

    @NonNull
    public static Drawable getThemedDrawable(@NonNull Context context, @DrawableRes int i) {
        Drawable vectorDrawable = getVectorDrawable(context, i);
        vectorDrawable.mutate();
        return vectorDrawable;
    }
}
