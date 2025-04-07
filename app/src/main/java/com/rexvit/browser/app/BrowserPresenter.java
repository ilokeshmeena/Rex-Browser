package com.rexvit.browser.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.rexvit.browser.Interface.BrowserView;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.constant.Constants;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.utils.OnSubscribe;
import com.rexvit.browser.utils.schedulerUtils.Schedulers;
import com.squareup.otto.Bus;
import javax.inject.Inject;


public class BrowserPresenter {
    private static final String TAG = "BrowserPresenter";
    @Nullable
    private com.rexvit.browser.view.BrowserView mCurrentTab;
    @Inject
    Bus mEventBus;
    private boolean mShouldClose;
    @NonNull
    private final TabsManager mTabsModel;
    @NonNull
    private final BrowserView mView;

    public BrowserPresenter(@NonNull BrowserView browserView) {
        BrowserApp.getAppComponent().inject(this);
        this.mTabsModel = ((UIController) browserView).getTabModel();
        this.mView = browserView;
        this.mTabsModel.setTabNumberChangedListener(new TabsManager.TabNumberChangedListener() { 
            @Override 
            public void tabNumberChanged(int i) {
                BrowserPresenter.this.mView.updateTabNumber(i);
            }
        });
    }

    public void setupTabs(@Nullable Intent intent) {
        this.mTabsModel.initializeTabs((Activity) this.mView, intent).subscribeOn(Schedulers.main()).subscribe(new OnSubscribe<Void>() { 
            @Override 
            public void onComplete() {
                BrowserPresenter.this.mView.notifyTabViewInitialized();
                BrowserPresenter.this.mView.updateTabNumber(BrowserPresenter.this.mTabsModel.size());
                BrowserPresenter browserPresenter = BrowserPresenter.this;
                browserPresenter.tabChanged(browserPresenter.mTabsModel.last());
            }
        });
    }

    public void tabChangeOccurred(@Nullable com.rexvit.browser.view.BrowserView browserView) {
        this.mView.notifyTabViewChanged(this.mTabsModel.indexOfTab(browserView));
    }

    private void onTabChanged(@Nullable com.rexvit.browser.view.BrowserView browserView) {
        Log.d(TAG, "On tab changed");
        if (browserView == null) {
            this.mView.removeTabView();
            com.rexvit.browser.view.BrowserView browserView2 = this.mCurrentTab;
            if (browserView2 != null) {
                browserView2.pauseTimers();
                this.mCurrentTab.onDestroy();
            }
        } else if (browserView.getWebView() == null) {
            this.mView.removeTabView();
            com.rexvit.browser.view.BrowserView browserView3 = this.mCurrentTab;
            if (browserView3 != null) {
                browserView3.pauseTimers();
                this.mCurrentTab.onDestroy();
            }
        } else {
            com.rexvit.browser.view.BrowserView browserView4 = this.mCurrentTab;
            if (browserView4 != null) {
                browserView4.setForegroundTab(false);
            }
            browserView.resumeTimers();
            browserView.onResume();
            browserView.setForegroundTab(true);
            this.mView.updateProgress(browserView.getProgress());
            this.mView.setForwardButtonEnabled(browserView.canGoForward());
            this.mView.updateUrl(browserView.getUrl(), true);
            this.mView.setTabView(browserView.getWebView());
            if (this.mTabsModel.indexOfTab(browserView) >= 0) {
                this.mView.notifyTabViewChanged(this.mTabsModel.indexOfTab(browserView));
            }
        }
        this.mCurrentTab = browserView;
    }

    public void deleteTab(int i) {
        Log.d(TAG, "delete Tab");
        com.rexvit.browser.view.BrowserView tabAtPosition = this.mTabsModel.getTabAtPosition(i);
        if (tabAtPosition == null) {
            return;
        }
        boolean isShown = tabAtPosition.isShown();
        boolean z = this.mShouldClose && isShown && Boolean.TRUE.equals(tabAtPosition.getTag());
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsModel.getCurrentTab();
        if (this.mTabsModel.size() <= 1 && currentTab != null && currentTab.getUrl().equals(true)) {
            this.mView.closeActivity();
            return;
        }
        if (isShown) {
            this.mView.removeTabView();
        }
        if (this.mTabsModel.deleteTab(i)) {
            tabChanged(this.mTabsModel.indexOfCurrentTab());
        }
        com.rexvit.browser.view.BrowserView currentTab2 = this.mTabsModel.getCurrentTab();
        this.mView.notifyTabViewRemoved(i);
        if (currentTab2 == null) {
            this.mView.closeBrowser();
            return;
        }
        if (currentTab2 != currentTab) {
            if (currentTab != null) {
                currentTab.pauseTimers();
            }
            this.mView.notifyTabViewChanged(this.mTabsModel.indexOfCurrentTab());
        }
        if (z) {
            this.mShouldClose = false;
            this.mView.closeActivity();
        }
        this.mView.updateTabNumber(this.mTabsModel.size());
        Log.d(TAG, "deleted tab");
    }

    public void onNewIntent(@Nullable final Intent intent) {
        this.mTabsModel.doAfterInitialization(new Runnable() { 
            @Override 
            public void run() {
                Intent intent2 = intent;
                final String dataString = intent2 != null ? intent2.getDataString() : null;
                int i = 0;
                Intent intent3 = intent;
                if (intent3 != null && intent3.getExtras() != null) {
                    i = intent.getExtras().getInt(Constants.INTENT_ORIGIN);
                }
                if (i == 1) {
                    BrowserPresenter.this.newTabOpenAfterIntent(dataString);
                } else if (dataString != null) {
                    if (dataString.startsWith(Constants.FILE)) {
                        BrowserPresenter.this.mView.showBlockedLocalFileDialog(new DialogInterface.OnClickListener() { 
                            @Override 
                            public void onClick(DialogInterface dialogInterface, int i2) {
                                BrowserPresenter.this.newTabOpenAfterIntent(dataString);
                                BrowserPresenter.this.mShouldClose = true;
                                com.rexvit.browser.view.BrowserView lastTab = BrowserPresenter.this.mTabsModel.lastTab();
                                if (lastTab != null) {
                                    lastTab.setTag(true);
                                }
                            }
                        });
                        return;
                    }
                    BrowserPresenter.this.newTabOpenAfterIntent(dataString);
                    BrowserPresenter.this.mShouldClose = true;
                    com.rexvit.browser.view.BrowserView lastTab = BrowserPresenter.this.mTabsModel.lastTab();
                    if (lastTab != null) {
                        lastTab.setTag(true);
                    }
                }
            }
        });
    }

    
    public void newTabOpenAfterIntent(String str) {
        newTab(str, true);
    }

    public void loadUrlInCurrentView(@NonNull String str) {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsModel.getCurrentTab();
        if (currentTab == null) {
            return;
        }
        currentTab.loadUrl(str);
    }

    public void shutdown() {
        onTabChanged(null);
        this.mTabsModel.setTabNumberChangedListener(null);
        this.mTabsModel.cancelPendingWork();
    }

    public synchronized void tabChanged(int i) {
        String str = TAG;
        Log.d(str, "tabChanged: " + i);
        if (i >= 0 && i < this.mTabsModel.size()) {
            onTabChanged(this.mTabsModel.switchToTab(i));
        }
    }

    public synchronized boolean newTab(@Nullable String str, boolean z) {
        com.rexvit.browser.view.BrowserView newTab = this.mTabsModel.newTab((Activity) this.mView, str);
        if (this.mTabsModel.size() == 1) {
            newTab.resumeTimers();
        }
        this.mView.notifyTabViewAdded();
        if (z) {
            onTabChanged(this.mTabsModel.switchToTab(this.mTabsModel.last()));
        }
        this.mView.updateTabNumber(this.mTabsModel.size());
        return true;
    }

    public void onAutoCompleteItemPressed() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsModel.getCurrentTab();
        if (currentTab != null) {
            currentTab.requestFocus();
        }
    }

    public void onAppLowMemory() {
        this.mTabsModel.freeMemory();
    }
}
