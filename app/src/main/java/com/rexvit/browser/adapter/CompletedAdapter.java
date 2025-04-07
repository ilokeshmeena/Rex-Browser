package com.rexvit.browser.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rexvit.browser.Interface.ActionListener;
import com.rexvit.browser.R;
import com.rexvit.browser.utils.DateUtils;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.view.Icons;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Request;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.DownloadsViewHolder> {
    private final String TAG;
    ActionListener actionListener;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    private List<Download> downloads;
    LinearLayout emptyStateView;
    private SparseArray<DownloadsViewHolder> holderMap;
    private Context mContext;
    private OnCompletedDownloadClickedListener onCompletedDownloadClickedListener;
    private DownloadingAdapter.OnRemoveDownloadsListener onRemoveDownloadsListener;
    private SparseBooleanArray selectedActiveDownloads;


    
    public interface OnCompletedDownloadClickedListener {
        void onCompletedDownloadClickedListener(Download download);
    }

    
    public class DownloadsViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView actionMore;
        public AppCompatImageView downloadActionIcon;
        public TextView downloadCreatedDate;
        public AppCompatImageView fileIcon;
        public TextView fileSize;
        public TextView fileTitle;
        public LinearLayout fileWrapper;

        public DownloadsViewHolder(View view) {
            super(view);
            this.fileTitle = (TextView) view.findViewById(R.id.file_name);
            this.fileSize = (TextView) view.findViewById(R.id.download_size);
            this.downloadCreatedDate = (TextView) view.findViewById(R.id.download_created_date);
            this.fileIcon = (AppCompatImageView) view.findViewById(R.id.file_icon);
            this.downloadActionIcon = (AppCompatImageView) view.findViewById(R.id.download_action);
            this.fileWrapper = (LinearLayout) view.findViewById(R.id.file_wrapper);
            this.actionMore = (AppCompatImageView) view.findViewById(R.id.action_more);
        }
    }

    public CompletedAdapter(Context context, List<Download> list, ActionListener actionListener, LinearLayout linearLayout) {

        this.TAG = "CompletedAdapter";
        this.holderMap = new SparseArray();
        this.selectedActiveDownloads = new SparseBooleanArray();
        this.downloads = list;
        this.mContext = context;
        this.actionListener = actionListener;
        this.emptyStateView = linearLayout;
        this.actionModeCallback = new ActionModeCallback();


        Context context2 = this.mContext;
        if (context2 instanceof OnCompletedDownloadClickedListener) {
            this.onCompletedDownloadClickedListener = (OnCompletedDownloadClickedListener) context2;
        }
        if (context2 instanceof DownloadingAdapter.OnRemoveDownloadsListener) {
            this.onRemoveDownloadsListener = (DownloadingAdapter.OnRemoveDownloadsListener) context2;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnRemoveDownloadsListener");
    }

    @Override 
    public DownloadsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DownloadsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_completed, viewGroup, false));
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
        final Download download = this.downloads.get(adapterPosition);
        this.holderMap.put(download.getId(), downloadsViewHolder);
        downloadsViewHolder.fileTitle.setText(FileUtils.getFileName(download));
        downloadsViewHolder.fileTitle.setSelected(true);
        downloadsViewHolder.downloadCreatedDate.setText(DateUtils.formatDateToHumanReadable(Long.valueOf(download.getCreated())));
        downloadsViewHolder.fileSize.setText(Formatter.formatShortFileSize(this.mContext, download.getTotal()));
        downloadsViewHolder.fileIcon.setImageResource(Icons.getIconDrawableId(download.getFile()).intValue());
        downloadsViewHolder.actionMore.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                showMenu(view, downloadsViewHolder.getAdapterPosition());
            }
        });
        toggleSelectionBackground(downloadsViewHolder, adapterPosition);
        downloadsViewHolder.fileWrapper.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                if (CompletedAdapter.this.actionMode != null) {
                    toggleSelection(downloadsViewHolder.getAdapterPosition());
                } else {
                    completedDownloadClicked(download);
                }
            }
        });
        downloadsViewHolder.fileWrapper.setOnLongClickListener(new View.OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                if (CompletedAdapter.this.actionMode == null) {
                    CompletedAdapter.this.actionMode = ((AppCompatActivity) CompletedAdapter.this.mContext).startSupportActionMode(CompletedAdapter.this.actionModeCallback);
                }
                toggleSelection(downloadsViewHolder.getAdapterPosition());
                return false;
            }
        });
    }



    public void filter(List<Download> list) {
        this.downloads = list;
        notifyDataSetChanged();
    }

    @Override 
    public int getItemCount() {
        return this.downloads.size();
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
            View decorView = ((Activity) CompletedAdapter.this.mContext).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = CompletedAdapter.this.mContext.getResources().getColor(R.color.mColorPrimaryDarK);
            this.colorTo = CompletedAdapter.this.mContext.getResources().getColor(R.color.colorDarkerGray);
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
                        ((Activity) CompletedAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());

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
            CompletedAdapter.this.deleteSelectedDownloads(actionMode);
            return true;
        }

        @Override 
        public void onDestroyActionMode(ActionMode actionMode) {
            CompletedAdapter.this.clearSelection();
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
                        ((Activity) CompletedAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());

                    }
                });
                ofObject.start();
            }
            CompletedAdapter.this.actionMode = null;
        }


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

    
    public void deleteSelectedDownloads(final ActionMode actionMode) {
        final List<Integer> selectedDownloads = getSelectedDownloads();
        final int selectedItemCount = getSelectedItemCount();
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this.mContext);
        materialAlertDialogBuilder.setTitle((CharSequence) this.mContext.getString(R.string.are_you_sure_to_delete)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { 
            @Override 
            public final void onClick(DialogInterface dialogInterface, int i) {
                CompletedAdapter.this.oDeleteSelectedDownloads(selectedItemCount, selectedDownloads, actionMode, dialogInterface, i);
            }
        }).setNegativeButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) null);
        materialAlertDialogBuilder.create().show();
    }

    public  void oDeleteSelectedDownloads(int i, List list, ActionMode actionMode, DialogInterface dialogInterface, int i2) {
        for (int i3 = 0; i3 < i; i3++) {
            this.actionListener.onRemoveDownload(this.downloads.get(((Integer) list.get(i3)).intValue()).getId());
        }
        removeItems(list);
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
                return CompletedAdapter.objRemoveItems((Integer) obj, (Integer) obj2);
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

    public void completedDownloadClicked(Download download) {
        this.onCompletedDownloadClickedListener.onCompletedDownloadClickedListener(download);
    }

    @SuppressLint("RestrictedApi")
    public void showMenu(View view, final int i) {
        final Download download = this.downloads.get(i);
        PopupMenu popupMenu = new PopupMenu(this.mContext, view);
        if (popupMenu.getMenu() instanceof MenuBuilder) {
            ((MenuBuilder) popupMenu.getMenu()).setOptionalIconsVisible(true);
        }
        popupMenu.getMenuInflater().inflate(R.menu.popup_completed, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() { 
            @Override 
            public final boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_delete) {
                    deleteCompletedDownload(i);
                    return false;
                } else if (itemId == R.id.action_details) {
                    showDownloadDetails(download);
                    return false;
                } else if (itemId == R.id.action_shere) {
                    FileUtils.shareFile(CompletedAdapter.this.mContext, download.getFile());
                    return false;
                }
                return false;
            }
        });
        popupMenu.show();
    }



    private void deleteCompletedDownload(final int i) {
        new MaterialAlertDialogBuilder(this.mContext).setTitle(R.string.are_you_sure_to_delete).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() { 
            @Override 
            public final void onClick(DialogInterface dialogInterface, int i2) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(Integer.valueOf(CompletedAdapter.this.downloads.get(i).getId()));
                removeDownloads(arrayList);
                CompletedAdapter.this.downloads.remove(i);
                notifyItemRemoved(i);
            }
        }).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) null).show();
    }



    private void removeDownloads(List<Integer> list) {
        DownloadingAdapter.OnRemoveDownloadsListener onRemoveDownloadsListener = this.onRemoveDownloadsListener;
        if (onRemoveDownloadsListener != null) {
            onRemoveDownloadsListener.onRemoveDownloads(list);
        }
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
