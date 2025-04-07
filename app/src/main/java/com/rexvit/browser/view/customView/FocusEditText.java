package com.rexvit.browser.view.customView;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;


public class FocusEditText extends AutoCompleteTextView {
    private boolean mHadVisibility;
    public interface OnSearchActivatedListener {
        void onSearchActivated();
    }

    public interface OnSearchDeactivatedListener {
        void onSearchDeactivated();
    }

    public FocusEditText(Context context) {
        super(context);
        this.mHadVisibility = false;
    }

    public FocusEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHadVisibility = false;
    }

    public FocusEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHadVisibility = false;
    }

    @Override
    protected void onFocusChanged(boolean hasFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(hasFocus, direction, previouslyFocusedRect);
        if (hasFocus) {
            setImeVisibility(true);
            this.mHadVisibility = true;
        } else {
            if (this.mHadVisibility) {
                setImeVisibility(false);
            }
            this.mHadVisibility = false;
        }
    }

    private void setImeVisibility(boolean visible) {
        if (visible) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(2, 1);
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState keyDispatcherState = getKeyDispatcherState();
                if (keyDispatcherState != null) {
                    keyDispatcherState.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState keyDispatcherState = getKeyDispatcherState();
                if (keyDispatcherState != null) {
                    keyDispatcherState.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    clearFocus();
                    setImeVisibility(false);
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnSearchActivatedListener(OnSearchActivatedListener listener) {
        if (listener != null) {
            listener.onSearchActivated();
        }
    }

    public void setOnSearchDeactivatedListener(OnSearchDeactivatedListener listener) {
        if (listener != null) {
            listener.onSearchDeactivated();
        }
    }
}

