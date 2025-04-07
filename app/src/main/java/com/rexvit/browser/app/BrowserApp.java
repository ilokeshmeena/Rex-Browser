package com.rexvit.browser.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.rexvit.browser.Interface.AppComponent;
import com.rexvit.browser.Interface.DaggerAppComponent;
import com.rexvit.browser.utils.MemoryLeakUtils;
import com.orm.SugarContext;
import com.squareup.otto.Bus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;


public class BrowserApp extends MultiDexApplication {
    private static AppComponent mAppComponent;
    private static final Executor mIOThread = Executors.newSingleThreadExecutor();
    private static final Executor mTaskThread = Executors.newCachedThreadPool();
    @Inject
    Bus mBus;
    private static BrowserApp instance;

    public BrowserApp() {
        instance = this;
    }

    @Override 
    public void onCreate() {
        super.onCreate();

        try {
            MultiDex.install(this);

        } catch (Exception e) {

        }

        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { 
            @Override 
            public void uncaughtException(Thread thread, Throwable th) {
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler = defaultUncaughtExceptionHandler;
                if (uncaughtExceptionHandler != null) {
                    uncaughtExceptionHandler.uncaughtException(thread, th);
                } else {
                    System.exit(2);
                }
            }
        });
        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        mAppComponent.inject(this);
        registerActivityLifecycleCallbacks(new MemoryLeakUtils.LifecycleAdapter() {
            @Override 
            public void onActivityDestroyed(Activity activity) {
                MemoryLeakUtils.clearNextServedView(activity, BrowserApp.this);
            }
        });
        SugarContext.init(getApplicationContext());
    }

    
    @Override 
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        try {
            MultiDex.install(this);

        } catch (Exception e) {
            
        }
    }

    @NonNull
    public static BrowserApp get(@NonNull Context context) {
        return (BrowserApp) context.getApplicationContext();
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

    @NonNull
    public static Executor getIOThread() {
        return mIOThread;
    }

    @NonNull
    public static Executor getTaskThread() {
        return mTaskThread;
    }

    public static void copyToClipboard(@NonNull Context context, @NonNull String str) {
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("URL", str));
    }

    @Override 
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    public static int dpToPx(int dp) {
        float density = instance.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}
