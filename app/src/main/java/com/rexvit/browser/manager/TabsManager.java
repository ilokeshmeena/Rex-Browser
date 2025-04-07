package com.rexvit.browser.manager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import com.rexvit.browser.Interface.Action;
import com.rexvit.browser.Interface.Subscriber;
import com.rexvit.browser.R;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.constant.Constants;
import com.rexvit.browser.database.HistoryDatabase;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.utils.Observable;
import com.rexvit.browser.utils.OnSubscribe;
import com.rexvit.browser.utils.schedulerUtils.Schedulers;
import com.rexvit.browser.view.BrowserView;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;


public class TabsManager {
    private static final String BUNDLE_KEY = "WEBVIEW_";
    private static final String BUNDLE_STORAGE = "SAVED_TABS.parcel";
    private static final String URL_KEY = "URL_KEY";
    @Inject
    Application mApp;
    @Nullable
    private BrowserView mCurrentTab;
    @Inject
    Bus mEventBus;
    @Inject
    HistoryDatabase mHistoryManager;
    @Nullable
    private TabNumberChangedListener mTabNumberListener;
    private final List<BrowserView> mTabList = new ArrayList(1);
    private boolean mIsInitialized = false;
    private final List<Runnable> mPostInitializationWorkList = new ArrayList();

    @Deprecated
    
    public interface TabNumberChangedListener {
        void tabNumberChanged(int i);
    }

    public TabsManager() {
        BrowserApp.getAppComponent().inject(this);
    }

    public void setTabNumberChangedListener(@Nullable TabNumberChangedListener tabNumberChangedListener) {
        this.mTabNumberListener = tabNumberChangedListener;
    }

    public void cancelPendingWork() {
        this.mPostInitializationWorkList.clear();
    }

    public synchronized void doAfterInitialization(@NonNull Runnable runnable) {
        if (this.mIsInitialized) {
            runnable.run();
        } else {
            this.mPostInitializationWorkList.add(runnable);
        }
    }

    
    public synchronized void finishInitialization() {
        this.mIsInitialized = true;
        for (Runnable runnable : this.mPostInitializationWorkList) {
            runnable.run();
        }
    }

    public synchronized Observable<Void> initializeTabs(@NonNull final Activity activity, @Nullable final Intent intent) {
        return Observable.create(new Action<Void>() { 
            @Override 
            public void onSubscribe(@NonNull Subscriber<Void> subscriber) {
                TabsManager.this.shutdown();
                Intent intent2 = intent;
                String dataString = intent2 != null ? intent2.getDataString() : null;
                TabsManager.this.mCurrentTab = null;
                TabsManager.this.restoreLostTabs(dataString, activity, subscriber);
            }
        });
    }

    
    public void restoreLostTabs(@Nullable final String str, @NonNull final Activity activity, @NonNull final Subscriber subscriber) {
        restoreState().subscribeOn(Schedulers.io()).observeOn(Schedulers.main()).subscribe(new OnSubscribe<Bundle>() {
            @Override 
            public void onNext(Bundle bundle) {
                BrowserView newTab = TabsManager.this.newTab(activity, "");
                if (newTab.getWebView() != null) {
                    newTab.getWebView().restoreState(bundle);
                }
            }

            @Override 
            public void onComplete() {
                String str2 = str;
                if (str2 == null) {
                    TabsManager.this.mTabList.isEmpty();
                    TabsManager.this.finishInitialization();
                    subscriber.onComplete();
                } else if (str2.startsWith(Constants.FILE)) {
                    new AlertDialog.Builder(activity).setCancelable(true).setTitle(R.string.title_warning).setMessage(R.string.message_blocked_local).setOnDismissListener(new DialogInterface.OnDismissListener() { 
                        @Override 
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (TabsManager.this.mTabList.isEmpty()) {
                                TabsManager.this.newTab(activity, null);
                            }
                            TabsManager.this.finishInitialization();
                            subscriber.onComplete();
                        }
                    }).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.open, new DialogInterface.OnClickListener() { 
                        @Override 
                        public void onClick(DialogInterface dialogInterface, int i) {
                            TabsManager.this.newTab(activity, str);
                        }
                    }).show();
                } else {
                    TabsManager.this.newTab(activity, str);
                    TabsManager.this.mTabList.isEmpty();
                    TabsManager.this.finishInitialization();
                    subscriber.onComplete();
                }
            }
        });
    }

    public void resumeAll(@NonNull Context context) {
        BrowserView currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.resumeTimers();
        }
        for (BrowserView browserView : this.mTabList) {
            if (browserView != null) {
                browserView.onResume();
                browserView.initializePreferences(context);
            }
        }
    }

    public void pauseAll() {
        BrowserView currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.pauseTimers();
        }
        for (BrowserView browserView : this.mTabList) {
            if (browserView != null) {
                browserView.onPause();
            }
        }
    }

    @Nullable
    public synchronized BrowserView getTabAtPosition(int i) {
        if (i >= 0) {
            if (i < this.mTabList.size()) {
                return this.mTabList.get(i);
            }
        }
        return null;
    }

    public synchronized void freeMemory() {
        for (BrowserView browserView : this.mTabList) {
            browserView.freeMemory();
        }
    }

    public synchronized void shutdown() {
        for (BrowserView browserView : this.mTabList) {
            browserView.onDestroy();
        }
        this.mTabList.clear();
        this.mIsInitialized = false;
        this.mCurrentTab = null;
    }

    public synchronized void notifyConnectionStatus(boolean z) {
        for (BrowserView browserView : this.mTabList) {
            WebView webView = browserView.getWebView();
            if (webView != null) {
                webView.setNetworkAvailable(z);
            }
        }
    }

    public synchronized int size() {
        return this.mTabList.size();
    }

    public synchronized int last() {
        return this.mTabList.size() - 1;
    }

    @Nullable
    public synchronized BrowserView lastTab() {
        if (last() < 0) {
            return null;
        }
        return this.mTabList.get(last());
    }

    @NonNull
    public synchronized BrowserView newTab(@NonNull Activity activity, @Nullable String str) {
        BrowserView browserView;
        browserView = new BrowserView(activity, str);
        this.mTabList.add(browserView);
        if (this.mTabNumberListener != null) {
            this.mTabNumberListener.tabNumberChanged(size());
        }
        return browserView;
    }

    private synchronized void removeTab(int i) {
        if (i >= this.mTabList.size()) {
            return;
        }
        BrowserView remove = this.mTabList.remove(i);
        if (this.mCurrentTab == remove) {
            this.mCurrentTab = null;
        }
        remove.onDestroy();
    }

    public synchronized boolean deleteTab(int i) {
        int positionOf;
        positionOf = positionOf(getCurrentTab());
        if (positionOf == i) {
            if (size() == 1) {
                this.mCurrentTab = null;
            } else if (positionOf < size() - 1) {
                switchToTab(positionOf + 1);
            } else {
                switchToTab(positionOf - 1);
            }
        }
        removeTab(i);
        if (this.mTabNumberListener != null) {
            this.mTabNumberListener.tabNumberChanged(size());
        }
        return positionOf == i;
    }

    public synchronized int positionOf(BrowserView browserView) {
        return this.mTabList.indexOf(browserView);
    }

    public void saveState() {
        Bundle bundle = new Bundle(ClassLoader.getSystemClassLoader());
        for (int i = 0; i < this.mTabList.size(); i++) {
            BrowserView browserView = this.mTabList.get(i);
            if (!TextUtils.isEmpty(browserView.getUrl())) {
                Bundle bundle2 = new Bundle(ClassLoader.getSystemClassLoader());
                if (browserView.getWebView() != null) {
                    browserView.getWebView().saveState(bundle2);
                    bundle.putBundle(BUNDLE_KEY + i, bundle2);
                } else if (browserView.getWebView() != null) {
                    bundle2.putString(URL_KEY, browserView.getUrl());
                    bundle.putBundle(BUNDLE_KEY + i, bundle2);
                }
            }
        }
        FileUtils.writeBundleToStorage(this.mApp, bundle, BUNDLE_STORAGE);
    }

    public void clearSavedState() {
        FileUtils.deleteBundleInStorage(this.mApp, BUNDLE_STORAGE);
    }

    private Observable<Bundle> restoreState() {
        return Observable.create(new Action<Bundle>() { 
            @Override 
            public void onSubscribe(@NonNull Subscriber<Bundle> subscriber) {
                Bundle readBundleFromStorage = FileUtils.readBundleFromStorage(TabsManager.this.mApp, TabsManager.BUNDLE_STORAGE);
                if (readBundleFromStorage != null) {
                    Log.d(Constants.TAG, "Restoring previous WebView state now");
                    for (String str : readBundleFromStorage.keySet()) {
                        if (str.startsWith(TabsManager.BUNDLE_KEY)) {
                            subscriber.onNext(readBundleFromStorage.getBundle(str));
                        }
                    }
                }
                FileUtils.deleteBundleInStorage(TabsManager.this.mApp, TabsManager.BUNDLE_STORAGE);
                subscriber.onComplete();
            }
        });
    }

    @Nullable
    public synchronized WebView getCurrentWebView() {
        return this.mCurrentTab != null ? this.mCurrentTab.getWebView() : null;
    }

    public synchronized int indexOfCurrentTab() {
        return this.mTabList.indexOf(this.mCurrentTab);
    }

    public synchronized int indexOfTab(BrowserView browserView) {
        return this.mTabList.indexOf(browserView);
    }

    @Nullable
    public synchronized BrowserView getCurrentTab() {
        return this.mCurrentTab;
    }

    @Nullable
    public synchronized BrowserView switchToTab(int i) {
        Log.d(Constants.TAG, "switch to tab: " + i);
        if (i >= 0 && i < this.mTabList.size()) {
            BrowserView browserView = this.mTabList.get(i);
            if (browserView != null) {
                this.mCurrentTab = browserView;
            }
            return browserView;
        }
        Log.e(Constants.TAG, "Returning a null  requested for position: " + i);
        return null;
    }
}
