package com.rexvit.browser.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rexvit.browser.R;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.view.Icons;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DownloadingAdapter extends RecyclerView.Adapter<DownloadingAdapter.DownloadsViewHolder> {
    private ActionMode actionMode;
    private final String TAG;

    private ActionModeCallback actionModeCallback;
    private List<Download> downloads;
    LinearLayout emptyStateView;
    private SparseArray<DownloadsViewHolder> holderMap;
    private Context mContext;
    private OnPauseDownloadClickedListener onPauseDownloadClickedListener;
    private OnRemoveDownloadsListener onRemoveDownloadsListener;
    private OnResumeDownloadClickedListener onResumeDownloadClickedListener;
    private OnRetryDownloadClickedListener onRetryDownloadClickedListener;
    private OnUrlChangedListener onUrlChangedListener;
    private SparseBooleanArray selectedActiveDownloads;


    
    public interface OnPauseDownloadClickedListener {
        void onPauseDownloadClicked(int i);
    }

    
    public interface OnRemoveDownloadsListener {
        void onRemoveDownloads(List<Integer> list);
    }

    
    public interface OnResumeDownloadClickedListener {
        void onResumeDownloadClicked(int i);
    }

    
    public interface OnRetryDownloadClickedListener {
        void onRetryDownloadClicked(int i);
    }

    
    public interface OnUrlChangedListener {
        void onUrlChanged(Download download);
    }




    
    public class DownloadsViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView actionMore;
        public AppCompatImageView downloadActionIcon;
        public TextView downloadPercent;
        public ProgressBar downloadProgress;
        public TextView downloadStatus;
        public AppCompatImageView fileIcon;
        public TextView fileSize;
        public TextView fileTitle;
        public LinearLayout fileWrapper;

        public DownloadsViewHolder(View view) {
            super(view);
            this.fileTitle = (TextView) view.findViewById(R.id.file_name);
            this.fileSize = (TextView) view.findViewById(R.id.download_size);
            this.downloadStatus = (TextView) view.findViewById(R.id.download_status);
            this.downloadProgress = (ProgressBar) view.findViewById(R.id.file_download_progress);
            this.fileIcon = (AppCompatImageView) view.findViewById(R.id.file_icon);
            this.downloadPercent = (TextView) view.findViewById(R.id.download_percent);
            this.downloadActionIcon = (AppCompatImageView) view.findViewById(R.id.download_action);
            this.actionMore = (AppCompatImageView) view.findViewById(R.id.action_more);
            this.fileWrapper = (LinearLayout) view.findViewById(R.id.file_wrapper);
        }
    }

    public DownloadingAdapter(Context context, List<Download> list, LinearLayout linearLayout) {

        this.TAG = "DownloadingAdapter";
        this.holderMap = new SparseArray();
        this.selectedActiveDownloads = new SparseBooleanArray();
        this.downloads = list;
        this.mContext = context;
        this.emptyStateView = linearLayout;
        this.actionModeCallback = new ActionModeCallback();


        initializeListeners();
        if (context instanceof OnUrlChangedListener) {
            this.onUrlChangedListener = (OnUrlChangedListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnUrlChangedListener");
    }

    @Override 
    public DownloadsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DownloadsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_download, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(final DownloadsViewHolder downloadsViewHolder, int i) {
        int adapterPosition = downloadsViewHolder.getAdapterPosition();
        int size = this.holderMap.size();
        int i2 = 0;
        while (true) {
            if (i2 >= size) {
                break;
            } else if (this.holderMap.valueAt(i2) == downloadsViewHolder) {
                this.holderMap.removeAt(i2);
                break;
            } else {
                i2++;
            }
        }
        Download download = this.downloads.get(adapterPosition);
        this.holderMap.put(download.getId(), downloadsViewHolder);
        downloadsViewHolder.fileTitle.setText(FileUtils.getFileName(download));
        downloadsViewHolder.fileTitle.setSelected(true);
        downloadsViewHolder.fileSize.setText(readableFileSize(this.mContext, download.getDownloaded(), download.getTotal()));
        downloadsViewHolder.fileIcon.setImageResource(Icons.getIconDrawableId(download.getFile()).intValue());
        downloadsViewHolder.downloadPercent.setText(getPercent(download.getDownloaded(), download.getTotal()));
        updateProgressBar(downloadsViewHolder.downloadProgress, download.getProgress(), true);
        setDownloadAction(downloadsViewHolder, download);
        toggleSelectionBackground(downloadsViewHolder, adapterPosition);
        downloadsViewHolder.fileWrapper.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                if (DownloadingAdapter.this.actionMode != null) {
                    toggleSelection(downloadsViewHolder.getAdapterPosition());
                }
            }
        });
        downloadsViewHolder.fileWrapper.setOnLongClickListener(new View.OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                if (DownloadingAdapter.this.actionMode == null) {
                    DownloadingAdapter.this.actionMode = ((AppCompatActivity) DownloadingAdapter.this.mContext).startSupportActionMode(DownloadingAdapter.this.actionModeCallback);
                }
                toggleSelection(downloadsViewHolder.getAdapterPosition());
                return false;
            }
        });
        downloadsViewHolder.actionMore.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                showMenu(view, downloadsViewHolder.getAdapterPosition());
            }
        });
    }



    public void filter(List<Download> list) {
        this.downloads = list;
        notifyDataSetChanged();
    }

    public static void updateProgressBar(ProgressBar progressBar, int i, boolean z) {
        if (i == -1) {
            i = 0;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            progressBar.setProgress(i, z);
        } else {
            progressBar.setProgress(i);
        }
    }

    public String readableFileSize(Context context, long j, long j2) {
        if (j2 == -1) {
            j2 = 0;
        }
        return Formatter.formatShortFileSize(context, j) + "/" + Formatter.formatShortFileSize(context, j2);
    }

    
    
    


    public void update(Download download, long j, long j2) {
        DownloadsViewHolder downloadsViewHolder = this.holderMap.get(download.getId());
        if (downloadsViewHolder == null) {
            return;
        }
        downloadsViewHolder.fileSize.setText(readableFileSize(this.mContext, download.getDownloaded(), download.getTotal()));
        downloadsViewHolder.downloadPercent.setText(getPercent(download.getDownloaded(), download.getTotal()));
        downloadsViewHolder.downloadStatus.setText(getDownloadSpeed(j2));
        updateProgressBar(downloadsViewHolder.downloadProgress, download.getProgress(), true);
        String str = this.TAG;
        Log.d(str, "progress " + download.getId() + " with " + downloadsViewHolder);
    }

    public void updateStatus(Download download) {
        DownloadsViewHolder downloadsViewHolder = this.holderMap.get(download.getId());
        if (downloadsViewHolder == null) {
            return;
        }
        setDownloadAction(downloadsViewHolder, download);
    }

    public void removeCompletedDownload(Download download) {
        ActionMode actionMode;
        DownloadsViewHolder downloadsViewHolder = this.holderMap.get(download.getId());
        if (downloadsViewHolder == null) {
            return;
        }
        int adapterPosition = downloadsViewHolder.getAdapterPosition();
        if (isSelected(adapterPosition)) {
            this.selectedActiveDownloads.delete(adapterPosition);
        }
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0 && (actionMode = this.actionMode) != null) {
            actionMode.finish();
        } else {
            ActionMode actionMode2 = this.actionMode;
            if (actionMode2 != null) {
                actionMode2.setTitle(selectedItemCount + " " + this.mContext.getString(R.string.selected));
                this.actionMode.invalidate();
            }
        }
        this.downloads.remove(adapterPosition);
        this.holderMap.remove(download.getId());
        notifyItemRemoved(adapterPosition);
        setupEmptyState();
    }



    public static class DevIn {
        private static final Map<Status, Integer> statusTextMap;

        static {
            statusTextMap = new HashMap<>();
            statusTextMap.put(Status.COMPLETED, R.string.done);
            statusTextMap.put(Status.DOWNLOADING, R.string.downloading);
            statusTextMap.put(Status.FAILED, R.string.error);
            statusTextMap.put(Status.PAUSED, R.string.paused);
            statusTextMap.put(Status.QUEUED, R.string.waiting_in_queue);
            statusTextMap.put(Status.REMOVED, R.string.removed);
            statusTextMap.put(Status.NONE, R.string.not_queued);
            statusTextMap.put(Status.CANCELLED, R.string.unknown);
            statusTextMap.put(Status.ADDED, R.string.unknown);
        }
    }

    private String getStatusText(Status status) {
        Integer stringResId = DevIn.statusTextMap.get(status);
        if (stringResId != null) {
            return mContext.getString(stringResId);
        } else {
            return mContext.getString(R.string.unknown);
        }
    }

    public void setDownloadAction(DownloadsViewHolder downloadsViewHolder, final Download download) {
        downloadsViewHolder.downloadStatus.setText(getStatusText(download.getStatus()));
        switch (download.getStatus()) {
            case COMPLETED:
                downloadsViewHolder.downloadActionIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_done));
                downloadsViewHolder.downloadActionIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            Context context = DownloadingAdapter.this.mContext;
                            Toast.makeText(context, "Downloaded Path:" + download.getFile(), Toast.LENGTH_LONG).show();
                        } else {
                            Uri fromFile = Uri.fromFile(new File(download.getFile()));
                            Intent intent = new Intent("android.intent.action.VIEW");
                            intent.setDataAndType(fromFile, FileUtils.getMimeType(DownloadingAdapter.this.mContext, fromFile));
                            DownloadingAdapter.this.mContext.startActivity(intent);
                        }
                    }
                });
                break;
            case DOWNLOADING:
            case QUEUED:
                downloadsViewHolder.downloadActionIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_pause));
                downloadsViewHolder.downloadActionIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pauseDownload(download.getId());
                    }
                });
                break;
            case FAILED:
            case NONE:
            case CANCELLED:
                downloadsViewHolder.downloadActionIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_retry));
                downloadsViewHolder.downloadActionIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        retryDownload(download.getId());
                    }
                });
                break;
            case PAUSED:
            case ADDED:
                downloadsViewHolder.downloadActionIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_action_play));
                downloadsViewHolder.downloadActionIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resumeDownload(download.getId());
                    }
                });
                break;
            case REMOVED:
            default:
                
                break;
        }
    }


    public class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        @Override 
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private ActionModeCallback() {
            View decorView = ((Activity) DownloadingAdapter.this.mContext).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = DownloadingAdapter.this.mContext.getResources().getColor(R.color.mColorPrimaryDarK);
            this.colorTo = DownloadingAdapter.this.mContext.getResources().getColor(R.color.colorDarkerGray);
        }

         ActionModeCallback(DownloadingAdapter downloadingAdapter, DevIn devIn) {
            this();
        }

        @Override 
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.fragment_downloading_action_mode, menu);
            if (Build.VERSION.SDK_INT >= 21) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int i = this.flags & (-8193);
                    this.flags = i;
                    this.view.setSystemUiVisibility(i);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.colorFrom), Integer.valueOf(this.colorTo));
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { 
                    @Override 
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) DownloadingAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());

                    }
                });
                ofObject.start();
            }
            return true;
        }



        @Override 
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() != R.id.action_delete) {
                return true;
            }
            DownloadingAdapter.this.deleteSelectedDownloads(actionMode);
            return true;
        }

        @Override 
        public void onDestroyActionMode(ActionMode actionMode) {
            DownloadingAdapter.this.clearSelection();
            if (Build.VERSION.SDK_INT >= 21) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int i = this.flags | 8192;
                    this.flags = i;
                    this.view.setSystemUiVisibility(i);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom));
                ofObject.setDuration(300L);
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { 
                    @Override 
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) DownloadingAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());

                    }
                });
                ofObject.start();
            }
            DownloadingAdapter.this.actionMode = null;
        }


    }

    public String getDownloadSpeed(long j) {
        return Formatter.formatShortFileSize(this.mContext, j) + "/s";
    }

    public String getPercent(long j, long j2) {
        double d;
        if (j2 == -1) {
            d = 0.0d;
        } else {
            double d2 = j;
            Double.isNaN(d2);
            double d3 = j2;
            Double.isNaN(d3);
            d = (d2 * 100.0d) / d3;
        }
        Double valueOf = Double.valueOf(d);
        return String.format(Locale.getDefault(), "%.1f", valueOf) + "%";
    }

    @Override 
    public int getItemCount() {
        return this.downloads.size();
    }

    
    public void clearSelection() {
        List<Integer> selectedDownloads = getSelectedDownloads();
        this.selectedActiveDownloads.clear();
        for (Integer num : selectedDownloads) {
            notifyItemChanged(num.intValue());
        }
    }

    private List<Integer> getSelectedDownloads() {
        int size = this.selectedActiveDownloads.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedActiveDownloads.keyAt(i)));
        }
        return arrayList;
    }

    private boolean isSelected(int i) {
        return getSelectedDownloads().contains(Integer.valueOf(i));
    }

    private void toggleSelectionBackground(DownloadsViewHolder downloadsViewHolder, int i) {
        if (isSelected(i)) {
            downloadsViewHolder.fileWrapper.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.colorSelectedDownloads));
            return;
        }
        TypedValue typedValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843534, typedValue, true);
        downloadsViewHolder.fileWrapper.setBackgroundResource(typedValue.resourceId);
    }

    private void toggleSelection(int i) {
        if (this.selectedActiveDownloads.get(i, false)) {
            this.selectedActiveDownloads.delete(i);
        } else {
            this.selectedActiveDownloads.put(i, true);
        }
        notifyItemChanged(i);
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode = this.actionMode;
        actionMode.setTitle(selectedItemCount + " " + this.mContext.getString(R.string.selected));
        this.actionMode.invalidate();
    }

    private int getSelectedItemCount() {
        return this.selectedActiveDownloads.size();
    }

    
    public void deleteSelectedDownloads(ActionMode actionMode) {
        List<Integer> selectedDownloads = getSelectedDownloads();
        removeDownloads(getSelectedDownloadIds(selectedDownloads));
        removeItems(selectedDownloads);
        actionMode.finish();
    }

    private void removeItem(int i) {
        this.downloads.remove(i);
        setupEmptyState();
        notifyItemRemoved(i);
    }

    
    public static  int objRemoveItems(Integer num, Integer num2) {
        return num2.intValue() - num.intValue();
    }

    private void removeItems(List<Integer> list) {
        Collections.sort(list, new Comparator() { 
            @Override 
            public final int compare(Object obj, Object obj2) {
                return DownloadingAdapter.objRemoveItems((Integer) obj, (Integer) obj2);
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeItem(list.get(0).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(Integer.valueOf(list.get(i - 1).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    removeItem(list.get(0).intValue());
                } else {
                    removeRange(list.get(i - 1).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removeRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.downloads.remove(i);
        }
        setupEmptyState();
        notifyItemRangeRemoved(i, i2);
    }

    private void setupEmptyState() {
        if (this.downloads.size() > 0) {
            this.emptyStateView.setVisibility(View.GONE);
        } else {
            this.emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    private void retryDownload(int i) {
        OnRetryDownloadClickedListener onRetryDownloadClickedListener = this.onRetryDownloadClickedListener;
        if (onRetryDownloadClickedListener != null) {
            onRetryDownloadClickedListener.onRetryDownloadClicked(i);
        }
    }

    private void resumeDownload(int i) {
        OnResumeDownloadClickedListener onResumeDownloadClickedListener = this.onResumeDownloadClickedListener;
        if (onResumeDownloadClickedListener != null) {
            onResumeDownloadClickedListener.onResumeDownloadClicked(i);
        }
    }

    private void pauseDownload(int i) {
        OnPauseDownloadClickedListener onPauseDownloadClickedListener = this.onPauseDownloadClickedListener;
        if (onPauseDownloadClickedListener != null) {
            onPauseDownloadClickedListener.onPauseDownloadClicked(i);
        }
    }

    private void removeDownloads(List<Integer> list) {
        OnRemoveDownloadsListener onRemoveDownloadsListener = this.onRemoveDownloadsListener;
        if (onRemoveDownloadsListener != null) {
            onRemoveDownloadsListener.onRemoveDownloads(list);
        }
    }

    private List<Integer> getSelectedDownloadIds(List<Integer> list) {
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.downloads.get(list.get(i).intValue()).getId()));
        }
        return arrayList;
    }

    private void initializeListeners() {
        Context context = this.mContext;
        if (context instanceof OnRetryDownloadClickedListener) {
            this.onRetryDownloadClickedListener = (OnRetryDownloadClickedListener) context;
            if (context instanceof OnResumeDownloadClickedListener) {
                this.onResumeDownloadClickedListener = (OnResumeDownloadClickedListener) context;
                if (context instanceof OnPauseDownloadClickedListener) {
                    this.onPauseDownloadClickedListener = (OnPauseDownloadClickedListener) context;
                    if (context instanceof OnRemoveDownloadsListener) {
                        this.onRemoveDownloadsListener = (OnRemoveDownloadsListener) context;
                        return;
                    }
                    throw new RuntimeException(this.mContext.toString() + " must implement OnRemoveDownloadsListener");
                }
                throw new RuntimeException(this.mContext.toString() + " must implement OnPauseDownloadClickedListener");
            }
            throw new RuntimeException(this.mContext.toString() + " must implement OnResumeDownloadClickedListener");
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnRetryDownloadClickedListener");
    }

    @SuppressLint("RestrictedApi")
    public void showMenu(View view, final int i) {
        PopupMenu popupMenu = new PopupMenu(this.mContext, view);
        if (popupMenu.getMenu() instanceof MenuBuilder) {
            ((MenuBuilder) popupMenu.getMenu()).setOptionalIconsVisible(true);
        }
        popupMenu.getMenuInflater().inflate(R.menu.popup_downloading, popupMenu.getMenu());
        final Download download = this.downloads.get(i);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() { 
            @Override 
            public final boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_delete) {
                    deleteActiveDownload(i);
                    return false;
                } else if (itemId == R.id.action_details) {
                    showDownloadDetails(download);
                    return false;
                }
                return false;            }
        });
        popupMenu.show();
    }

    public void updateUrl(Download download) {
        OnUrlChangedListener onUrlChangedListener = this.onUrlChangedListener;
        if (onUrlChangedListener != null) {
            onUrlChangedListener.onUrlChanged(download);
        }
    }

    private void deleteActiveDownload(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(this.downloads.get(i).getId()));
        removeDownloads(arrayList);
        this.downloads.remove(i);
        notifyItemRemoved(i);
    }

    private void showDownloadDetails(Download download) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this.mContext);
        materialAlertDialogBuilder.setView(R.layout.dialog_file_details).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null);
        AlertDialog show = materialAlertDialogBuilder.show();
        TextView textView = (TextView) show.findViewById(R.id.download_link);
        TextView textView2 = (TextView) show.findViewById(R.id.download_path);
        String parent = new File(download.getFile()).getParent();
        Request request = download.getRequest();
        if (textView != null) {
            textView.setText(request.getUrl());
        }
        if (textView2 != null) {
            textView2.setText(parent.replaceAll("/storage/emulated/\\d", ""));
        }
    }
}
