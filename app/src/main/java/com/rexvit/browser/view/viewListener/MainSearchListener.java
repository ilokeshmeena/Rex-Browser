package com.rexvit.browser.view.viewListener;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.SuggestionsAdapter;
import com.rexvit.browser.utils.UrlUtils;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.view.customView.FocusEditText;


public class MainSearchListener implements TextView.OnEditorActionListener, View.OnFocusChangeListener, View.OnKeyListener {
    private Context c;
    private FrameLayout mIconLayout;
    private FocusEditText mMainSearchBar;
    private UIController mUiController;

    public MainSearchListener(Context context, FocusEditText focusEditText, FrameLayout frameLayout) {
        this.c = context;
        this.mUiController = (UIController) context;
        this.mMainSearchBar = focusEditText;
        this.mIconLayout = frameLayout;
    }

    public MainSearchListener() {
    }

    @Override 
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (i == 0) {
            inputMethodManager.hideSoftInputFromWindow(this.mMainSearchBar.getWindowToken(), 0);
            this.mIconLayout.setVisibility(View.VISIBLE);
        } else if (i == 66) {
            String obj = this.mMainSearchBar.getText().toString();
            inputMethodManager.hideSoftInputFromWindow(this.mMainSearchBar.getWindowToken(), 0);
            final String str = this.mUiController.searchtext() + UrlUtils.QUERY_PLACE_HOLDER;
            final String trim = obj.trim();
            new Handler().postDelayed(new Runnable() { 
                @Override 
                public void run() {
                    MainSearchListener.this.mUiController.newtab(UrlUtils.smartUrlFilter(trim, true, str), true);
                    MainSearchListener.this.mUiController.mainuigone();
                    MainSearchListener.this.mMainSearchBar.setText("");
                    MainSearchListener.this.mMainSearchBar.clearFocus();
                }
            }, 300L);
            this.mIconLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override 
    public void onFocusChange(View view, boolean z) {
        if (z) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(800L);
            alphaAnimation.setFillAfter(true);
            AlphaAnimation alphaAnimation2 = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation2.setDuration(800L);
            alphaAnimation2.setFillAfter(true);
            this.mIconLayout.setVisibility(View.GONE);
            suggestions();
            return;
        }
        this.mMainSearchBar.clearFocus();
        this.mIconLayout.setVisibility(View.VISIBLE);
        ((InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mMainSearchBar.getWindowToken(), 0);
    }

    @Override 
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        String obj = this.mMainSearchBar.getText().toString();
        if (i != 2 && i != 6 && i != 5 && i != 4 && i != 3) {
            try {
                if (keyEvent.getAction() != 66) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        if (this.mMainSearchBar != null && !this.mMainSearchBar.getText().toString().isEmpty()) {
            ((InputMethodManager) this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.mMainSearchBar.getWindowToken(), 0);
            final String str = this.mUiController.searchtext() + UrlUtils.QUERY_PLACE_HOLDER;
            final String trim = obj.trim();
            new Handler().postDelayed(new Runnable() { 
                @Override 
                public void run() {
                    MainSearchListener.this.mUiController.newtab(UrlUtils.smartUrlFilter(trim, true, str), true);
                    MainSearchListener.this.mUiController.mainuigone();
                    MainSearchListener.this.mMainSearchBar.setText("");
                    MainSearchListener.this.mMainSearchBar.clearFocus();
                }
            }, 300L);
            this.mIconLayout.setVisibility(View.VISIBLE);
            return true;
        }
        Utils.msg(this.c.getString(R.string.search_empty), this.c);
        return true;
    }

    private void suggestions() {
        SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(this.c);
        this.mMainSearchBar.setThreshold(1);
        this.mMainSearchBar.setDropDownWidth(-1);
        this.mMainSearchBar.setDropDownVerticalOffset(0);
        this.mMainSearchBar.setDropDownAnchor(R.id.searchLayoutUp);
        this.mMainSearchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
            @Override 
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                CharSequence text;
                CharSequence text2 = ((TextView) view.findViewById(R.id.url)).getText();
                String charSequence = text2 != null ? text2.toString() : null;
                if (charSequence == null && (text = ((TextView) view.findViewById(R.id.title)).getText()) != null) {
                    charSequence = text.toString();
                }
                if (charSequence == null) {
                    return;
                }
                MainSearchListener.this.mMainSearchBar.setText("");
                MainSearchListener.this.mUiController.newtab(UrlUtils.smartUrlFilter(charSequence.trim(), true, MainSearchListener.this.mUiController.searchtext() + UrlUtils.QUERY_PLACE_HOLDER), true);
                MainSearchListener.this.mUiController.mainuigone();
                ((InputMethodManager) MainSearchListener.this.c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(MainSearchListener.this.mMainSearchBar.getWindowToken(), 0);
                MainSearchListener.this.mUiController.autocomplete();
            }
        });
        this.mMainSearchBar.setSelectAllOnFocus(true);
        this.mMainSearchBar.setAdapter(suggestionsAdapter);
    }
}
