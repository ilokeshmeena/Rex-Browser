package com.rexvit.browser.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.rexvit.browser.Interface.TabsView;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.utils.DrawableUtils;
import com.rexvit.browser.view.BrowserView;
import com.rexvit.browser.view.TabsImageView;
import com.rexvit.browser.view.customView.BackgroundDrawable;
import com.rexvit.browser.view.customView.ItemSwipeHelper;
import com.rexvit.browser.view.customView.SwitchRecyclerView;
import com.rexvit.browser.view.customView.VerticalItemAnimator;
import com.squareup.otto.Bus;


public class TabsFragment extends Fragment implements View.OnClickListener, TabsView {
    Bus mBus;
    SwitchRecyclerView mRecyclerView ;
    ImageView menulayout ;
    ImageView frameButton;

    LinearLayout lrBackTab;

    @Nullable
    private ViewAdapter mTabsAdapter;
    private TabsManager mTabsManager;
    private UIController mUiController;
   

    public TabsFragment() {
        BrowserApp.getAppComponent().inject(this);
    }

    @Override 
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        this.mUiController = (UIController) getActivity();
        this.mTabsManager = this.mUiController.getTabModel();

    }

    @Override 
    @Nullable
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.tab_drawer, viewGroup, false);
        VerticalItemAnimator verticalItemAnimator = new VerticalItemAnimator();
        verticalItemAnimator.setSupportsChangeAnimations(false);
        verticalItemAnimator.setAddDuration(200L);
        verticalItemAnimator.setChangeDuration(0L);
        verticalItemAnimator.setRemoveDuration(200L);
        verticalItemAnimator.setMoveDuration(200L);
        mRecyclerView = inflate.findViewById( R.id.tabs_list);
        this.mRecyclerView.setLayerType(View.LAYER_TYPE_NONE, null);
        this.mRecyclerView.setItemAnimator(verticalItemAnimator);
        this.mTabsAdapter = new ViewAdapter();
        this.mRecyclerView.setAdapter(this.mTabsAdapter);
        this.mRecyclerView.setHasFixedSize(true);
        this.mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL,false));
        menulayout = inflate.findViewById( R.id.menu_tab_button);
        frameButton = inflate.findViewById( R.id.new_tab_button);
        lrBackTab = inflate.findViewById(R.id.lrBackTab);
        lrBackTab.setOnClickListener(this);
        this.frameButton.setOnClickListener(this);
        this.menulayout.setOnClickListener(this);
       
        return inflate;
    }

    
    public TabsManager getTabsManager() {
        if (this.mTabsManager == null) {
            this.mTabsManager = this.mUiController.getTabModel();
        }
        return this.mTabsManager;
    }

    @Override 
    public void onDestroyView() {
        super.onDestroyView();
        this.mTabsAdapter = null;
    }

    @Override 
    public void onStart() {
        super.onStart();
        this.mBus.register(this);
    }

    @Override 
    public void onResume() {
        super.onResume();
        new ItemTouchHelper(new ItemSwipeHelper(1, this.mTabsAdapter)).attachToRecyclerView(this.mRecyclerView);
        new ItemTouchHelper(new ItemSwipeHelper(2, this.mTabsAdapter)).attachToRecyclerView(this.mRecyclerView);
        ViewAdapter viewAdapter = this.mTabsAdapter;
        if (viewAdapter != null) {
            viewAdapter.notifyDataSetChanged();
        }
    }

    @Override 
    public void onStop() {
        super.onStop();
        this.mBus.unregister(this);
    }

    public void reinitializePreferences() {
        ViewAdapter viewAdapter;
        if (getActivity() == null || (viewAdapter = this.mTabsAdapter) == null) {
            return;
        }
        viewAdapter.notifyDataSetChanged();
    }

    @Override 
    public void tabsInitialized() {
        ViewAdapter viewAdapter = this.mTabsAdapter;
        if (viewAdapter != null) {
            viewAdapter.notifyDataSetChanged();
        }
    }

    @Override 
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.menu_tab_button) {
            this.mUiController.closealltabs();
        } else if (id != R.id.new_tab_button) {
        } else {
            this.mUiController.newTabButtonClicked();
        }
        if (id == R.id.lrBackTab){
            onBackPressed();
        }
    }

    @Override 
    public void tabAdded() {
        ViewAdapter viewAdapter = this.mTabsAdapter;
        if (viewAdapter != null) {
            viewAdapter.notifyItemInserted(getTabsManager().last());
            this.mRecyclerView.postDelayed(new Runnable() { 
                @Override 
                public void run() {
                    try {
                        TabsFragment.this.mRecyclerView.smoothScrollToPosition(TabsFragment.this.mTabsAdapter.getItemCount() - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 300L);
        }
    }

    @Override 
    public void tabRemoved(int i) {
        ViewAdapter viewAdapter = this.mTabsAdapter;
        if (viewAdapter != null) {
            viewAdapter.notifyItemRemoved(i);
        }
    }

    @Override 
    public void tabChanged(int i) {
        ViewAdapter viewAdapter = this.mTabsAdapter;
        if (viewAdapter != null) {
            viewAdapter.notifyItemChanged(i);
        }
    }

    
    public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder> {
        private int mLayoutResourceId;

        private ViewAdapter() {
            this.mLayoutResourceId = R.layout.tab_list_item;
        }

        public void swipeToDelete(int i) {
            TabsFragment.this.mUiController.tabCloseClicked(i);
        }

        @Override 
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(this.mLayoutResourceId, viewGroup, false);
            DrawableUtils.setBackground(inflate, new BackgroundDrawable(inflate.getContext()));
            return new ViewHolder(inflate);
        }

        @Override 
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.exitButton.setTag(Integer.valueOf(i));
            ViewCompat.jumpDrawablesToCurrentState(viewHolder.exitButton);
            BrowserView tabAtPosition = TabsFragment.this.getTabsManager().getTabAtPosition(i);
            if (tabAtPosition == null) {
                return;
            }
            viewHolder.txtTitle.setText(tabAtPosition.getTitle());
            viewHolder.favicon.setImageBitmap(TabsImageView.capture(tabAtPosition.getWebView(), 125.0f, 125.0f, false, Bitmap.Config.RGB_565));
            if (tabAtPosition.isForegroundTab()) {
                viewHolder.txtTitle.setTextColor(Color.parseColor("#5F9FFA"));
                viewHolder.exit.setColorFilter(Color.parseColor("#5F9FFA"));
                viewHolder.faviconbg.setBackgroundColor(Color.parseColor("#5F9FFA"));
                viewHolder.bg.setBackgroundColor(Color.parseColor("#f2f2f2"));
            } else {
                viewHolder.txtTitle.setTextColor(ViewCompat.MEASURED_STATE_MASK);
                viewHolder.exit.setColorFilter(ViewCompat.MEASURED_STATE_MASK);
                viewHolder.faviconbg.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
                viewHolder.bg.setBackgroundColor(Color.parseColor("#00FFFFFF"));
            }
            BackgroundDrawable backgroundDrawable = (BackgroundDrawable) viewHolder.layout.getBackground();
            backgroundDrawable.setCrossFadeEnabled(false);
            if (tabAtPosition.isForegroundTab()) {
                backgroundDrawable.startTransition(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
            } else {
                backgroundDrawable.reverseTransition(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
            }
        }

        @Override 
        public int getItemCount() {
            return TabsFragment.this.getTabsManager().size();
        }

        
        
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @NonNull
            RelativeLayout bg;
            @NonNull
            ImageView exit;
            @NonNull
            FrameLayout exitButton;
            @NonNull
            FrameLayout exitButton_tab;
            @NonNull
            ImageView favicon;
            @NonNull
            LinearLayout faviconbg;
            @NonNull
            LinearLayout layout;
            @NonNull
            LinearLayout layout_tab;
            @NonNull
            TextView txtTitle;

            ViewHolder(@NonNull View view) {
                super(view);
                this.txtTitle = (TextView) view.findViewById(R.id.textTab);
                this.favicon = (ImageView) view.findViewById(R.id.faviconTab);
                this.exit = (ImageView) view.findViewById(R.id.deleteButton);
                this.layout = (LinearLayout) view.findViewById(R.id.tab_item_background);
                this.faviconbg = (LinearLayout) view.findViewById(R.id.tabImageIconLayout);
                this.exitButton = (FrameLayout) view.findViewById(R.id.deleteAction);
                this.bg = (RelativeLayout) view.findViewById(R.id.browserItem);
                this.exitButton.setOnClickListener(this);
                this.layout.setOnClickListener(this);
            }

            @Override 
            public void onClick(View view) {
                if (view == this.exitButton || view == this.exitButton_tab) {
                    TabsFragment.this.mUiController.tabCloseClicked(getAdapterPosition());
                }
                if (view == this.layout || view == this.layout_tab) {
                    TabsFragment.this.mUiController.tabClicked(getAdapterPosition());
                }
            }
        }
    }

  
    public void onBackPressed() {

        int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.getActivity().onBackPressed();
            
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }

    }
}
