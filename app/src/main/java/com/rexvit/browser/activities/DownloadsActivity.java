package com.rexvit.browser.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.CompletedAdapter;
import com.rexvit.browser.adapter.DownloadingAdapter;
import com.rexvit.browser.adapter.DownloadsPagerAdapter;
import com.rexvit.browser.database.DataUpdatedEvent;
import com.rexvit.browser.services.DownloadsService;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.view.AdvancedAppCompatEditText;
import com.rexvit.browser.view.MaterialSearchView;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Request;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


public class DownloadsActivity extends BaseActivity implements CompletedAdapter.OnCompletedDownloadClickedListener, DownloadingAdapter.OnRetryDownloadClickedListener, DownloadingAdapter.OnResumeDownloadClickedListener, DownloadingAdapter.OnPauseDownloadClickedListener, DownloadingAdapter.OnRemoveDownloadsListener, DownloadingAdapter.OnUrlChangedListener {
    private static final int RC_READ_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_CODE = 3;
    public static final String SELECTED_TAB = "com.rexvit.browser.SELECTED";
    Intent downloadIntent;
    FloatingActionButton fab;
    Download mDownload;
    int mDownloadId;
    List<Integer> mDownloadIds;
    String mNewUrl;
    DownloadsService mService;
    private TabLayout mTabLayout;
    private MaterialSearchView searchView;
    boolean shouldPauseAfterBound;
    boolean shouldRemoveAfterBound;
    boolean shouldResumeAfterBound;
    boolean shouldRetryAfterBound;
    boolean shouldUpdateAfterBound;
    private ViewPager viewPagerDownloads;
    public final String TAG = "DownloadsActivity";
    boolean mBound = false;
    @Override
    public int getLayoutResId() {
        return R.layout.activity_downloads;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() { 
        @Override 
        public final void onClick(View view) {
            addDownloadLink();
        }
    };
    TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() { 
        @Override 
        public void onTabReselected(TabLayout.Tab tab) {
        }

        @Override 
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override 
        public void onTabSelected(TabLayout.Tab tab) {
            DownloadsActivity.this.viewPagerDownloads.setCurrentItem(tab.getPosition());
        }
    };
    private ServiceConnection connection = new ServiceConnection() { 
        @Override 
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(DownloadsActivity.this.TAG, "Bound to Service");
            DownloadsActivity.this.mService = ((DownloadsService.DownloadsBinder) iBinder).getService();
            DownloadsActivity.this.mBound = true;
            if (DownloadsActivity.this.downloadIntent != null) {
                DownloadsActivity.this.mService.addNewDownload(DownloadsActivity.this.downloadIntent);
                EventBus.getDefault().post(new DataUpdatedEvent.NewDownloadAdded());
            }
            if (DownloadsActivity.this.shouldPauseAfterBound) {
                DownloadsActivity.this.mService.pauseDownload(DownloadsActivity.this.mDownloadId);
                DownloadsActivity.this.shouldPauseAfterBound = false;
            }
            if (DownloadsActivity.this.shouldResumeAfterBound) {
                DownloadsActivity.this.mService.resumeDownload(DownloadsActivity.this.mDownloadId);
                DownloadsActivity.this.shouldResumeAfterBound = false;
            }
            if (DownloadsActivity.this.shouldRetryAfterBound) {
                DownloadsActivity.this.mService.retryDownload(DownloadsActivity.this.mDownloadId);
                DownloadsActivity.this.shouldRetryAfterBound = false;
            }
            if (DownloadsActivity.this.shouldRemoveAfterBound) {
                DownloadsActivity.this.mService.removeDownload(DownloadsActivity.this.mDownloadIds);
                DownloadsActivity.this.shouldRemoveAfterBound = false;
            }
            if (DownloadsActivity.this.shouldUpdateAfterBound) {
                DownloadsActivity.this.mService.updateDownload(DownloadsActivity.this.mDownload, DownloadsActivity.this.mNewUrl);
                DownloadsActivity.this.shouldRemoveAfterBound = false;
            }
        }

        @Override 
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(DownloadsActivity.this.TAG, "Disconnected form service");
            DownloadsActivity.this.mBound = false;
        }
    };

    Toolbar toolbar;


    @Override
    protected void onExit() {
        finish();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_downloads);
        this.searchView = (MaterialSearchView) findViewById(R.id.search_view);
        this.mTabLayout = (TabLayout) findViewById(R.id.tab_layout_browse_pdf);
        this.viewPagerDownloads = (ViewPager) findViewById(R.id.viewpager_downloads);
        this.fab = (FloatingActionButton) findViewById(R.id.fab);
        this.toolbar= findViewById(R.id.toolbar_downloads);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setTitle(getString(R.string.downloads));
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);

        }


        TabLayout tabLayout = this.mTabLayout;
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.downloading)));
        TabLayout tabLayout2 = this.mTabLayout;
        tabLayout2.addTab(tabLayout2.newTab().setText(getString(R.string.completed)));
        this.viewPagerDownloads.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(this.mTabLayout));
        this.mTabLayout.addOnTabSelectedListener(this.tabSelectedListener);
        this.fab.setOnClickListener(this.onClickListener);
        String str = this.TAG;
        Log.d(str, Environment.getExternalStorageDirectory() + "/");
        if (Build.VERSION.SDK_INT < 30 && ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            requestStoragePermission();
            return;
        }
        initializeAdapter();
        Intent intent = getIntent();
        if (TextUtils.isEmpty(intent.getStringExtra(NewDownloadActivity.DOWNLOAD_URL))) {
            return;
        }
        addNewDownload(intent);
    }

    @Override 
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 2 && iArr.length >= 1 && iArr[0] == 0) {
            initializeAdapter();
            Log.d(this.TAG, "Permission read External storage permission granted");
            return;
        }
        Log.d(this.TAG, "Permission read External storage permission not granted");
        new MaterialAlertDialogBuilder(this).setTitle(R.string.app_name).setMessage(R.string.exit_app_has_no_permission).setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { 
            @Override 
            public final void onClick(DialogInterface dialogInterface, int i2) {
                finish();
            }
        }).show();
    }


    @Override 
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 3 && i2 == -1) {
            addNewDownload(intent);
        }
    }

    @Override 
    public void onBackPressed() {
        if (this.searchView.isSearchOpen()) {
            this.searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override 
    public void onStop() {
        if (this.mBound) {
            unbindService(this.connection);
            this.mBound = false;
            Log.d(this.TAG, "Unbound to SweepService");
        }
        super.onStop();
    }

    @Override 
    public void onCompletedDownloadClickedListener(Download download) {
        FileUtils.openFile(this, download.getFile());
    }

    public void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 2);
    }

    public void addDownloadLink() {
        startActivityForResult(new Intent(this, NewDownloadActivity.class), 3);
    }

    private void addNewDownload(Intent intent) {
        this.downloadIntent = intent;
        if (this.mBound) {
            this.mService.addNewDownload(intent);
            EventBus.getDefault().post(new DataUpdatedEvent.NewDownloadAdded());
            return;
        }
        createAndBoundToService();
        Log.d(this.TAG, "createAndBoundToService()");
    }

    public void initializeAdapter() {
        this.viewPagerDownloads.setAdapter(new DownloadsPagerAdapter(getSupportFragmentManager()));
    }

    private void createAndBoundToService() {
        Intent intent = new Intent(this, DownloadsService.class);
        ContextCompat.startForegroundService(this, intent);
        bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
    }

    @Override 
    public void onRetryDownloadClicked(int i) {
        retryDownload(i);
    }

    @Override 
    public void onResumeDownloadClicked(int i) {
        resumeDownload(i);
    }

    @Override 
    public void onPauseDownloadClicked(int i) {
        pauseDownload(i);
    }

    @Override 
    public void onRemoveDownloads(List<Integer> list) {
        removeDownloads(list);
    }

    private void retryDownload(int i) {
        if (this.mBound) {
            this.mService.retryDownload(i);
            return;
        }
        this.shouldRetryAfterBound = true;
        this.mDownloadId = i;
        createAndBoundToService();
    }

    private void resumeDownload(int i) {
        if (this.mBound) {
            this.mService.resumeDownload(i);
            return;
        }
        this.shouldResumeAfterBound = true;
        this.mDownloadId = i;
        createAndBoundToService();
    }

    private void pauseDownload(int i) {
        if (this.mBound) {
            this.mService.pauseDownload(i);
            return;
        }
        this.shouldPauseAfterBound = true;
        this.mDownloadId = i;
        createAndBoundToService();
    }

    private void removeDownloads(List<Integer> list) {
        if (this.mBound) {
            this.mService.removeDownload(list);
            return;
        }
        this.shouldRemoveAfterBound = true;
        this.mDownloadIds = list;
        createAndBoundToService();
    }

    private void updateDownload(Download download, String str) {
        if (this.mBound) {
            this.mService.updateDownload(download, str);
            return;
        }
        this.mDownload = download;
        this.mNewUrl = str;
        this.shouldUpdateAfterBound = true;
        createAndBoundToService();
    }

    @Override 
    public void onUrlChanged(Download download) {
        updateDownloadUrl(download);
    }

    private void updateDownloadUrl(final Download download) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setTitle(R.string.update_url).setView(R.layout.dialog_update_url).setPositiveButton(R.string.update, (DialogInterface.OnClickListener) null).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null);
        final AlertDialog show = materialAlertDialogBuilder.show();
        show.getButton(-1).setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                AdvancedAppCompatEditText advancedAppCompatEditText = (AdvancedAppCompatEditText) show.findViewById(R.id.edit_text_url);
                if (advancedAppCompatEditText == null || advancedAppCompatEditText.getText() == null) {
                    return;
                }
                Request request = download.getRequest();
                String obj = advancedAppCompatEditText.getText().toString();
                if (validUrl(advancedAppCompatEditText) && !TextUtils.equals(request.getUrl(), obj)) {
                    updateDownload(download, obj);
                }
                show.cancel();
            }
        });
    }


    private boolean validUrl(AdvancedAppCompatEditText advancedAppCompatEditText) {
        if (advancedAppCompatEditText.getText() == null) {
            return false;
        }
        String obj = advancedAppCompatEditText.getText().toString();
        TextInputLayout textInputLayout = (TextInputLayout) advancedAppCompatEditText.getParent().getParent();
        if (Patterns.WEB_URL.matcher(obj).matches() && !TextUtils.isEmpty(obj)) {
            textInputLayout.setErrorEnabled(false);
            return true;
        }
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setError(getText(R.string.invalid_url));
        return false;
    }


    @Override
    public void onClick(View v) {

    }
}
