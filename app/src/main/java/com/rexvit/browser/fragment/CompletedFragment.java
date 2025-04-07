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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rexvit.browser.Interface.ActionListener;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.CompletedAdapter;
import com.rexvit.browser.database.DataUpdatedEvent;
import com.rexvit.browser.models.DownloadData;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.view.MaterialSearchView;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.Func;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CompletedFragment extends Fragment implements ActionListener, MaterialSearchView.OnQueryTextListener {
    private ActionListener actionListener;
    private FragmentActivity activityCompat;
    private CompletedAdapter adapter;
    private Context context;
    private LinearLayout emptyState;
    private Fetch fetch;
    private boolean isFragmentVisibleToUser;
    private RecyclerView recyclerView;
    private MaterialSearchView searchView;
    public final String TAG = "CompletedFragment";
    private List<Download> mDownloads = new ArrayList();
    private List<DownloadData> activeDownloads = new ArrayList();

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.context = getContext();
        setHasOptionsMenu(true);
        this.actionListener = this;
        this.activityCompat = getActivity();
        this.fetch = Fetch.Impl.getDefaultInstance();
    }

    @Override 
    public void onPause() {
        super.onPause();
    }

    @Override 
    public void onDestroy() {
        super.onDestroy();
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_completed, viewGroup, false);
    }

    @Override 
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_downloading);
        this.emptyState = (LinearLayout) view.findViewById(R.id.empty_state_downloading);
        MaterialSearchView materialSearchView = (MaterialSearchView) this.activityCompat.findViewById(R.id.search_view);
        this.searchView = materialSearchView;
        materialSearchView.setOnQueryTextListener(this);
    }

    @Override 
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override 
    public void onDetach() {
        super.onDetach();
    }

    @Override 
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        loadCompletedDownloads();
        Log.d("londo", "Completed onStart");
    }

    @Override 
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        Log.d("londo", "Completed onStop");
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.fragment_completed, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override 
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_search) {
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

    @Subscribe
    public void onDownloadCompleted(DataUpdatedEvent.DownloadCompleted downloadCompleted) {
        loadCompletedDownloads();
    }

    @Subscribe
    public void onNewDownloadAdded(DataUpdatedEvent.NewDownloadAdded newDownloadAdded) {
        loadCompletedDownloads();
    }

    private void loadCompletedDownloads() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(Status.COMPLETED);
        this.fetch.getDownloadsInGroupWithStatus(DownloadingFragment.GROUP_ID, arrayList, new Func() { 
            @Override 
            public final void call(Object obj) {
                CompletedFragment.this.sortListCompleted((List) obj);
            }
        });
    }

    
    public static  int mLoadCompletedDownloads(Download download, Download download2) {
        return (download2.getCreated() > download.getCreated() ? 1 : (download2.getCreated() == download.getCreated() ? 0 : -1));
    }

    public  void sortListCompleted(List list) {
        Collections.sort(list, new Comparator() { 
            @Override 
            public final int compare(Object obj, Object obj2) {
                return CompletedFragment.mLoadCompletedDownloads((Download) obj, (Download) obj2);
            }
        });
        this.mDownloads = list;
        this.adapter = new CompletedAdapter(this.context, this.mDownloads, this.actionListener, this.emptyState);
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
}
