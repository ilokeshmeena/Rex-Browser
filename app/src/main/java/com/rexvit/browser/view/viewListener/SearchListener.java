package com.rexvit.browser.view.viewListener;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.view.BrowserView;
import com.rexvit.browser.view.customView.SearchView;


public class SearchListener implements View.OnKeyListener, TextView.OnEditorActionListener, View.OnFocusChangeListener, SearchView.PreFocusListener {
    private Context c;
    private SearchView mSearch;
    private TabsManager mTabsManager;
    private UIController mUiController;
    private ViewGroup mUiLayout;
    SwipeRefreshLayout mSwipe;
    public SearchListener() {
    }

    public SearchListener(Context context, Activity activity, TabsManager tabsManager, SearchView searchView, ViewGroup viewGroup, SwipeRefreshLayout swipeRefreshLayout) {
        this.c = context;
        this.mUiController = (UIController) context;
        this.mTabsManager = tabsManager;
        this.mSearch = searchView;
        this.mUiLayout = viewGroup;
        this.mSwipe = swipeRefreshLayout;

    }

    @Override 
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i != 66) {
            return false;
        }
        ((InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mSearch.getWindowToken(), 0);
        this.mUiController.serachWebUrl(this.mSearch.getText().toString());
        BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.requestFocus();
            return true;
        }
        return true;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 2 || i == 6 || i == 5 || i == 4 || i == 3 || keyEvent.getAction() == 66) {
            ((InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mSearch.getWindowToken(), 0);
            this.mUiController.serachWebUrl(this.mSearch.getText().toString());
            BrowserView currentTab = this.mTabsManager.getCurrentTab();
            if (currentTab != null) {
                currentTab.requestFocus();
                return true;
            }
            return true;
        }
        return false;
    }
    /*
    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == 2 || i == 6 || i == 5 || i == 4 || i == 3 || keyEvent.getAction() == 66) {
            String searchText = mSearch.getText().toString();
            if (searchText.contains(".")) {
                // Append "https://" to the beginning of the search text
                searchText = "https://" + searchText;
            }

            ((InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mSearch.getWindowToken(), 0);
            this.mUiController.serachWebUrl(searchText);
            BrowserView currentTab = this.mTabsManager.getCurrentTab();
            if (currentTab != null) {
                currentTab.requestFocus();
                return true;
            }
            return true;
        }
        return false;
    }

     */
    @Override 
    public void onFocusChange(View view, boolean z) {
        BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab != null) {
            if (z) {
                this.mSearch.selectAll();
                return;
            }
            this.mUiController.updateUrl(currentTab.getUrl(), true);
            this.mSwipe.setRefreshing(false);
            this.mUiLayout.requestFocus();
            this.mSearch.clearFocus();

            ((InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mSearch.getWindowToken(), 0);
        }
    }

    @Override 
    public void onPreFocus() {
        BrowserView currentTab = this.mTabsManager.getCurrentTab();
        if (currentTab == null) {
            return;
        }
        String url = currentTab.getUrl();
        if (Build.VERSION.SDK_INT >= 24) {
            this.mSearch.setText(Html.fromHtml(Utils.urlWrapper(url), 0), TextView.BufferType.SPANNABLE);
        } else {
            this.mSearch.setText(Html.fromHtml(Utils.urlWrapper(url)), TextView.BufferType.SPANNABLE);
        }
        this.mSwipe.setRefreshing(false);
    }
}
