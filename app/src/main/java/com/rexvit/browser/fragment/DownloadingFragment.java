package com.rexvit.browser.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.rexvit.browser.Interface.ActionListener;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.DownloadingAdapter;
import com.rexvit.browser.database.DataUpdatedEvent;
import com.rexvit.browser.models.DownloadData;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.view.MaterialSearchView;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2core.Func;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class DownloadingFragment extends Fragment implements ActionListener, MaterialSearchView.OnQueryTextListener {
    public static final String FETCH_NAMESPACE = "DownloadBooster";
    public static final int GROUP_ID = -1246295935;
    private FragmentActivity activityCompat;
    DownloadingAdapter adapter;
    Context context;
    CoordinatorLayout coordinatorLayout;
    LinearLayout emptyState;
    private Fetch fetch;
    private boolean isFragmentVisibleToUser;
    RecyclerView recyclerView;
    private MaterialSearchView searchView;
    public final String TAG = "DownloadingFragment";
    private int DURATION = 5000;
    List<Download> mDownloads = new ArrayList();
    List<DownloadData> activeDownloads = new ArrayList();
    private final FetchListener fetchListener = new AbstractFetchListener() { 
        @Override 
        public void onQueued(Download download, boolean z) {
            DownloadingFragment.this.adapter.updateStatus(download);
            Log.d(DownloadingFragment.this.TAG, "OnQueued");
            
        }

        @Override 
        public void onAdded(Download download) {
            super.onAdded(download);
            DownloadingFragment.this.adapter.updateStatus(download);
            Log.d(DownloadingFragment.this.TAG, "OnAdded");
        }

        @Override 
        public void onWaitingNetwork(Download download) {
            super.onWaitingNetwork(download);
            DownloadingFragment.this.adapter.updateStatus(download);
            Log.d(DownloadingFragment.this.TAG, "OnWaiting");
        }

        @Override 
        public void onStarted(Download download, List<? extends DownloadBlock> list, int i) {
            super.onStarted(download, list, i);
            DownloadingFragment.this.adapter.updateStatus(download);

            Log.d(DownloadingFragment.this.TAG, "OnStarted");

        }

        @Override 
        public void onCompleted(Download download) {
            Log.d(DownloadingFragment.this.TAG, "OnCompleted");
            DownloadingFragment.this.adapter.removeCompletedDownload(download);
            EventBus.getDefault().post(new DataUpdatedEvent.DownloadCompleted(download));

            DownloadingFragment downloadingFragment = DownloadingFragment.this;
            downloadingFragment.showDownloadCompleteSnackBar(downloadingFragment.coordinatorLayout, download);
        }

        @Override 
        public void onError(Download download, Error error, Throwable th) {
            super.onError(download, error, th);
            Downloader.Response httpResponse = download.getError().getHttpResponse();
            if (httpResponse != null && httpResponse.getCode() == 403) {
                Log.d(DownloadingFragment.this.TAG, httpResponse.getErrorResponse());
            }
            String str = DownloadingFragment.this.TAG;
            Log.d(str, "OnError called " + download.getError());
            DownloadingFragment.this.adapter.updateStatus(download);
        }

        @Override 
        public void onProgress(Download download, long j, long j2) {
            DownloadingFragment.this.adapter.update(download, j, j2);
            Log.d(DownloadingFragment.this.TAG, "onPaused");
        }

        @Override 
        public void onPaused(Download download) {
            Log.d(DownloadingFragment.this.TAG, "onPaused");
            DownloadingFragment.this.adapter.updateStatus(download);
        }

        @Override 
        public void onResumed(Download download) {
            Log.d(DownloadingFragment.this.TAG, "onResumed");
            DownloadingFragment.this.adapter.updateStatus(download);
        }

        @Override 
        public void onCancelled(Download download) {
            Log.d(DownloadingFragment.this.TAG, "onCancelled");
            DownloadingFragment.this.adapter.updateStatus(download);
        }

        @Override 
        public void onRemoved(Download download) {
            Log.d(DownloadingFragment.this.TAG, "onRemoved");
            DownloadingFragment.this.adapter.updateStatus(download);
        }

        @Override 
        public void onDeleted(Download download) {
            Log.d(DownloadingFragment.this.TAG, "onDeleted");
            DownloadingFragment.this.adapter.updateStatus(download);
        }
    };

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.context = getContext();
        setHasOptionsMenu(true);
        this.activityCompat = getActivity();
        this.fetch = Fetch.Impl.getDefaultInstance();
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_downloading, viewGroup, false);
    }

    @Override 
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_downloading);
        this.emptyState = (LinearLayout) view.findViewById(R.id.empty_state_downloading);
        this.searchView = (MaterialSearchView) this.activityCompat.findViewById(R.id.search_view);
        this.coordinatorLayout = (CoordinatorLayout) this.activityCompat.findViewById(R.id.coordinator_layout);
        this.adapter = new DownloadingAdapter(this.context, this.mDownloads, this.emptyState);
        this.searchView.setOnQueryTextListener(this);
    }

    @Override 
    public void onDetach() {
        super.onDetach();
    }

    @Override 
    public void onPause() {
        super.onPause();
        this.fetch.removeListener(this.fetchListener);
    }

    @Override 
    public void onDestroy() {
        super.onDestroy();
    }

    @Override 
    public void onResume() {
        super.onResume();
        loadActiveDownloads();
    }

    @Override 
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override 
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("RestrictedApi")
    @Override 
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.fragment_downloading, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override 
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_download_all_parallel) {
            this.fetch.resumeGroup(GROUP_ID);
            loadActiveDownloads();
        } else if (itemId == R.id.action_pause_all) {
            this.fetch.pauseGroup(GROUP_ID);
            loadActiveDownloads();
        } else if (itemId == R.id.action_search) {
            this.searchView.openSearch();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override 
    public void setUserVisibleHint(boolean z) {
        super.setUserVisibleHint(z);
        if (z) {
            this.isFragmentVisibleToUser = true;
            MaterialSearchView materialSearchView = this.searchView;
            if (materialSearchView != null) {
                materialSearchView.setOnQueryTextListener(this);
                return;
            }
            return;
        }
        this.isFragmentVisibleToUser = false;
        MaterialSearchView materialSearchView2 = this.searchView;
        if (materialSearchView2 != null) {
            materialSearchView2.setOnQueryTextListener(null);
        }
    }

    @Override 
    public void onPauseDownload(int i) {
        this.fetch.pause(i);
    }

    @Override 
    public void onResumeDownload(int i) {
        this.fetch.resume(i);
    }

    @Override 
    public void onRemoveDownload(int i) {
        this.fetch.remove(i);
    }

    @Override 
    public void onRetryDownload(int i) {
        this.fetch.retry(i);
    }

    private void loadActiveDownloads() {
        this.fetch.getDownloadsInGroup(GROUP_ID, new Func() { 
            @Override 
            public final void call(Object obj) {
                DownloadingFragment.this.listLoadActiveDownloads((List) obj);
            }
        }).addListener(this.fetchListener);
    }

    
    public static  int stLoadActiveDownloads(Download download, Download download2) {
        return (download.getCreated() > download2.getCreated() ? 1 : (download.getCreated() == download2.getCreated() ? 0 : -1));
    }

    public  void listLoadActiveDownloads(List list) {
        Collections.sort(list, new Comparator() { 
            @Override 
            public final int compare(Object obj, Object obj2) {
                return DownloadingFragment.stLoadActiveDownloads((Download) obj, (Download) obj2);
            }
        });
        ArrayList arrayList = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Download download = (Download) it.next();
            if (download.getStatus() != Status.COMPLETED) {
                arrayList.add(download);
            }
        }
        this.mDownloads = arrayList;
        this.adapter = new DownloadingAdapter(this.context, this.mDownloads, this.emptyState);
        String str = this.TAG;
        Log.d(str, "Downloads size " + this.mDownloads.size());
        if (this.mDownloads.size() == 0) {
            this.emptyState.setVisibility(View.VISIBLE);
            return;
        }
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.context, RecyclerView.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.context, 1);
        dividerItemDecoration.setDrawable(this.context.getResources().getDrawable(R.drawable.ic_divider));
        this.recyclerView.addItemDecoration(dividerItemDecoration);
        this.recyclerView.setAdapter(this.adapter);
        this.emptyState.setVisibility(View.GONE);
    }

    @Subscribe
    public void onNewDownloadAdded(DataUpdatedEvent.NewDownloadAdded newDownloadAdded) {
        Log.d(this.TAG, "onNewDownloadAdded()");
        loadActiveDownloads();
    }

    @Override 
    public boolean onQueryTextSubmit(String str) {
        if (this.isFragmentVisibleToUser) {
            searchDownloads(str);
            return true;
        }
        return true;
    }

    @Override 
    public boolean onQueryTextChange(String str) {
        if (this.isFragmentVisibleToUser) {
            searchDownloads(str);
            return true;
        }
        return true;
    }

    private void searchDownloads(String str) {
        ArrayList arrayList = new ArrayList();
        for (Download download : this.mDownloads) {
            if (FileUtils.getFileName(download).toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(download);
            }
            this.adapter.filter(arrayList);
        }
    }

    
    public void showDownloadCompleteSnackBar(CoordinatorLayout coordinatorLayout, final Download download) {
        if (isAdded()) {
            Snackbar.make(coordinatorLayout, getString(R.string.download_completed) + " \"" + FileUtils.getFileName(download) + "\"", this.DURATION).setAction("Open", new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    FileUtils.openFile(DownloadingFragment.this.context, download.getFile());
                }
            }).show();
        }
    }



}
