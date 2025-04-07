package com.rexvit.browser.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.multidex.BuildConfig;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

//import com.ads.control.AdmobHelp;
//import com.ads.control.InterstitialAdShow;
import com.afollestad.materialdialogs.MaterialDialog;
import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.rexvit.browser.utils.WebPage;
import com.rexvit.browser.adapter.WebPageAdapter;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.rexvit.browser.Interface.Action;
import com.rexvit.browser.Interface.BrowserView;
import com.rexvit.browser.Interface.Subscriber;
import com.rexvit.browser.Interface.TabsView;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.SuggestionsAdapter;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.app.BrowserEvents;
import com.rexvit.browser.app.BrowserPresenter;
import com.rexvit.browser.constant.SettingsConstant;
import com.rexvit.browser.constant.SettingsKeys;
import com.rexvit.browser.database.AdBlockDb;
import com.rexvit.browser.database.BookmarksDb;
import com.rexvit.browser.database.HistoryDatabase;
import com.rexvit.browser.database.HistoryItem;
import com.rexvit.browser.dialog.AppSettings;
import com.rexvit.browser.dialog.MessageBox;
import com.rexvit.browser.dialog.OneTimeAlertDialog;
import com.rexvit.browser.dialog.SettingsBottomSheetFragment;
import com.rexvit.browser.fragment.TabsFragment;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.services.NetworkReceiver;
import com.rexvit.browser.taskUtils.ScreenshotTask;
import com.rexvit.browser.utils.AdBlock;
import com.rexvit.browser.utils.ClearingData;
import com.rexvit.browser.utils.DialogUtility;
import com.rexvit.browser.utils.Observable;
import com.rexvit.browser.utils.Preference;
import com.rexvit.browser.utils.Task;
import com.rexvit.browser.utils.ThemeUtils;
import com.rexvit.browser.utils.UrlUtils;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.utils.ViewUnit;
import com.rexvit.browser.utils.schedulerUtils.Schedulers;
import com.rexvit.browser.view.customView.AnimatedProgressBar;
import com.rexvit.browser.view.customView.FocusEditText;
import com.rexvit.browser.view.customView.SearchView;
import com.rexvit.browser.view.viewListener.MainSearchListener;
import com.rexvit.browser.view.viewListener.SearchListener;
import com.rexvit.browser.view.viewListener.VideoCompletionListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import angtrim.com.fivestarslibrary.FiveStarsDialog;


public class MainActivity extends BaseActivity implements View.OnClickListener, BrowserView, UIController, WebPageAdapter.ItemClickListener {
    private static final String NETWORK_BROADCAST_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    static final boolean assertionsDisabled = false;

    private static final String TAG = "MainActivity";
    private static long back_pressed;
    public static Activity mActivity;
    private static int searchEngineInt;
    private boolean ask;
    private boolean baidu;
    private boolean bing;
    private boolean lukayn;
    private boolean duckDuckGo;
    ImageView exitfindInPage;
    ImageView findInPageDown;
    ImageView findInPageUp;
    FocusEditText findInput;
    ImageView ivClearText;

    private boolean google;
    private int mBackgroundColor;
    FrameLayout mBrowserFrame;
    private String mCameraPhotoPath;
    @Nullable
    private View mCurrentView;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    @Inject
    Bus mEventBus;
    ImageView ivSearch;
    static PopupWindow toolsPopWindowTop;
    static PopupWindow firstPopWindowTop;

    private LinearLayout lrRefStop;
    private ImageView ivRefStop;
    private LinearLayout lrCleaText;

    private ValueCallback<Uri[]> mFilePathCallback;
    LinearLayout mFindInPageLayout;
    private ImageView tools;
    private FrameLayout mFullscreenContainer;
    @Inject
    HistoryDatabase mHistoryDatabase;
    FrameLayout mIconLayout;
    private long mKeyDownStartTime;
    FocusEditText mMainSearchBar;
    TextView mMainTextNotif;
    TextView mMainTextNotif2;
    RelativeLayout mMainUi;
    ImageView mNotifCountButtonImage;
    FrameLayout mNotifIcon2;
    private BrowserPresenter mPresenter;
    AnimatedProgressBar mProgressBar;
    public static boolean isWelcomeLinkInputFocused;
    private SearchView mSearch;
    private String mSearchText;
    private SuggestionsAdapter mSuggestionsAdapter;
    RelativeLayout mTabLayout;
    private TabsManager mTabsManager;
    private TabsView mTabsView;
    ViewGroup mUiLayout;
    private ValueCallback<Uri> mUploadMessage;
    private VideoView mVideoView;
    ImageView mserachEngineIons;
    private Context applicationContext;
    private UIController mUiController;
    SwitchCompat mAdBlock;
    TextView mAdsCount;
    SwitchCompat mCloseTabs;
    SwitchCompat mDesktopSwith;
    SwitchCompat mLoadImages;
    TextView mSetSearchenginetext;
    private int num;
    private boolean yahoo;
    private boolean yandex;
    ViewTreeObserver.OnScrollChangedListener mScrollListner;

    private static final int API = Build.VERSION.SDK_INT;
    private static final ViewGroup.LayoutParams MATCH_PARENT = new ViewGroup.LayoutParams(-1, -1);
    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(-1, -1);
    private final ColorDrawable mBackground = new ColorDrawable();
    private ConsentInformation consentInformation;
    SwipeRefreshLayout mSwipe;
    public AppCompatRadioButton themeSystemDev, themeLight, themeDark;

    private static final int STORAGE_PERMISSION_CODE = 100;

    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.POST_NOTIFICATIONS};

    private List<WebPage> webPages = new ArrayList<>();
    private WebPageAdapter adapter;
    private RecyclerView recyclerView;
    private final NetworkReceiver mNetworkReceiver = new NetworkReceiver() {
        @Override
        public void onConnectivityChange(boolean z) {
            MainActivity.this.mTabsManager.notifyConnectionStatus(z);
        }
    };
    private final Object mBusEventListener = new Object() {
        @Subscribe
        public void loadUrlInNewTab(BrowserEvents.OpenUrlInNewTab openUrlInNewTab) {
            if (openUrlInNewTab.location == BrowserEvents.OpenUrlInNewTab.Location.NEW_TAB) {
                MainActivity.this.newTab(openUrlInNewTab.url, true);
            } else if (openUrlInNewTab.location == BrowserEvents.OpenUrlInNewTab.Location.BACKGROUND) {
                MainActivity.this.newTab(openUrlInNewTab.url, false);
            }
        }
    };


    public Context getActivity() {
        return this;
    }

    private int getTabsFragmentViewId() {
        return R.id.tablayoutcontainer;
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void setForwardButtonEnabled(boolean z) {
    }

    @Override
    public void updateHistory(@Nullable String str, @NonNull String str2) {
    }

    private static void removeViewFromParent(@Nullable View view) {
        if (view == null) {
            return;
        }
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    @SuppressLint("NonConstantResourceId")

    public void openTabs() {
        this.mTabLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void showtablayout() {
        this.mTabLayout.setVisibility(View.VISIBLE);
        this.mMainUi.setVisibility(View.GONE);
        this.mUiLayout.setVisibility(View.VISIBLE);
    }

    public Observable<Void> updateCookiePreference() {
        return Observable.create(new Action<Void>() {
            @Override
            public void onSubscribe(@NonNull Subscriber<Void> subscriber) {
                CookieManager cookieManager = CookieManager.getInstance();
                if (Build.VERSION.SDK_INT < 21) {
                    CookieSyncManager.createInstance(MainActivity.this);
                }
                cookieManager.setAcceptCookie(true);
                subscriber.onComplete();
            }
        });
    }

    @Override
    public void closeActivity() {
        this.mMainUi.setVisibility(View.VISIBLE);
        this.mUiLayout.setVisibility(View.GONE);
    }

    @Override
    public void autocomplete() {
        this.mPresenter.onAutoCompleteItemPressed();
    }


    public void makeShortcut(com.rexvit.browser.view.BrowserView browserView) {
        if (browserView != null) {
            HistoryItem historyItem = new HistoryItem(browserView.getUrl(), browserView.getTitle());
            historyItem.setBitmap(browserView.getFavicon());
            Utils.createShortcut(this, historyItem);
        }
    }


    public void captureScreen(final com.rexvit.browser.view.BrowserView browserView) {

        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mActivity, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, new PermissionsResultAction() {
            @Override
            public void onDenied(String str) {
            }

            @Override
            public void onGranted() {
                com.rexvit.browser.view.BrowserView browserView2;
                if (!ViewUnit.isExternalStorageIsAvailable() || (browserView2 = browserView) == null) {
                    return;
                }
                new ScreenshotTask(MainActivity.this, browserView2.getWebView()).execute();
            }
        });
    }


    public void translatePage(com.rexvit.browser.view.BrowserView browserView) {
        browserView.loadUrl("https://translate.google.com/translate?sl=auto" + Locale.getDefault().getLanguage() + "&u=" + browserView.getUrl());


    }

    public void openFindInPage() {
        showActionBar();
        setUpFindBarListeners(this.findInput);
        this.mFindInPageLayout.setVisibility(View.VISIBLE);
        this.findInput.requestFocus();
    }

    public void exitFindInPage() {
        this.mFindInPageLayout.setVisibility(View.INVISIBLE);
        this.findInput.clearFocus();
        this.findInput.setText("");
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.getWebView().clearMatches();
        }
    }

    public void findUp() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.getWebView().findNext(false);
        }
    }

    public void findDown() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.getWebView().findNext(true);
        }
    }

    public void setUpFindBarListeners(FocusEditText focusEditText) {
        final com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        focusEditText.setImeOptions(268435457);
        focusEditText.selectAll();
        focusEditText.setSelectAllOnFocus(true);
        focusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (Build.VERSION.SDK_INT >= 16) {
                        currentTab.getWebView().findAllAsync(editable.toString());
                    }
                } catch (Exception unused) {
                }
            }
        });
    }

    LinearLayout mainScreenMenuIcon2;
    FrameLayout mNotificationTabIcon;
    LinearLayout ShowMenuPopup;


    @Override
    protected void onExit() {

    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        BrowserApp.getAppComponent().inject(this);
        applicationContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        mActivity = this;

        SharedPreferences sharedPref = getSharedPreferences("WebPages", MODE_PRIVATE);
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> isGranted) {
                        boolean granted = true;
                        for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                            logthis(x.getKey() + " is " + x.getValue());
                            if (!x.getValue()) granted = false;
                        }
                        if (granted)
                            logthis("Permissions granted for api 33+");
                    }
                }
        );

        isAskNotifi();
        this.mUiController = (UIController) mActivity;
        this.mTabsManager = new TabsManager();
        Preference.vpn(this);

        this.mPresenter = new BrowserPresenter(this);
        toolsPopWindowTop = new PopupWindow(this);
        firstPopWindowTop = new PopupWindow(this);

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {


                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {

                    }
                });

//        AdmobHelp.getInstance().loadBanner(this);
        this.mBackground.setColor(ThemeUtils.getPrimaryColor(this));
        if (Build.VERSION.SDK_INT < 33) {
            permissionReq();
        }
        intView();
        intEvent();
        initialize(bundle);

        this.mHistoryDatabase = new HistoryDatabase(getActivity());
        fivestar();
        recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5, RecyclerView.VERTICAL, false);
        gridLayoutManager.setRecycleChildrenOnDetach(true);
        gridLayoutManager.setAutoMeasureEnabled(true);
        this.recyclerView.setNestedScrollingEnabled(false);

        this.recyclerView.setItemAnimator(null);
        this.recyclerView.setLayoutManager(gridLayoutManager);
        webPages = loadWebPages();
        if (webPages == null || webPages.isEmpty()) {
            webPages = createDefaultPages();
            saveWebPages(webPages);
        }

        adapter = new WebPageAdapter(this, webPages);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            if (currentTab.getProgress() < 100) {
                ivRefStop.setImageResource(R.drawable.ic_stop_or_close);
            } else {
                ivRefStop.setImageResource(R.drawable.ic_refresh);
            }
        }

    }

    private boolean isValidUrl(String url) {


        return url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://"));
    }

    @Override
    public void onItemClick(View view, int position) {
        WebPage page = webPages.get(position);


        Intent intent = new Intent("android.intent.action.WEB_SEARCH", Uri.parse(page.getUrl()));
        intent.setPackage(this.getPackageName());
        MainActivity.this.startActivity(intent);
    }

    private void saveWebPages(List<WebPage> webPages) {
        SharedPreferences sharedPref = getSharedPreferences("WebPages", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(webPages);
        editor.putString("savedPages", json);
        editor.apply();
    }

    private List<WebPage> loadWebPages() {
        SharedPreferences sharedPref = getSharedPreferences("WebPages", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("savedPages", null);
        Type type = new TypeToken<ArrayList<WebPage>>() {
        }.getType();
        List<WebPage> webPages = gson.fromJson(json, type);
        return webPages == null ? new ArrayList<WebPage>() : webPages;
    }

    @Override
    public void onItemLongClick(View view, final int position) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Page")
                .setMessage("Are you sure you want to delete this page?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onAddButtonClick() {
        showAddPageDialog();
    }

    private void deleteItem(int position) {
        webPages.remove(position);
        adapter.notifyItemRemoved(position);
        saveWebPages(webPages);
    }

    private void fivestar() {
        new FiveStarsDialog(this, "contact." + getString(R.string.email_dev)).setRateText(getString(R.string.rating_title)).setTitle(getString(R.string.rating_desc)).setForceMode(true).setUpperBound(4).showAfter(5);
    }

    private void permissionReq() {

        AlertDialog.Builder title = new OneTimeAlertDialog.Builder(this, "my_dialog_key").setTitle(R.string.permissions_title);
        title.setMessage(getString(R.string.permission_msg_WRITE_EXTERNAL_STORAGE) + "\n\n" + getString(R.string.permission_msg_READ_EXTERNAL_STORAGE) + "\n\n" + getString(R.string.permission_msg_text)).setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"}, new PermissionsResultAction() {
                    @Override
                    public void onDenied(String str) {
                    }

                    @Override
                    public void onGranted() {
                    }
                });


            }
        }).setCancelable(false).show();
    }

    public void intView() {
        mSwipe = findViewById(R.id.swipeMAin);
        mBrowserFrame = findViewById(R.id.content_frame);
        lrRefStop = findViewById(R.id.lrRefStop);
        lrCleaText = findViewById(R.id.lrCleaText);
        ivRefStop = findViewById(R.id.ivRefStop);
        mserachEngineIons = findViewById(R.id.mainScreenSearchEngine);
        mUiLayout = findViewById(R.id.ui_layout);
        mTabLayout = findViewById(R.id.rightDrawerLayout2);
        mFindInPageLayout = findViewById(R.id.findInPageLayout);
        mIconLayout = findViewById(R.id.topSearchIconLayout);
        mMainSearchBar = findViewById(R.id.searchBar2);
        mMainTextNotif = findViewById(R.id.notif_count_text);
        mMainTextNotif2 = findViewById(R.id.notif_count_text2);
        mMainUi = findViewById(R.id.mainscreenui);
        mNotifCountButtonImage = findViewById(R.id.notif_count);
        mNotifIcon2 = findViewById(R.id.notificationTabIcon2);
        mNotificationTabIcon = findViewById(R.id.notificationTabIcon);
        ShowMenuPopup = findViewById(R.id.btn_menuPopup);
        mProgressBar = findViewById(R.id.progress_view);
        tools = findViewById(R.id.tools_button);
        ivClearText = findViewById(R.id.ivClearText);
        mFindInPageLayout = findViewById(R.id.findInPageLayout);
        exitfindInPage = findViewById(R.id.exitfindInPage);
        findInPageUp = findViewById(R.id.findinPageUp);
        findInput = findViewById(R.id.find_input);
        ivSearch = findViewById(R.id.ivSearch);
        findInPageDown = findViewById(R.id.findInPageDown);
        mainScreenMenuIcon2 = findViewById(R.id.mainScreenMenuIcon2);

        ShowMenuPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindowTop(v);
            }
        });
    }

    public void intEvent() {
        ShowMenuPopup.setOnClickListener(this);
        mNotifIcon2.setOnClickListener(this);
        findInPageDown.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        lrCleaText.setOnClickListener(this);
        lrRefStop.setOnClickListener(this);
        mNotificationTabIcon.setOnClickListener(this);
        mainScreenMenuIcon2.setOnClickListener(this);
        findInPageUp.setOnClickListener(this);
        exitfindInPage.setOnClickListener(this);
    }

    private synchronized void initialize(Bundle bundle) {
        TabsFragment tabsFragment = new TabsFragment();
        this.mTabsView = tabsFragment;
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean("", true);
        tabsFragment.setArguments(bundle2);
        getSupportFragmentManager().beginTransaction().replace(getTabsFragmentViewId(), tabsFragment, "tabss").commit();
        boolean z = false;
        updateTabNumber(0);
        this.mSwipe.setColorSchemeColors(Utils.mSwipeColor);
        this.mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.this.refreshOrStop();
            }
        });
        this.mSearch = findViewById(R.id.search);
        this.mSearch.setHintTextColor(ThemeUtils.getThemedTextHintColor());
        this.mBackgroundColor = ThemeUtils.getPrimaryColor(this);
        lrCleaText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch.getText().clear();
            }
        });
        Utils.dpToPx(20.0f);
        this.mSearch.setImeOptions(268435458);
        this.mSearch.clearFocus();
        initializeSearchSuggestions(this.mSearch);
        searchListners();

        mSearch.setOnFocusChangeListener((v, hasFocus) -> {
            isWelcomeLinkInputFocused = hasFocus;

            if (hasFocus) {
                lrRefStop.setVisibility(View.GONE);
                lrCleaText.setVisibility(View.VISIBLE);
            } else {
                lrCleaText.setVisibility(View.GONE);
                lrRefStop.setVisibility(View.VISIBLE);

            }

        });


        this.mMainSearchBar.setImeOptions(268435458);
        if (API <= 18) {
            WebIconDatabase.getInstance().open(getDir("icons", 0).getPath());
        }
        Intent intent = bundle == null ? getIntent() : null;
        if (intent != null && (intent.getFlags() & 1048576) != 0) {
            z = true;
        }
        if (Utils.isPanicTrigger(intent)) {
            setIntent(null);
            panicClean();
        } else {
            if (z) {
                intent = null;
            }
            this.mPresenter.setupTabs(intent);
            setIntent(null);
        }
    }

    @Override
    public void refreshOrStop() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            if (currentTab.getProgress() < 100) {
                currentTab.stopLoading();
            } else {
                currentTab.reload();
            }
        }
    }


    private String escapeJavaScriptString(String original) {
        return original.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    private void searchListners() {
        SearchListener searchListener = new SearchListener(this, this, this.mTabsManager, this.mSearch, this.mUiLayout, this.mSwipe);
        MainSearchListener mainSearchListener = new MainSearchListener(this, this.mMainSearchBar, this.mIconLayout);

        this.mSearch.setCompoundDrawablePadding(Utils.dpToPx(5.0f));
        this.mSearch.setOnKeyListener(searchListener);
        this.mSearch.setOnFocusChangeListener(searchListener);
        this.mSearch.setOnEditorActionListener(searchListener);
        this.mSearch.setOnPreFocusListener(searchListener);

        this.mMainSearchBar.setOnFocusChangeListener(mainSearchListener);
        this.mMainSearchBar.setOnKeyListener(mainSearchListener);
        this.mMainSearchBar.setOnEditorActionListener(mainSearchListener);

        mMainSearchBar.setOnFocusChangeListener((v, hasFocus) -> {
            isWelcomeLinkInputFocused = hasFocus;

            if (hasFocus) {
                ivSearch.setVisibility(View.GONE);


            } else {
                ivSearch.setVisibility(View.VISIBLE);

            }

        });
        this.mMainSearchBar.setOnSearchActivatedListener(() -> {
            this.mSearch.setOnKeyListener(null);
            this.mSearch.setOnFocusChangeListener(null);
            this.mSearch.setOnEditorActionListener(null);
            this.mSearch.setOnPreFocusListener(null);
        });

        this.mMainSearchBar.setOnSearchDeactivatedListener(() -> {
            this.mSearch.setOnKeyListener(searchListener);
            this.mSearch.setOnFocusChangeListener(searchListener);
            this.mSearch.setOnEditorActionListener(searchListener);
            this.mSearch.setOnPreFocusListener(searchListener);
        });
    }


    void panicClean() {
        this.mTabsManager.newTab(this, "");
        this.mTabsManager.switchToTab(0);
        this.mTabsManager.clearSavedState();
        closeBrowser();
        System.exit(1);
    }

    @Override
    public String searchtext() {
        return this.mSearchText;
    }

    @Override
    public void newtab(String str, boolean z) {
        newTab(str, z);
        this.mTabLayout.setVisibility(View.GONE);
    }

    private void initializePreferences() {
        Fragment findFragmentByTag = getSupportFragmentManager().findFragmentByTag("tabss");
        if (findFragmentByTag instanceof TabsFragment) {
            ((TabsFragment) findFragmentByTag).reinitializePreferences();
        }
        this.google = Preference.one(this);
        this.yahoo = Preference.two(this);
        this.bing = Preference.three(this);
        this.duckDuckGo = Preference.four(this);
        this.ask = Preference.five(this);
        this.baidu = Preference.six(this);
        this.yandex = Preference.seven(this);
        this.lukayn = Preference.eight(this);
        SearchEngineSetUp();
        updateCookiePreference().subscribeOn(Schedulers.worker()).subscribe();
    }


    public void fab() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle((int) R.string.set_search_engine);
        builder.setSingleChoiceItems((int) R.array.fab_choice, searchEngineInt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                searchEngineInt = i;
            }
        }).setNegativeButton((int) R.string.cancel, null).setPositiveButton((int) R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                i = searchEngineInt;
                String str = SettingsConstant.FAB_VALUE_SEVEN;
                String str2 = SettingsConstant.FAB_VALUE_SIX;
                String str3 = SettingsConstant.FAB_VALUE_FIVE;
                String str4 = SettingsConstant.FAB_VALUE_FOUR;
                String str5 = SettingsConstant.FAB_VALUE_THREE;
                String str6 = SettingsConstant.FAB_VALUE_TWO;
                String str7 = SettingsConstant.FAB_VALUE_ONE;
                String str8 = SettingsConstant.FAB_VALUE_EIGHT;
                if (i == 0) {
                    MainActivity.this.mSearchText = SettingsConstant.GOOGLE_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.google_engine);
                    Preference.savePreferences(str7, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 1) {
                    MainActivity.this.mSearchText = SettingsConstant.YAHOO_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.yahoo_engine);
                    Preference.savePreferences(str6, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 2) {
                    MainActivity.this.mSearchText = SettingsConstant.BING_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.bing_engine);
                    Preference.savePreferences(str5, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 3) {
                    MainActivity.this.mSearchText = SettingsConstant.DUCKDUCKGO_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.duckduckgo_engine);
                    Preference.savePreferences(str4, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 4) {
                    MainActivity.this.mSearchText = SettingsConstant.ASK_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.ask_engine);
                    Preference.savePreferences(str3, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 5) {
                    MainActivity.this.mSearchText = SettingsConstant.BAIDU_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.baidu_engine);
                    Preference.savePreferences(str2, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 6) {
                    MainActivity.this.mSearchText = SettingsConstant.YANDEX_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.yandex_engine);
                    Preference.savePreferences(str, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str8, assertionsDisabled, MainActivity.this.getActivity());
                } else if (searchEngineInt == 7) {
                    MainActivity.this.mSearchText = SettingsConstant.LUKAYN_ENGINE;
                    mserachEngineIons.setImageResource(R.drawable.ic_lukayn);
                    Preference.savePreferences(str8, true, MainActivity.this.getActivity());
                    Preference.savePreferences(str, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str7, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str6, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str4, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str3, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str2, assertionsDisabled, MainActivity.this.getActivity());
                    Preference.savePreferences(str5, assertionsDisabled, MainActivity.this.getActivity());
                }
            }
        });
        builder.create().show();
    }


    @Override
    public void GOOGLE_ENGINE() {

        this.mSearchText = SettingsConstant.GOOGLE_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.google_engine);
    }

    @Override
    public void YAHOO_ENGINE() {

        this.mSearchText = SettingsConstant.YAHOO_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.yahoo_engine);
    }

    @Override
    public void BING_ENGINE() {

        this.mSearchText = SettingsConstant.BING_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.bing_engine);
    }

    @Override
    public void DUCKDUCKGO_ENGINE() {

        this.mSearchText = SettingsConstant.DUCKDUCKGO_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.duckduckgo_engine);
    }

    @Override
    public void ASK_ENGINE() {

        this.mSearchText = SettingsConstant.ASK_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.ask_engine);
    }

    @Override
    public void BAIDU_ENGINE() {

        this.mSearchText = SettingsConstant.BAIDU_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.bing_engine);
    }

    @Override
    public void YANDEX_ENGINE() {

        this.mSearchText = SettingsConstant.YANDEX_ENGINE;
        mserachEngineIons.setImageResource(R.drawable.yandex_engine);
    }

    @Override
    public void LUKAYN_ENGINE() {

        this.mSearchText = SettingsConstant.LUKAYN_ENGINE;
        this.mserachEngineIons.setImageResource(R.drawable.ic_lukayn);
    }

    private void SearchEngineSetUp() {


        this.mserachEngineIons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.fab();
            }
        });
        if (this.google) {
            this.mSearchText = SettingsConstant.GOOGLE_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.google_engine);
        } else if (this.yahoo) {
            this.mSearchText = SettingsConstant.YAHOO_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.yahoo_engine);
        } else if (this.bing) {
            this.mSearchText = SettingsConstant.BING_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.bing_engine);
        } else if (this.ask) {
            this.mSearchText = SettingsConstant.ASK_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.ask_engine);
        } else if (this.baidu) {
            this.mSearchText = SettingsConstant.BAIDU_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.bing_engine);
        } else if (this.duckDuckGo) {
            this.mSearchText = SettingsConstant.DUCKDUCKGO_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.duckduckgo_engine);
        } else if (this.yandex) {
            this.mSearchText = SettingsConstant.YANDEX_ENGINE;
            mserachEngineIons.setImageResource(R.drawable.yandex_engine);
        } else if (this.lukayn) {
            this.mSearchText = SettingsConstant.LUKAYN_ENGINE;
            this.mserachEngineIons.setImageResource(R.drawable.ic_lukayn);
        }
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 66) {
            if (this.mSearch.hasFocus()) {
                searchTheWeb(this.mSearch.getText().toString());
            }
        } else if (i == 82 && Build.VERSION.SDK_INT <= 16 && Build.MANUFACTURER.compareTo("LGE") == 0) {
            return true;
        } else {
            if (i == 4) {
                this.mKeyDownStartTime = System.currentTimeMillis();
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override
    public boolean onKeyUp(int i, @NonNull KeyEvent keyEvent) {
        if (i == 82 && Build.VERSION.SDK_INT <= 16 && Build.MANUFACTURER.compareTo("LGE") == 0) {
            PopupWindowTop(ShowMenuPopup);
            return true;
        } else if (i != 4 || System.currentTimeMillis() - this.mKeyDownStartTime <= ViewConfiguration.getLongPressTimeout()) {
            return super.onKeyUp(i, keyEvent);
        } else {
            return true;
        }
    }


    public void addBookmark(String str, String str2) {
        if (str2 == null || str2.isEmpty() || str == null || str.isEmpty()) {
            Utils.msg(getString(R.string.wait_until_page_loads), this);
            return;
        }
        String iconText = Utils.getIconText(str2, str);
        new BookmarksDb(str2, str, iconText, 0, Utils.getDateTime() + " " + Utils.getDateString()).save();
        Utils.msg(getString(R.string.saved_to_bookmarks), this);
    }

    public boolean isBookmarkAdded(String title, String url) {
        List<BookmarksDb> bookmarks = BookmarksDb.find(BookmarksDb.class, "title = ? AND bookmarks = ?", title, url);
        return !bookmarks.isEmpty();
    }

    @Override
    public TabsManager getTabModel() {
        return this.mTabsManager;
    }

    @Override
    public void closeapp() {
        finish();
    }

    @Override
    public void closealltabs() {
        this.mBrowserFrame.setBackgroundColor(this.mBackgroundColor);
        removeViewFromParent(this.mCurrentView);
        performExitCleanUp();
        int size = this.mTabsManager.size();
        this.mTabsManager.shutdown();
        this.mCurrentView = null;
        for (int i = 0; i < size; i++) {
            this.mTabsView.tabRemoved(0);
        }
        finish();
    }

    @Override
    public void mainuigone() {
        this.mMainUi.setVisibility(View.GONE);
        this.mUiLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyTabViewRemoved(int i) {
        String str = TAG;
        Log.d(str, "Notify Tab Removed: " + i);
        this.findInput.clearFocus();
        this.findInput.setText("");
        this.mMainUi.setVisibility(View.GONE);
        this.mUiLayout.setVisibility(View.VISIBLE);
        this.mFindInPageLayout.setVisibility(View.INVISIBLE);
        this.mTabsView.tabRemoved(i);
    }

    @Override
    public void notifyTabViewAdded() {
        Log.d(TAG, "Notify Tab Added");
        this.findInput.clearFocus();
        this.findInput.setText("");
        this.mFindInPageLayout.setVisibility(View.INVISIBLE);
        this.mTabsView.tabAdded();
    }

    @Override
    public void notifyTabViewChanged(int i) {
        String str = TAG;
        Log.d(str, "Notify Tab Changed: " + i);
        this.findInput.clearFocus();
        this.findInput.setText("");
        this.mFindInPageLayout.setVisibility(View.INVISIBLE);
        this.mTabsView.tabChanged(i);
    }

    @Override
    public void notifyTabViewInitialized() {
        Log.d(TAG, "Notify Tabs Initialized");
        this.mTabsView.tabsInitialized();
    }

    @Override
    public void tabChanged(com.rexvit.browser.view.BrowserView browserView) {
        this.mPresenter.tabChangeOccurred(browserView);
    }

    @Override
    public void removeTabView() {
        Log.d(TAG, "Remove the tab view");
        this.mBrowserFrame.setBackgroundColor(this.mBackgroundColor);
        removeViewFromParent(this.mCurrentView);
        this.mCurrentView = null;
    }

    @Override
    public void setTabView(@NonNull View view) {
        if (this.mCurrentView == view) {
            return;
        }
        this.mBrowserFrame.setBackgroundColor(this.mBackgroundColor);
        removeViewFromParent(view);
        removeViewFromParent(this.mCurrentView);
        this.mBrowserFrame.addView(view, 0, MATCH_PARENT);
        view.requestFocus();
        this.mCurrentView = view;
        showActionBar();
    }

    @Override
    public void showBlockedLocalFileDialog(DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(this).setTitle(R.string.title_warning).setMessage(R.string.message_blocked_local).setPositiveButton(getString(R.string.open), onClickListener).setNegativeButton(getString(R.string.cancel), null).show();
    }

    @Override
    public void tabCloseClicked(int i) {
        this.mPresenter.deleteTab(i);
    }

    @Override
    public void tabClicked(int i) {
        showTab(i);
        mainuigone();
    }

    @Override
    public void newTabButtonClicked() {
        this.mMainSearchBar.requestFocus();
        this.mMainUi.setVisibility(View.VISIBLE);
        this.mUiLayout.setVisibility(View.GONE);
    }

    private synchronized void showTab(int i) {
        this.mTabsManager.switchToTab(i);
        this.mTabLayout.setVisibility(View.GONE);
        this.mPresenter.tabChanged(i);
    }

    void handleNewIntent(Intent intent) {
        if (this.mTabLayout.getVisibility() == View.VISIBLE) {
            this.mTabLayout.setVisibility(View.GONE);
        }
        this.mPresenter.onNewIntent(intent);
    }

    @Override
    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        if (i <= 60 || Build.VERSION.SDK_INT >= 19) {
            return;
        }
        Log.d(TAG, "Low Memory, Free Memory");
        this.mPresenter.onAppLowMemory();
    }


    public synchronized boolean newTab(String str, boolean z) {
        if (this.mTabLayout.getVisibility() == View.VISIBLE) {
            this.mTabLayout.setVisibility(View.GONE);
        }
        this.mMainUi.setVisibility(View.GONE);
        this.mUiLayout.setVisibility(View.VISIBLE);
        return this.mPresenter.newTab(str, z);
    }

    void performExitCleanUp() {
        try {
            ClearingData.clearCache(this);
            ClearingData.removePasswords(this);
            ClearingData.removeCookies(this);
            ClearingData.removeWebStorage();
            WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(this);
            webViewDatabase.clearFormData();
            webViewDatabase.clearHttpAuthUsernamePassword();
            this.mHistoryDatabase.deleteHistory();
            if (Build.VERSION.SDK_INT < 18) {
                webViewDatabase.clearUsernamePassword();
                WebIconDatabase.getInstance().removeAllIcons();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mSuggestionsAdapter.clearCache();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Log.d(TAG, "onConfigurationChanged");
        showActionBar();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void closeBrowser() {
        this.mBrowserFrame.setBackgroundColor(this.mBackgroundColor);
        removeViewFromParent(this.mCurrentView);
        performExitCleanUp();
        int size = this.mTabsManager.size();
        this.mTabsManager.shutdown();
        this.mCurrentView = null;
        for (int i = 0; i < size; i++) {
            this.mTabsView.tabRemoved(0);
        }
        this.mTabLayout.setVisibility(View.GONE);
        this.mMainUi.setVisibility(View.VISIBLE);
        this.mUiLayout.setVisibility(View.GONE);
    }

    @Override
    public synchronized void onBackPressed() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            Log.d(TAG, "onBackPressed");
            if (this.mSearch.hasFocus()) {
                currentTab.requestFocus();

            } else if (this.mTabLayout.getVisibility() == View.VISIBLE && this.mMainUi.getVisibility() == View.GONE) {
                this.mTabLayout.setVisibility(View.GONE);
            } else if (this.mFindInPageLayout.getVisibility() == View.VISIBLE) {
                this.mFindInPageLayout.setVisibility(View.INVISIBLE);
            } else if (this.mMainUi.getVisibility() == View.VISIBLE && this.num > 0) {
                this.mUiLayout.setVisibility(View.VISIBLE);
                this.mMainUi.setVisibility(View.GONE);
            } else if (currentTab.canGoBack()) {
                if (!currentTab.isShown()) {
                    onHideCustomView();
                } else {
                    currentTab.goBack();
                }
            } else {
                if (this.mCustomView == null && this.mCustomViewCallback == null) {
                    this.mPresenter.deleteTab(this.mTabsManager.positionOf(currentTab));
                }
                onHideCustomView();
            }
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            finish();
            Toast.makeText(this, R.string.data_cleared, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.mSearch.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(this.mMainSearchBar.getWindowToken(), 0);
        this.mTabsManager.pauseAll();
        try {
            BrowserApp.get(this).unregisterReceiver(this.mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver was not registered", e);
        }
        saveOpenTabs();
        this.mEventBus.unregister(this.mBusEventListener);
    }

    void saveOpenTabs() {
        if (Preference.closeTabs(this)) {
            return;
        }
        this.mTabsManager.saveState();
    }

    @Override
    protected void onDestroy() {
        this.mPresenter.shutdown();
        performExitCleanUp();
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        this.mTabsManager.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "Downloads"))));
        this.mTabsManager.resumeAll(this);
        initializePreferences();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NETWORK_BROADCAST_ACTION);
        BrowserApp.get(this).registerReceiver(this.mNetworkReceiver, intentFilter);
        this.mEventBus.register(this.mBusEventListener);
        showActionBar();
    }

    @Override
    public void serachWebUrl(String str) {
        searchTheWeb(str);
    }


    public void searchTheWeb(@NonNull String str) {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (str.isEmpty()) {
            return;
        }
        String str2 = this.mSearchText + UrlUtils.QUERY_PLACE_HOLDER;
        String trim = str.trim();
        if (currentTab != null) {
            currentTab.stopLoading();
            this.mPresenter.loadUrlInCurrentView(UrlUtils.smartUrlFilter(trim, true, str2));
            this.mSearch.clearFocus();
        }
    }

    @Override
    public void updateUrl(@Nullable String str, boolean z) {
        SearchView searchView;
        if (str == null || (searchView = this.mSearch) == null || searchView.hasFocus()) {
            return;
        }
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (z) {
            if (currentTab != null && !currentTab.getTitle().isEmpty()) {
                this.mSearch.setText(currentTab.getTitle());
                return;
            } else {
                this.mSearch.setText(Utils.getDomainName(str));
                return;
            }
        }
        this.mSearch.setText(Utils.getDomainName(str));
    }

    @Override
    public void updateTabNumber(int i) {
        this.num = i;
        if (this.num <= 0) {
            this.mUiLayout.setVisibility(View.GONE);
            this.mMainUi.setVisibility(View.VISIBLE);
            this.mNotifIcon2.setVisibility(View.GONE);
        } else {
            this.mUiLayout.setVisibility(View.VISIBLE);
            this.mMainUi.setVisibility(View.GONE);
            this.mNotifIcon2.setVisibility(View.VISIBLE);
        }
        if (i > 99) {
            this.mMainTextNotif.setText("99+");
            this.mMainTextNotif2.setText("99+");
            return;
        }
        TextView textView = this.mMainTextNotif;
        textView.setText(i + "");
        TextView textView2 = this.mMainTextNotif2;
        textView2.setText(i + "");
    }

    @Override
    public void updateProgress(int i) {
        this.mProgressBar.setProgress(i);
        if (mProgressBar.getProgress() < 100) {
            ivRefStop.setImageResource(R.drawable.ic_stop_or_close);

        } else {
            ivRefStop.setImageResource(R.drawable.ic_refresh);

        }
    }

    private void initializeSearchSuggestions(final AutoCompleteTextView autoCompleteTextView) {
        this.mSuggestionsAdapter = new SuggestionsAdapter(this);
        autoCompleteTextView.setDropDownWidth(-1);
        autoCompleteTextView.setDropDownAnchor(R.id.toolbar_layout);
        autoCompleteTextView.setDropDownVerticalOffset(0);
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, j) -> {
            CharSequence text;
            CharSequence text2 = ((TextView) view.findViewById(R.id.url)).getText();
            String charSequence = text2 != null ? text2.toString() : null;
            if (charSequence == null && (text = ((TextView) view.findViewById(R.id.title)).getText()) != null) {
                charSequence = text.toString();
            }
            if (charSequence == null) {
                return;
            }
            autoCompleteTextView.setText(charSequence);
            MainActivity.this.searchTheWeb(charSequence);
            ((InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
            MainActivity.this.mPresenter.onAutoCompleteItemPressed();
        });
        autoCompleteTextView.setSelectAllOnFocus(true);
        autoCompleteTextView.setAdapter(this.mSuggestionsAdapter);
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> valueCallback) {
        this.mUploadMessage = valueCallback;
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.open)), 1);
    }

    @Override
    protected void onActivityResult(int i, final int i2, final Intent intent) {
        Uri[] uriArr = new Uri[0];
        if (i == 177) {
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mActivity, new String[]{"android.permission.RECORD_AUDIO"}, new PermissionsResultAction() {
                @Override
                public void onDenied(String str) {
                }

                @Override
                public void onGranted() {
                    Intent intent2;
                    if (i2 != -1 || (intent2 = intent) == null) {
                        return;
                    }
                    MainActivity.this.openTabFromVoice(intent2.getStringArrayListExtra("android.speech.extra.RESULTS").get(0));
                }
            });
            return;
        }
        if (API < 21 && i == 1) {
            if (this.mUploadMessage == null) {
                return;
            }
            this.mUploadMessage.onReceiveValue((intent == null || i2 != -1) ? null : intent.getData());
            this.mUploadMessage = null;
        }
        if (i != 1 || this.mFilePathCallback == null) {
            super.onActivityResult(i, i2, intent);
            return;
        }
        if (i2 == -1) {
            if (intent == null) {
                String str = this.mCameraPhotoPath;
                if (str != null) {
                    uriArr = new Uri[]{Uri.parse(str)};
                }
            } else {
                String dataString = intent.getDataString();
                if (dataString != null) {
                    uriArr = new Uri[]{Uri.parse(dataString)};
                }
            }
            this.mFilePathCallback.onReceiveValue(uriArr);
            this.mFilePathCallback = null;
        }
        uriArr = null;
        this.mFilePathCallback.onReceiveValue(uriArr);
        this.mFilePathCallback = null;
    }

    @Override
    public void openTabFromVoice(String str) {
        if (this.google) {
            newTab(SettingsConstant.GOOGLE_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.yahoo) {
            newTab(SettingsConstant.YAHOO_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.bing) {
            newTab(SettingsConstant.BING_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.ask) {
            newTab(SettingsConstant.ASK_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.baidu) {
            newTab(SettingsConstant.BAIDU_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.duckDuckGo) {
            newTab(SettingsConstant.DUCKDUCKGO_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.yandex) {
            newTab(SettingsConstant.YANDEX_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        } else if (this.lukayn) {
            newTab(SettingsConstant.LUKAYN_ENGINE + str.replace(" ", "%20").replace("+", "%2B"), true);
        }
    }

    @Override
    public void showFileChooser(ValueCallback<Uri[]> valueCallback) {
        File createImageFile;
        StringBuilder stringBuilder;
        Parcelable[] parcelableArr = new Parcelable[0];
        Throwable th;
        ValueCallback valueCallback2 = this.mFilePathCallback;
        this.mFilePathCallback = valueCallback;
        if (valueCallback2 != null) {
            valueCallback2.onReceiveValue(null);
        }
        this.mFilePathCallback = valueCallback;

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        Intent var3 = new Intent("android.media.action.IMAGE_CAPTURE");
        Intent var6 = var3;
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                createImageFile = Utils.createImageFile();
                intent.putExtra("PhotoPath", this.mCameraPhotoPath);
            } catch (IOException e2) {
                th = e2;
                createImageFile = null;
                Log.e(TAG, "Unable to create Image File", th);
                if (createImageFile != null) {
                    intent = null;
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("file:");
                    stringBuilder.append(createImageFile.getAbsolutePath());
                    this.mCameraPhotoPath = stringBuilder.toString();
                    intent.putExtra("output", Uri.fromFile(createImageFile));
                }
                Intent intent2 = new Intent("android.intent.action.GET_CONTENT");
                intent2.addCategory("android.intent.category.OPENABLE");
                intent2.setType("*/*");
                if (valueCallback != null) {
                }
                Intent intent1 = new Intent("android.intent.action.CHOOSER");
                intent1.putExtra("android.intent.extra.INTENT", intent2);
                intent1.putExtra("android.intent.extra.TITLE", "Image Chooser");
                intent1.putExtra("android.intent.extra.INITIAL_INTENTS", parcelableArr);
                startActivityForResult(intent1, 1);
            }
            if (createImageFile != null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("file:");
                stringBuilder.append(createImageFile.getAbsolutePath());
                this.mCameraPhotoPath = stringBuilder.toString();
                intent.putExtra("output", Uri.fromFile(createImageFile));
            } else {
                intent = null;
            }
        }
        Intent intent22 = new Intent("android.intent.action.GET_CONTENT");

        intent22.addCategory("android.intent.category.OPENABLE");
        intent22.setType("*/*");
        parcelableArr = intent22 != null ? new Parcelable[]{intent22} : new Parcelable[0];
        intent22 = new Intent("android.intent.action.CHOOSER");
        intent22.putExtra("android.intent.extra.INTENT", intent22);
        intent22.putExtra("android.intent.extra.TITLE", "Image Chooser");
        intent22.putExtra("android.intent.extra.INITIAL_INTENTS", parcelableArr);
        startActivityForResult(intent22, 1);
    }


    @Override
    public synchronized void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback) {
        onShowCustomView(view, customViewCallback, getRequestedOrientation());
    }

    @SuppressLint("ResourceType")
    @Override
    @TargetApi(23)
    public synchronized void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback, int i) {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (view == null || this.mCustomView != null) {
            if (customViewCallback != null) {
                try {
                    customViewCallback.onCustomViewHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding custom view", e);
                }
            }
            return;
        }
        try {
            view.setKeepScreenOn(true);
        } catch (SecurityException unused) {
            Log.e(TAG, "WebView is not allowed to keep the screen on");
        }
        this.mCustomViewCallback = customViewCallback;
        this.mCustomView = view;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        FrameLayout frameLayout = (FrameLayout) getWindow().getDecorView();
        this.mFullscreenContainer = new FrameLayout(this);
        this.mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(this, 17170444));
        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                this.mVideoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                this.mVideoView.setOnErrorListener(new VideoCompletionListener(this));
                this.mVideoView.setOnCompletionListener(new VideoCompletionListener(this));
            }
        } else if (view instanceof VideoView) {
            this.mVideoView = (VideoView) view;
            this.mVideoView.setOnErrorListener(new VideoCompletionListener(this));
            this.mVideoView.setOnCompletionListener(new VideoCompletionListener(this));
        }
        frameLayout.addView(this.mFullscreenContainer, COVER_SCREEN_PARAMS);
        this.mFullscreenContainer.addView(this.mCustomView, COVER_SCREEN_PARAMS);
        frameLayout.requestLayout();
        setFullscreen(true, true);
        if (currentTab != null) {
            currentTab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onHideCustomView() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (this.mCustomView == null || this.mCustomViewCallback == null || currentTab == null) {
            WebChromeClient.CustomViewCallback customViewCallback = this.mCustomViewCallback;
            if (customViewCallback != null) {
                try {
                    customViewCallback.onCustomViewHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding custom view", e);
                }
                this.mCustomViewCallback = null;
                return;
            }
            return;
        }
        Log.d(TAG, "onHideCustomView");
        currentTab.setVisibility(View.VISIBLE);
        try {
            this.mCustomView.setKeepScreenOn(false);
        } catch (SecurityException unused) {
            Log.e(TAG, "WebView is not allowed to keep the screen on");
        }
        FrameLayout frameLayout = this.mFullscreenContainer;
        if (frameLayout != null) {
            ViewGroup viewGroup = (ViewGroup) frameLayout.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(this.mFullscreenContainer);
            }
            this.mFullscreenContainer.removeAllViews();
        }
        this.mFullscreenContainer = null;
        this.mCustomView = null;
        if (this.mVideoView != null) {
            Log.d(TAG, "VideoView is being stopped");
            this.mVideoView.stopPlayback();
            this.mVideoView.setOnErrorListener(null);
            this.mVideoView.setOnCompletionListener(null);
            this.mVideoView = null;
        }
        WebChromeClient.CustomViewCallback customViewCallback2 = this.mCustomViewCallback;
        if (customViewCallback2 != null) {
            try {
                customViewCallback2.onCustomViewHidden();
            } catch (Exception e2) {
                Log.e(TAG, "Error hiding custom view", e2);
            }
        }
        this.mCustomViewCallback = null;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setFullscreen(false, false);
    }

    @Override
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
    }


    public void back() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            if (currentTab.canGoBack()) {
                currentTab.goBack();
            } else {
                Toast.makeText(mActivity, R.string.no_more_tabs, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void forward() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            if (currentTab.canGoForward()) {
                currentTab.goForward();

            } else {

                Toast.makeText(mActivity, R.string.no_more_tabs, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void desktopSet() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.desktopSet();
        }
    }

    @Override
    public void phoneSet() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.phoneSet();
        }
    }

    @Override
    public void imageOnSet() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.imageOnSet();
        }
    }

    @Override
    public void imageOffSet() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.imageOffSet();
        }
    }

    @Override
    public void reloadPage() {
        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.reload();
        }
    }

    @TargetApi(19)
    private void setFullscreen(boolean z, boolean z2) {
        Window window = getWindow();
        View decorView = window.getDecorView();
        if (z) {
            if (z2) {
                decorView.setSystemUiVisibility(5894);
            } else {
                decorView.setSystemUiVisibility(0);
            }
            window.setFlags(1024, 1024);
            return;
        }
        window.clearFlags(1024);
        decorView.setSystemUiVisibility(0);

    }

    @Override
    public synchronized void onCreateWindow(Message message) {
        com.rexvit.browser.view.BrowserView tabAtPosition;
        WebView webView;
        if (message == null) {
            return;
        }
        if (newTab("", true) && (tabAtPosition = this.mTabsManager.getTabAtPosition(this.mTabsManager.size() - 1)) != null && (webView = tabAtPosition.getWebView()) != null) {
            ((WebView.WebViewTransport) message.obj).setWebView(webView);
            message.sendToTarget();
        }
    }

    @Override
    public void onCloseWindow(com.rexvit.browser.view.BrowserView browserView) {
        this.mPresenter.deleteTab(this.mTabsManager.positionOf(browserView));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (write && read) {

                    finish();

                } else {
                    requestPermission();


                }

            }

        }
    }

    public void openTabsMainScreen() {
        showtablayout();
    }


    @Override
    protected void onNewIntent(final Intent intent) {
        if (intent.getStringExtra("url") != null) {
            mPresenter.loadUrlInCurrentView(intent.getStringExtra("url"));


        }
        if (Utils.isPanicTrigger(intent)) {
            panicClean();
            return;
        }
        new Handler().postDelayed(() -> MainActivity.this.handleNewIntent(intent), 1000L);
        super.onNewIntent(intent);
    }


    public void PopupWindowTop(View view) {
        SwitchCompat mAdBlock;
        TextView mAdsCount;
        SwitchCompat mCloseTabs;
        SwitchCompat mDesktopSwith;
        SwitchCompat mLoadImages;
        TextView mSetSearchenginetext;


        LayoutInflater toolsInflater = LayoutInflater.from(this);
        View popupView = toolsInflater.inflate(R.layout.pop_window_tools_top, null);

        toolsPopWindowTop = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        toolsPopWindowTop.setOutsideTouchable(true);
        toolsPopWindowTop.setFocusable(true);
        toolsPopWindowTop.showAtLocation(popupView, Gravity.TOP | Gravity.END, 0, 0);


        mAdBlock = popupView.findViewById(R.id.adBlock);
        mAdsCount = popupView.findViewById(R.id.adsCount);
        mCloseTabs = popupView.findViewById(R.id.closetabsSwitch);
        mDesktopSwith = popupView.findViewById(R.id.desktopSwith);
        mLoadImages = popupView.findViewById(R.id.loadImages);
        mSetSearchenginetext = popupView.findViewById(R.id.setSearchenginetext);
        this.mLoadImages = mLoadImages;
        this.mAdBlock = mAdBlock;
        this.mCloseTabs = mCloseTabs;
        this.mDesktopSwith = mDesktopSwith;
        this.mAdsCount = mAdsCount;
        this.mSetSearchenginetext = mSetSearchenginetext;
        LinearLayout lrAddToHome = popupView.findViewById(R.id.lrAddToHome);
        LinearLayout lrQuickAccess = popupView.findViewById(R.id.lrAddToQuickAccess);
        LinearLayout searchEngineClick = popupView.findViewById(R.id.searchEngineClick);

        ImageView goBack = popupView.findViewById(R.id.ivBackward);
        ImageView btnDown = popupView.findViewById(R.id.btnDown);

        ImageView goForward = popupView.findViewById(R.id.ivForward);

        ImageView mRefreshpage = popupView.findViewById(R.id.ivRefreshing);
        LinearLayout offline_page = popupView.findViewById(R.id.lrFind_on_page);
        ImageView add_bookmarks = popupView.findViewById(R.id.add_bookmarks);
        ImageButton page_info = popupView.findViewById(R.id.page_info);
        LinearLayout action_new_Tab = popupView.findViewById(R.id.action_new_Tab);
        LinearLayout action_home = popupView.findViewById(R.id.action_home);
        LinearLayout lrCapture_screen = popupView.findViewById(R.id.lrCapture_screen);
        LinearLayout translate = popupView.findViewById(R.id.translate);
        LinearLayout exit_menu2 = popupView.findViewById(R.id.exit_menu2);
        LinearLayout download = popupView.findViewById(R.id.lrDownload);
        LinearLayout action_share = popupView.findViewById(R.id.action_share);
        LinearLayout help = popupView.findViewById(R.id.help);
        LinearLayout lrShow_bookmarks = popupView.findViewById(R.id.lrShow_bookmarks);
        LinearLayout settings = popupView.findViewById(R.id.settings);
        LinearLayout theme = popupView.findViewById(R.id.lrTheme);
        LinearLayout find_on_page = popupView.findViewById(R.id.lrFind_on_page);

        theme.setOnClickListener(this);
        help.setOnClickListener(this);

        com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            if (currentTab.canGoForward()) {
                goForward.setColorFilter(ContextCompat.getColor(this, R.color.mColorPrimary), PorterDuff.Mode.SRC_IN);

            } else {
                goForward.setColorFilter(ContextCompat.getColor(this, R.color.tint_off), PorterDuff.Mode.SRC_IN);


            }
        }
        if (mProgressBar.getProgress() < 100) {
            mRefreshpage.setImageResource(R.drawable.ic_stop_or_close);

        } else {
            mRefreshpage.setImageResource(R.drawable.ic_refresh);

        }
        action_home.setOnClickListener(this);
        btnDown.setOnClickListener(this);
        searchEngineClick.setOnClickListener(this);
        mRefreshpage.setOnClickListener(this);
        goForward.setOnClickListener(this);
        translate.setOnClickListener(this);
        goBack.setOnClickListener(this);
        offline_page.setOnClickListener(this);
        add_bookmarks.setOnClickListener(this);
        page_info.setOnClickListener(this);
        action_new_Tab.setOnClickListener(this);
        lrAddToHome.setOnClickListener(this);
        lrQuickAccess.setOnClickListener(this);
        lrShow_bookmarks.setOnClickListener(this);
        lrCapture_screen.setOnClickListener(this);
        download.setOnClickListener(this);
        action_share.setOnClickListener(this);
        find_on_page.setOnClickListener(this);
        exit_menu2.setOnClickListener(this);
        settings.setOnClickListener(this);
        help.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= 32) {
            lrCapture_screen.setVisibility(View.GONE);
        }
        adBlock(this.mAdBlock, this);
        closeTabs(this.mCloseTabs, mActivity);
        loadImages(this.mLoadImages, mActivity);
        desktop(this.mDesktopSwith, mActivity);
        searchEngineText();
        long count = AdBlockDb.count(AdBlockDb.class, null, null);
        String str = count > 1 ? " ads blocked" : " ad blocked";
        String format = Utils.format(count);
        TextView textView = this.mAdsCount;
        textView.setText(format + str);
        toolsPopWindowTop.showAsDropDown(view);
    }

    @Override
    public void onClick(View v) {
        final com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        final String url = currentTab != null ? currentTab.getUrl() : null;
        int id = v.getId();
        if (id == R.id.lrDownload) {
            startActivity(new Intent(this, DownloadsActivity.class));
            toolsPopWindowTop.dismiss();
            firstPopWindowTop.dismiss();
        } else if (id == R.id.settings) {
            toolsPopWindowTop.dismiss();
            try {
                SettingsBottomSheetFragment addPhotoBottomDialogFragment =
                        SettingsBottomSheetFragment.newInstance();
                addPhotoBottomDialogFragment.show(this.getSupportFragmentManager(),
                        SettingsBottomSheetFragment.TAG);

            } catch (Exception e2) {
                e2.printStackTrace();
            }

        } else if (id == R.id.settingsF) {
            firstPopWindowTop.dismiss();

            try {
                SettingsBottomSheetFragment addPhotoBottomDialogFragment =
                        SettingsBottomSheetFragment.newInstance();
                addPhotoBottomDialogFragment.show(this.getSupportFragmentManager(),
                        SettingsBottomSheetFragment.TAG);

            } catch (Exception e2) {
                e2.printStackTrace();
            }

        } else if (id == R.id.lrAddToHome) {
            MainActivity.this.makeShortcut(currentTab);
            toolsPopWindowTop.dismiss();
        } else if (id == R.id.lrFind_on_page) {
            MainActivity.this.openFindInPage();
            toolsPopWindowTop.dismiss();
        } else if (id == R.id.lrRefStop) {
            MainActivity.this.refreshOrStop();
        } else if (id == R.id.lrShow_bookmarks) {
//            InterstitialAdShow.getInstance(this).showInterstitialAd(this, () -> {
//                firstPopWindowTop.dismiss();
//                toolsPopWindowTop.dismiss();
//                startActivity(new Intent(this,BookmarksActivity.class));
//
//            });
            firstPopWindowTop.dismiss();
            toolsPopWindowTop.dismiss();
            startActivity(new Intent(this, BookmarksActivity.class));

        } else if (id == R.id.lrCapture_screen) {
            toolsPopWindowTop.dismiss();
            try {
                MainActivity.this.captureScreen(currentTab);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, R.string.toastAn13, Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }

        } else if (id == R.id.btnDown) {
            startActivity(new Intent(this, DownloadsActivity.class));
            toolsPopWindowTop.dismiss();

        } else if (id == R.id.translate) {
//            InterstitialAdShow.getInstance(this).showInterstitialAd(this, () -> {
//
//                MainActivity mainActivity = MainActivity.this;
//                mainActivity.translatePage(currentTab);
//                toolsPopWindowTop.dismiss();
//            });

            MainActivity mainActivity = MainActivity.this;
            mainActivity.translatePage(currentTab);
            toolsPopWindowTop.dismiss();

        } else if (id == R.id.ivRefreshing) {
            toolsPopWindowTop.dismiss();
            MainActivity.this.refreshOrStop();

        } else if (id == R.id.add_bookmarks) {


            if (url != null) {
                MainActivity.this.addBookmark(currentTab.getTitle(), url);
                toolsPopWindowTop.dismiss();
            } else {
                Utils.msg(getString(R.string.bookmark_already_added), this);
            }


        } else if (id == R.id.lrAddToQuickAccess) {
            if (url != null) {
                String currentUrl = currentTab.getUrl();
                String currentPageTitle = currentTab.getTitle();
                String baseUrl = currentUrl.replaceFirst("/[^/]*$", "");
                String iconUrl = baseUrl + "/favicon.ico";


                for (WebPage page : webPages) {
                    if (page.getUrl().equals(currentUrl)) {
                        Utils.msg(getString(R.string.page_already_added), this);
                        return;
                    }
                }

                if (iconUrl == null || iconUrl.isEmpty()) {
                    iconUrl = "android.resource://" + getPackageName() + "/" + R.drawable.ic_default_page;
                }

                WebPage newPage = new WebPage(currentPageTitle, currentUrl, iconUrl);
                webPages.add(newPage);
                adapter.notifyItemInserted(webPages.size() - 1);
                Utils.msg(getString(R.string.page_exists), this);
                saveWebPages(webPages);
                toolsPopWindowTop.dismiss();
            } else {
                toolsPopWindowTop.dismiss();
                Utils.msg(getString(R.string.page_already_added), this);
            }


        } else if (id == R.id.ivForward) {
            toolsPopWindowTop.dismiss();
            MainActivity.this.forward();
        } else if (id == R.id.ivBackward) {
            toolsPopWindowTop.dismiss();
            MainActivity.this.back();
        } else if (id == R.id.action_new_Tab) {
            toolsPopWindowTop.dismiss();
            MainActivity.this.mEventBus.post(new BrowserEvents.OpenUrlInNewTab(" "));

        } else if (id == R.id.action_home) {
            toolsPopWindowTop.dismiss();
            this.mUiController.newTabButtonClicked();

        } else if (id == R.id.action_share) {
            String str = url;
            if (str != null) {
                Task.ShareUrl(MainActivity.this, str);
                toolsPopWindowTop.dismiss();
                return;
            }
        } else if (id == R.id.exit_menu2) {
            MainActivity.this.finish();
            Toast.makeText(MainActivity.this, R.string.data_cleared, Toast.LENGTH_SHORT).show();
        } else if (id == R.id.searchEngineClick) {
//            InterstitialAdShow.getInstance(this).showInterstitialAd(this, () -> {
//                fabSetting();
//                toolsPopWindowTop.dismiss();
//            });

            fabSetting();
            toolsPopWindowTop.dismiss();

        } else if (id == R.id.action_shareF) {
            firstPopWindowTop.dismiss();
            MainActivity mainActivity3 = MainActivity.this;
            Task.ShareApp(mainActivity3, BuildConfig.APPLICATION_ID, mainActivity3.getActivity().getString(R.string.share_app_title), MainActivity.this.getActivity().getString(R.string.share_app_msg));
        } else if (id == R.id.mainScreenMenuIcon2) {

            firstScreenMenuIcon();

        } else if (id == R.id.notificationTabIcon) {
            openTabs();
        } else if (id == R.id.contact) {
            toolsPopWindowTop.dismiss();
            Task.Feedback(this);
        } else if (id == R.id.findInPageDown) {
            findDown();

        } else if (id == R.id.exitfindInPage) {
            exitFindInPage();
        } else if (id == R.id.findinPageUp) {
            findUp();
        } else if (id == R.id.notificationTabIcon2) {
            openTabsMainScreen();
        } else if (id == R.id.btn_menuPopup) {
            PopupWindowTop(ShowMenuPopup);
        } else if (id == R.id.help) {

            toolsPopWindowTop.dismiss();
            Task.Feedback(this);
        } else if (id == R.id.lrTheme) {
            firstPopWindowTop.dismiss();
            toolsPopWindowTop.dismiss();
            onThemeSettingsClick(v);

        }


    }

    private List<WebPage> createDefaultPages() {
        List<WebPage> defaultPages = new ArrayList<>();
        defaultPages.add(new WebPage("X", "https://www.twitter.com", "android.resource://" + getPackageName() + "/" + R.drawable.twitter_x_logo));
        defaultPages.add(new WebPage("Facebook", "https://www.facebook.com", "android.resource://" + getPackageName() + "/" + R.drawable.facebook));
        defaultPages.add(new WebPage("Instagram", "https://www.instagram.com", "android.resource://" + getPackageName() + "/" + R.drawable.instagram));
        defaultPages.add(new WebPage("YouTube", "https://www.youtube.com", "android.resource://" + getPackageName() + "/" + R.drawable.youtube));
        defaultPages.add(new WebPage("TikTok", "https://www.tiktok.com", "android.resource://" + getPackageName() + "/" + R.drawable.tiktok));
        defaultPages.add(new WebPage("Pinterest", "https://www.pinterest.com", "android.resource://" + getPackageName() + "/" + R.drawable.pinterest));
        defaultPages.add(new WebPage("LinkedIn", "https://www.linkedin.com", "android.resource://" + getPackageName() + "/" + R.drawable.linkedin));

        return defaultPages;
    }


    public void fabSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.set_search_engine);
        builder.setSingleChoiceItems(R.array.fab_choice, searchEngineInt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int unused = searchEngineInt = i;
            }
        }).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (searchEngineInt == 0) {
                    mUiController.GOOGLE_ENGINE();
                    mSetSearchenginetext.setText("Google");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (searchEngineInt == 1) {
                    mUiController.YAHOO_ENGINE();
                    mSetSearchenginetext.setText("Yahoo");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (searchEngineInt == 2) {
                    mUiController.BING_ENGINE();
                    mSetSearchenginetext.setText("Bing");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (searchEngineInt == 3) {
                    mUiController.DUCKDUCKGO_ENGINE();
                    mSetSearchenginetext.setText("DuckDuckGo");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (MainActivity.searchEngineInt == 4) {
                    mUiController.ASK_ENGINE();
                    mSetSearchenginetext.setText("Ask");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (searchEngineInt == 5) {
                    mUiController.BAIDU_ENGINE();
                    mSetSearchenginetext.setText("Baidu");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (searchEngineInt == 6) {
                    mUiController.YANDEX_ENGINE();
                    mSetSearchenginetext.setText("Yandex");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, false, mActivity);
                } else if (searchEngineInt == 7) {
                    mUiController.LUKAYN_ENGINE();
                    mSetSearchenginetext.setText("Lukayn");
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_EIGHT, true, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SEVEN, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_ONE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_TWO, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FOUR, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_FIVE, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_SIX, false, mActivity);
                    Preference.savePreferences(SettingsConstant.FAB_VALUE_THREE, false, mActivity);
                }
            }
        });
        builder.create().show();
    }

    private static void closeTabs(final SwitchCompat switchCompat, final Context context) {
        switchCompat.setChecked(Preference.closeTabs(context));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (compoundButton.isChecked()) {
                    Preference.savePreferences(SettingsConstant.CLOSE_TABS, true, context);
                    toolsPopWindowTop.dismiss();
                } else {
                    Preference.savePreferences(SettingsConstant.CLOSE_TABS, false, context);
                    toolsPopWindowTop.dismiss();


                }
            }
        });
    }

    private void adBlock(final SwitchCompat switchCompat, Context context) {
        switchCompat.setChecked(Preference.adBlock(context));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (switchCompat.isChecked()) {
                    try {
                        AdBlock.mBlockAds = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mUiController.reloadPage();
                    Preference.savePreferences(SettingsConstant.ADBLOCK, true, mActivity);
                    toolsPopWindowTop.dismiss();
                    return;
                }
                try {
                    AdBlock.mBlockAds = false;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                mUiController.reloadPage();
                Preference.savePreferences(SettingsConstant.ADBLOCK, false, mActivity);
                toolsPopWindowTop.dismiss();
            }
        });
    }

    private void loadImages(final SwitchCompat switchCompat, Context context) {
        switchCompat.setChecked(Preference.datSaveMode(context));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (switchCompat.isChecked()) {
                    mUiController.imageOnSet();
                    mUiController.reloadPage();
                    Preference.savePreferences(SettingsConstant.SWITCH_IMAGES, true, mActivity);
                    return;
                }
                mUiController.imageOffSet();
                mUiController.reloadPage();
                Preference.savePreferences(SettingsConstant.SWITCH_IMAGES, false, mActivity);
                toolsPopWindowTop.dismiss();
            }
        });
    }

    private void desktop(final SwitchCompat switchCompat, Context context) {
        switchCompat.setChecked(Preference.desktopMode(context));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (switchCompat.isChecked()) {
                    mUiController.desktopSet();
                    mUiController.reloadPage();
                    toolsPopWindowTop.dismiss();
                    Preference.savePreferences(SettingsConstant.DESKTOP, true, mActivity);
                    return;
                }
                toolsPopWindowTop.dismiss();
                mUiController.phoneSet();
                mUiController.reloadPage();
                Preference.savePreferences(SettingsConstant.DESKTOP, false, mActivity);

            }
        });
    }

    public void searchEngineClick() {
        fabSetting();
    }

    public void searchEngineText() {
        if (Preference.one(this)) {
            this.mSetSearchenginetext.setText("Google");
        } else if (Preference.two(this)) {
            this.mSetSearchenginetext.setText("Yahoo");
        } else if (Preference.three(this)) {
            this.mSetSearchenginetext.setText("Bing");
        } else if (Preference.four(this)) {
            this.mSetSearchenginetext.setText("DuckDuckGo");
        } else if (Preference.five(this)) {
            this.mSetSearchenginetext.setText("Ask");
        } else if (Preference.six(this)) {
            this.mSetSearchenginetext.setText("Baidu");
        } else if (Preference.seven(this)) {
            this.mSetSearchenginetext.setText("Yandex");
        } else if (Preference.eight(this)) {
            this.mSetSearchenginetext.setText("Lukayn");
        }
    }

    public void firstScreenMenuIcon() {

        LayoutInflater toolsInflater = LayoutInflater.from(this);
        View popupView = toolsInflater.inflate(R.layout.pop_window_tools_first, null);

        firstPopWindowTop = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        firstPopWindowTop.setOutsideTouchable(true);
        firstPopWindowTop.setFocusable(true);
        firstPopWindowTop.showAtLocation(popupView, Gravity.TOP | Gravity.END, 0, 0);


        LinearLayout exit_menu2 = popupView.findViewById(R.id.exit_menu2);
        LinearLayout download = popupView.findViewById(R.id.lrDownload);
        LinearLayout action_share = popupView.findViewById(R.id.action_shareF);
        LinearLayout settings = popupView.findViewById(R.id.settingsF);
        LinearLayout lrShow_bookmarks = popupView.findViewById(R.id.lrShow_bookmarks);
        LinearLayout theme = popupView.findViewById(R.id.lrTheme);
        theme.setOnClickListener(this);
        lrShow_bookmarks.setOnClickListener(this);
        download.setOnClickListener(this);
        action_share.setOnClickListener(this);
        exit_menu2.setOnClickListener(this);
        settings.setOnClickListener(this);

    }

    private static void doOnLayout(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                runnable.run();
            }
        });
    }

    private void initializeToolbarHeight(final Configuration configuration) {
        doOnLayout(this.mUiLayout, new Runnable() {
            @Override
            public void run() {
                if (configuration.orientation == 1) {
                    Utils.dpToPx(56.0f);
                } else {
                    Utils.dpToPx(52.0f);
                }
            }
        });
    }


    @Override
    public void showActionBar() {
        Log.d(TAG, "showActionBar");
        final com.rexvit.browser.view.BrowserView currentTab = this.mTabsManager.getCurrentTab();
        ViewTreeObserver viewTreeObserver = this.mSwipe.getViewTreeObserver();
        ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                com.rexvit.browser.view.BrowserView browserView = currentTab;
                if ((browserView != null ? browserView.getScroll() : 0) == 0) {
                    MainActivity.this.mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            MainActivity.this.refreshOrStop();
                        }
                    });
                    MainActivity.this.mSwipe.setEnabled(true);
                    return;
                }
                MainActivity.this.mSwipe.setEnabled(false);
            }
        };
        this.mScrollListner = onScrollChangedListener;
        viewTreeObserver.addOnScrollChangedListener(onScrollChangedListener);
    }


    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            try {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);

            } catch (Exception e) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);

            }

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        }

    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(

            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {

                            MainActivity.this.finishAffinity();

                        } else {
                            Toast.makeText(MainActivity.this, "storage permission required", Toast.LENGTH_SHORT).show();


                        }
                    }

                }
            }

    );

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean isAskNotifi() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        }


        return true;
    }


    public boolean isStoragePermissionGranted() {

        boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasPermission) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    public void logthis(String msg) {
        Log.d(TAG, msg);
    }


    public void onThemeSettingsClick(View pressedButtonView) {
        SharedPreferences preferences = new AppSettings(this.app).getPreferences();
        String themeSettingsKey = SettingsKeys.THEME_SETTINGS_KEY;
        MaterialDialog dialog = DialogUtility.getDefaultBuilder(this).title(R.string.themes).customView(R.layout.dialog_theme_chosser, false).positiveText(R.string.apply_theme).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(final MaterialDialog dialog2) {
                super.onPositive(dialog2);
                MainActivity.this.setTheme(dialog2);

            }
        }).build();

        themeSystemDev = (AppCompatRadioButton) dialog.findViewById(R.id.bnt_system_theme);
        themeLight = (AppCompatRadioButton) dialog.findViewById(R.id.bnt_light_theme);
        themeDark = (AppCompatRadioButton) dialog.findViewById(R.id.bnt_dark_theme);

        String stringSys = getString(R.string.theme_def);
        String stringLight = getString(R.string.theme_light);
        String stringDark = getString(R.string.theme_dark);
        String string9 = preferences.getString(themeSettingsKey, stringSys);
        if (string9.equals(stringSys)) {
            themeSystemDev.setChecked(true);

        } else if (string9.equals(stringLight)) {
            themeLight.setChecked(true);

        } else if (string9.equals(stringDark)) {
            themeDark.setChecked(true);
        } else {
            themeSystemDev.setChecked(true);
        }
        dialog.show();
    }

    public void setTheme(MaterialDialog dialog) {
        RadioGroup themeGroup = (RadioGroup) dialog.findViewById(R.id.theme_choose_group);
        int radioButtonID = themeGroup.getCheckedRadioButtonId();
        View radioButton = themeGroup.findViewById(radioButtonID);
        int idx = themeGroup.indexOfChild(radioButton);

        if (themeGroup != null) {
            int id = themeGroup.getCheckedRadioButtonId();

            AppCompatRadioButton theme = (AppCompatRadioButton) themeGroup.getChildAt(idx);
            if (theme != null) {
                String value = theme.getText().toString();
                AppSettings appSettings = new AppSettings(this.app);
                String key = SettingsKeys.THEME_SETTINGS_KEY;
                SharedPreferences.Editor editor = appSettings.getPreferences().edit();
                editor.putString(key, value).apply();
                String msg = getString(R.string.restart_suggestion_msg);
                showSimpleHtmlMessageBox(msg, new MessageBox.OnOkListener() {
                    @Override
                    public void onClick() {
                        MainActivity.this.closeApplication();
                    }
                });


            }
        }
    }

    private String getUrlFromIntent() {
        Intent i = getIntent();
        if (i != null) {
            if (i.getData() != null)
                return i.getData().toString();
            else
                return i.getStringExtra(Intent.EXTRA_TEXT);
        }

        return null;
    }

    private void showAddPageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_page, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextPageName);
        EditText editTextUrl = dialogView.findViewById(R.id.editTextPageUrl);


        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();


        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(view -> {
                String name = editTextName.getText().toString().trim();
                String userInput = editTextUrl.getText().toString().trim();

                String validatedInput = validateAndModifyUrl(userInput);
                if (validatedInput == null) {
                    Toast.makeText(this, "Invalid URL. Please enter a valid URL or search term.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (name.isEmpty() && validatedInput.isEmpty()) {
                    Toast.makeText(this, "Please enter a name or URL.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!name.isEmpty()) {
                    addNewPage(name, validatedInput);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private String validateAndModifyUrl(String input) {
        if (input != null && !input.isEmpty()) {
            if (!input.contains(".")) {

                input = "https://www.google.com/search?q=" + Uri.encode(input);
            } else {

                int dotIndex = input.lastIndexOf('.');
                if (dotIndex == -1 || dotIndex >= input.length() - 1) {

                    return null;
                }

                if (!input.startsWith("http://") && !input.startsWith("https://") && !input.startsWith("file://")) {
                    input = "http://" + input;
                }
            }
            return input;
        }
        return null;
    }

    private void addNewPage(String name, String url) {
        if (url != null) {
            String currentUrl = url;
            String currentPageTitle = name;
            String baseUrl = currentUrl.replaceFirst("/[^/]*$", "");
            String iconUrl = baseUrl + "/favicon.ico";


            for (WebPage page : adapter.getPages()) {
                if (page.getUrl().equals(currentUrl)) {
                    Utils.msg(getString(R.string.page_already_added), this);
                    return;
                }
            }


            if (iconUrl == null || iconUrl.isEmpty()) {
                iconUrl = "android.resource://" + getPackageName() + "/" + R.drawable.ic_default_page;
            }


            WebPage newPage = new WebPage(currentPageTitle, currentUrl, iconUrl);
            adapter.getPages().add(adapter.getItemCount() - 1, newPage);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
            Utils.msg(getString(R.string.page_exists), this);
            saveWebPages(adapter.getPages());
        } else {
            Utils.msg(getString(R.string.invalid_url), this);
        }
    }

}



