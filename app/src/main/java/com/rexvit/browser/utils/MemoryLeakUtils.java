package com.rexvit.browser.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MemoryLeakUtils {
    private static final String TAG = "MemoryLeakUtils";
    private static Method sFinishInputLocked;

    
    public static abstract class LifecycleAdapter implements Application.ActivityLifecycleCallbacks {
        @Override 
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override 
        public void onActivityDestroyed(Activity activity) {
        }

        @Override 
        public void onActivityPaused(Activity activity) {
        }

        @Override 
        public void onActivityResumed(Activity activity) {
        }

        @Override 
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override 
        public void onActivityStarted(Activity activity) {
        }

        @Override 
        public void onActivityStopped(Activity activity) {
        }
    }

    public static void clearNextServedView(Activity activity, @NonNull Application application) {
        boolean z;
        Method method;
        Object obj = null;
        if (Build.VERSION.SDK_INT > 23) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) application.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (sFinishInputLocked == null) {
            try {
                sFinishInputLocked = InputMethodManager.class.getDeclaredMethod("finishInputLocked", new Class[0]);
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "Unable to find method in clearNextServedView", e);
            }
        }
        try {
            Field declaredField = InputMethodManager.class.getDeclaredField("mNextServedView");
            declaredField.setAccessible(true);
            obj = declaredField.get(inputMethodManager);
        } catch (IllegalAccessException e2) {
            Log.d(TAG, "Unable to access mNextServedView field", e2);
        } catch (NoSuchFieldException e3) {
            Log.d(TAG, "Unable to get mNextServedView field", e3);
        }
        if (obj instanceof View) {
            if (((View) obj).getContext() == activity) {
                z = true;
                method = sFinishInputLocked;
                if (method == null && z) {
                    method.setAccessible(true);
                    try {
                        sFinishInputLocked.invoke(inputMethodManager, new Object[0]);
                        return;
                    } catch (Exception e4) {
                        Log.d(TAG, "Unable to invoke method in clearNextServedView", e4);
                        return;
                    }
                }
            }
        }
        z = false;
        method = sFinishInputLocked;
        if (method == null) {
        }
    }
}
