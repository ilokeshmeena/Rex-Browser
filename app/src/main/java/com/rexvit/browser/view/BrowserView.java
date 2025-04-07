package com.rexvit.browser.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.rexvit.browser.Interface.Action;
import com.rexvit.browser.Interface.Subscriber;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.activities.MainActivity;
import com.rexvit.browser.constant.Constants;
import com.rexvit.browser.database.BookmarksDb;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.app.BrowserEvents;
import com.rexvit.browser.downloads.DownloadHandler;
import com.rexvit.browser.downloads.DownloadStart;
import com.rexvit.browser.utils.FetchTitle;
import com.rexvit.browser.utils.Observable;
import com.rexvit.browser.utils.OnSubscribe;
import com.rexvit.browser.utils.Preference;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.utils.schedulerUtils.Schedulers;
import com.rexvit.browser.view.webClient.ChromeClient;
import com.rexvit.browser.view.webClient.WebClient;
import com.squareup.otto.Bus;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;


public class BrowserView {
    private static final int API = Build.VERSION.SDK_INT;
    private static final String HEADER_DNT = "DNT";
    private static final String TAG = "BrowserView";
    private static String sDefaultUserAgent;
    private boolean isForegroundTab;
    @NonNull
    private final Activity mActivity;
    @Inject
    Bus mEventBus;
    @NonNull
    private final Paint mPaint = new Paint();
    @NonNull
    private final Map<String, String> mRequestHeaders = new ArrayMap();
    @Nullable
    private Object mTag;
    @NonNull
    private final FetchTitle mTitle;
    @NonNull
    private final UIController mUIController;
    @Nullable
    private WebView mWebView;
    private WebSettings settings;

    @SuppressLint({"ClickableViewAccessibility"})
    public BrowserView(@NonNull Activity activity, @Nullable String str) {
        BrowserApp.getAppComponent().inject(this);
        this.mActivity = activity;
        this.mUIController = (UIController) activity;
        this.mWebView = new WebView(activity);
        if (Build.VERSION.SDK_INT > 16) {
            this.mWebView.setId(View.generateViewId());
        }
        this.mTitle = new FetchTitle(activity);
        this.mWebView.setDrawingCacheBackgroundColor(-1);
        this.mWebView.setFocusableInTouchMode(true);
        this.mWebView.setFocusable(true);
        this.mWebView.setDrawingCacheEnabled(false);
        this.mWebView.setWillNotCacheDrawing(true);
        if (Build.VERSION.SDK_INT <= 22) {
            this.mWebView.setAnimationCacheEnabled(false);
            this.mWebView.setAlwaysDrawnWithCacheEnabled(false);
        }
        this.mWebView.setBackgroundColor(-1);
        this.mWebView.setScrollbarFadingEnabled(true);
        this.mWebView.setSaveEnabled(true);
        this.mWebView.setNetworkAvailable(true);
        this.mWebView.setWebChromeClient(new ChromeClient(activity, this));
        this.mWebView.setWebViewClient(new WebClient(activity, this));
        this.mWebView.setDownloadListener(new DownloadStart(activity));
        sDefaultUserAgent = this.mWebView.getSettings().getUserAgentString();


        initializeSettings();
        initializePreferences(activity);
        if (str != null && !str.trim().isEmpty()) {
            this.mWebView.loadUrl(str, this.mRequestHeaders);
        }
        setupContextMenu(this.mWebView);
    }

    @Nullable
    public Object getTag() {
        return this.mTag;
    }

    public void setTag(@Nullable Object obj) {
        this.mTag = obj;
    }

    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    public synchronized void initializePreferences(@NonNull Context context) {
        if (this.mWebView == null) {
            return;
        }
        this.settings = this.mWebView.getSettings();
        this.mRequestHeaders.remove(HEADER_DNT);
        this.settings.setGeolocationEnabled(false);
        if (API < 19) {
            this.settings.setPluginState(WebSettings.PluginState.ON);
        }
        setUserAgent(context);
        if (API < 18) {
            this.settings.setSavePassword(false);
        }
        this.settings.setSaveFormData(false);
        this.settings.setJavaScriptEnabled(true);
        this.settings.setJavaScriptCanOpenWindowsAutomatically(true);
        this.settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        if (API >= 19) {
            try {
                this.settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Exception unused) {
                Log.e(TAG, "Problem setting LayoutAlgorithm to TEXT_AUTOSIZING");
            }
        }
        this.settings.setLoadsImagesAutomatically(Preference.datSaveMode(this.mActivity));
        this.settings.setSupportMultipleWindows(true);
        this.settings.setUseWideViewPort(true);
        this.settings.setLoadWithOverviewMode(true);
        this.settings.setTextZoom(100);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this.mWebView, true);
        }
    }

    @SuppressLint({"NewApi"})
    private void initializeSettings() {
        WebView webView = this.mWebView;
        if (webView == null) {
            return;
        }
        final WebSettings settings = webView.getSettings();
        if (API < 18) {

            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        }
        if (API < 17) {
            settings.setEnableSmoothTransition(true);
        }
        if (API > 16) {
            settings.setMediaPlaybackRequiresUserGesture(true);
        }
        if (API >= 21) {
            settings.setMixedContentMode(2);
        }
        settings.setDomStorageEnabled(true);
       
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);


        int nightModeFlags = mActivity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            settings.setForceDark(WebSettings.FORCE_DARK_ON);

        }


        getPathObservable("appcache").subscribeOn(Schedulers.io()).observeOn(Schedulers.main()).subscribe(new OnSubscribe<File>() {
            @Override 
            public void onComplete() {
            }

            @Override 
            public void onNext(File file) {
               
            }
        });
        if (Build.VERSION.SDK_INT < 24) {
            getPathObservable("geolocation").subscribeOn(Schedulers.io()).observeOn(Schedulers.main()).subscribe(new OnSubscribe<File>() { 
                @Override 
                public void onComplete() {
                }

                @Override 
                public void onNext(File file) {
                    settings.setGeolocationDatabasePath(file.getPath());
                }
            });
        }
        getPathObservable("databases").subscribeOn(Schedulers.io()).observeOn(Schedulers.main()).subscribe(new OnSubscribe<File>() { 
            @Override 
            public void onComplete() {
            }

            @Override 
            public void onNext(File file) {
                if (BrowserView.API < 19) {
                    settings.setDatabasePath(file.getPath());
                }
            }
        });
    }

    private Observable<File> getPathObservable(final String str) {
        return Observable.create(new Action<File>() {
            @Override 
            public void onSubscribe(@NonNull Subscriber<File> subscriber) {
                subscriber.onNext(BrowserApp.get(BrowserView.this.mActivity).getDir(str, 0));
                subscriber.onComplete();
            }
        });
    }

    @NonNull
    public FetchTitle getTitleInfo() {
        return this.mTitle;
    }

    @SuppressLint({"NewApi"})
    private void setUserAgent(Context context) {
        WebView webView = this.mWebView;
        if (webView == null) {
            return;
        }
        WebSettings settings = webView.getSettings();
        if (Preference.desktopMode(this.mActivity)) {
            settings.setUserAgentString(Constants.DESKTOP_USER_AGENT);
        } else if (API >= 17) {
            settings.setUserAgentString(WebSettings.getDefaultUserAgent(context));
        } else {
            settings.setUserAgentString(sDefaultUserAgent);
        }
    }

    @NonNull
    public Map<String, String> getRequestHeaders() {
        return this.mRequestHeaders;
    }

    public boolean isShown() {
        WebView webView = this.mWebView;
        return webView != null && webView.isShown();
    }

    public synchronized void onPause() {
        if (this.mWebView != null) {
            this.mWebView.onPause();
            String str = TAG;
            Log.d(str, "WebView onPause: " + this.mWebView.getId());
        }
    }

    public synchronized void onResume() {
        if (this.mWebView != null) {
            this.mWebView.onResume();
            String str = TAG;
            Log.d(str, "WebView onResume: " + this.mWebView.getId());
        }
    }

    @Deprecated
    public synchronized void freeMemory() {
        if (this.mWebView != null && Build.VERSION.SDK_INT < 19) {
            this.mWebView.freeMemory();
        }
    }

    public boolean isForegroundTab() {
        return this.isForegroundTab;
    }

    public void setForegroundTab(boolean z) {
        this.isForegroundTab = z;
        this.mUIController.tabChanged(this);
    }

    public int getProgress() {
        WebView webView = this.mWebView;
        if (webView != null) {
            return webView.getProgress();
        }
        return 100;
    }

    public synchronized void stopLoading() {
        if (this.mWebView != null) {
            this.mWebView.stopLoading();
        }
    }

    public synchronized void pauseTimers() {
        if (this.mWebView != null) {
            this.mWebView.pauseTimers();
            Log.d(TAG, "Pausing JS timers");
        }
    }

    public synchronized void resumeTimers() {
        if (this.mWebView != null) {
            this.mWebView.resumeTimers();
            Log.d(TAG, "Resuming JS timers");
        }
    }

    public void requestFocus() {
        WebView webView = this.mWebView;
        if (webView == null || webView.hasFocus()) {
            return;
        }
        this.mWebView.requestFocus();
    }

    public void setVisibility(int i) {
        WebView webView = this.mWebView;
        if (webView != null) {
            webView.setVisibility(i);
        }
    }

    public synchronized void reload() {
        if (this.mWebView != null) {
            this.mWebView.reload();
        }
    }

    public synchronized void desktopSet() {
        if (this.settings != null) {
            this.settings.setUserAgentString(Constants.DESKTOP_USER_AGENT);
        }
    }

    public synchronized void phoneSet() {
        if (this.settings != null) {
            if (Build.VERSION.SDK_INT >= 17) {
                this.settings.setUserAgentString(WebSettings.getDefaultUserAgent(MainActivity.mActivity));
            }
            this.settings.setUserAgentString(sDefaultUserAgent);
        }
    }

    public synchronized void imageOnSet() {
        if (this.settings != null) {
            this.settings.setLoadsImagesAutomatically(true);
        }
    }

    public synchronized void imageOffSet() {
        if (this.settings != null) {
            this.settings.setLoadsImagesAutomatically(false);
        }
    }

    public synchronized void onDestroy() {
        if (this.mWebView != null) {
            ViewGroup viewGroup = (ViewGroup) this.mWebView.getParent();
            if (viewGroup != null) {
                Log.e(TAG, "WebView was not detached from window before onDestroy");
                viewGroup.removeView(this.mWebView);
            }
            this.mWebView.stopLoading();
            this.mWebView.onPause();
            this.mWebView.clearHistory();
            this.mWebView.setVisibility(View.GONE);
            this.mWebView.removeAllViews();
            this.mWebView.destroyDrawingCache();
            if (Build.VERSION.SDK_INT >= 18) {
                this.mWebView.destroy();
            }
            this.mWebView = null;
        }
    }

    public synchronized void goBack() {
        if (this.mWebView != null) {
            this.mWebView.goBack();
        }
    }

    public synchronized void goForward() {
        if (this.mWebView != null) {
            this.mWebView.goForward();
        }
    }

    public boolean canGoBack() {
        WebView webView = this.mWebView;
        return webView != null && webView.canGoBack();
    }

    public boolean canGoForward() {
        WebView webView = this.mWebView;
        return webView != null && webView.canGoForward();
    }

    @Nullable
    public synchronized WebView getWebView() {
        return this.mWebView;
    }




    public synchronized void loadUrl(@NonNull String url) {
        if (this.mWebView != null && isValidUrl(url)) {
            this.mWebView.loadUrl(url, this.mRequestHeaders);
        }
    }


    /**
     * Validates the provided URL to ensure it is safe for loading.
     * This method can be enhanced to include more complex URL validation logic.
     */
    private boolean isValidUrl(String url) {
        // Validate the URL to prevent XSS or other security threats
        // For example, only allow HTTP or HTTPS protocols and avoid JavaScript code
        return url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://"));
    }





    @NonNull
    public String getTitle() {
        return this.mTitle.getTitle();
    }

    @NonNull
    public WebView getWeb() {
        return this.mWebView;
    }

    @NonNull
    public Bitmap getFavicon() {
        return this.mWebView.getFavicon();
    }

    @NonNull
    public String getUrl() {
        WebView webView = this.mWebView;
        return (webView == null || webView.getUrl() == null) ? "" : this.mWebView.getUrl();
    }

    
    
    
    public class ActionMenuLis implements View.OnCreateContextMenuListener {
        ActionMenuLis() {
        }

        @Override 
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            WebView.HitTestResult hitTestResult = ((WebView) view).getHitTestResult();
            int type = hitTestResult.getType();
            final String extra = hitTestResult.getExtra();
            if (type == 7) {
                contextMenu.add(0, 12, 0, R.string.open_in_new).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.mEventBus.post(new BrowserEvents.OpenUrlInNewTab(extra));
                        return true;
                    }
                });
                
                contextMenu.setHeaderTitle(hitTestResult.getExtra());
            } else if (type == 5) {
                contextMenu.add(0, 12, 0, R.string.view_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.mEventBus.post(new BrowserEvents.OpenUrlInNewTab(extra));
                        return true;
                    }
                });
                contextMenu.add(0, 13, 0, R.string.dialog_open_background_tab).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.mEventBus.post(new BrowserEvents.OpenUrlInNewTab(extra, BrowserEvents.OpenUrlInNewTab.Location.BACKGROUND));
                        return true;
                    }
                });
                contextMenu.add(0, 15, 0, R.string.copy_image_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserApp.copyToClipboard(BrowserView.this.mActivity, extra);
                        Utils.msg(BrowserView.this.mActivity.getString(R.string.copied), BrowserView.this.mActivity);
                        return true;
                    }
                });
                contextMenu.add(0, 17, 0, R.string.share_image_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.shareUrl(extra);
                        return true;
                    }
                });
                contextMenu.add(0, 14, 0, R.string.save_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(BrowserView.this.mActivity, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, new PermissionsResultAction() { 
                            @Override 
                            public void onDenied(String str) {
                            }

                            @Override 
                            public void onGranted() {
                                DownloadHandler.onDownloadStart(BrowserView.this.mActivity, extra, "", "attachment", null, "");
                            }
                        });
                        return true;
                    }
                });
                contextMenu.setHeaderTitle(hitTestResult.getExtra());
            } else if (type == 4) {
                contextMenu.add(0, 16, 0, R.string.send_email).setIntent(new Intent("android.intent.action.VIEW", Uri.parse("mailto:" + hitTestResult.getExtra())));
                contextMenu.add(0, 15, 0, R.string.copy_email).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserApp.copyToClipboard(BrowserView.this.mActivity, extra);
                        Utils.msg(BrowserView.this.mActivity.getString(R.string.copied), BrowserView.this.mActivity);
                        return true;
                    }
                });
                contextMenu.add(0, 17, 0, R.string.share_email).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.shareUrl(extra);
                        return true;
                    }
                });
                contextMenu.setHeaderTitle(hitTestResult.getExtra());
            } else if (type == 2) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("tel:"));
                MainActivity.mActivity.startActivity(intent);
                contextMenu.add(0, 18, 0, R.string.call).setIntent(intent);
                contextMenu.add(0, 15, 0, R.string.copy_num).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserApp.copyToClipboard(BrowserView.this.mActivity, extra);
                        Utils.msg(BrowserView.this.mActivity.getString(R.string.copied), BrowserView.this.mActivity);
                        return true;
                    }
                });
                contextMenu.add(0, 17, 0, R.string.share_num).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.shareUrl(extra);
                        return true;
                    }
                });
                contextMenu.setHeaderTitle(hitTestResult.getExtra());
            }

                contextMenu.add(0, 13, 0, R.string.dialog_open_background_tab).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.mEventBus.post(new BrowserEvents.OpenUrlInNewTab(extra, BrowserEvents.OpenUrlInNewTab.Location.BACKGROUND));
                        return true;
                    }
                });
                contextMenu.add(0, 15, 0, R.string.copy_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserApp.copyToClipboard(BrowserView.this.mActivity, extra);
                        Utils.msg(BrowserView.this.mActivity.getString(R.string.copied), BrowserView.this.mActivity);
                        return true;
                    }
                });
                contextMenu.add(0, 17, 0, R.string.share_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        BrowserView.this.shareUrl(extra);
                        return true;
                    }
                });
                contextMenu.add(0, 19, 0, R.string.save_to_bookmarks).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() { 
                    @Override 
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String str = extra;
                        String iconText = Utils.getIconText(str, Utils.getTitleForSearchBar(str));
                        String str2 = extra;
                        new BookmarksDb(str2, str2, iconText, 0, Utils.getDateTime() + " " + Utils.getDateString()).save();
                        Utils.msg(BrowserView.this.mActivity.getString(R.string.saved_to_bookmarks), BrowserView.this.mActivity);
                        return true;
                    }
                });
                contextMenu.setHeaderTitle(hitTestResult.getExtra());
            }
        }


    private void setupContextMenu(View view) {
        view.setOnCreateContextMenuListener(new ActionMenuLis());
    }

    
    public void shareUrl(String str) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TITLE", "");
        intent.putExtra("android.intent.extra.TEXT", str);
        try {
            this.mActivity.startActivity(Intent.createChooser(intent, this.mActivity.getString(R.string.share_with)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getScroll() {
        WebView webView = this.mWebView;
        if (webView != null) {
            return webView.getScrollY();
        }
        return 0;
    }
}
