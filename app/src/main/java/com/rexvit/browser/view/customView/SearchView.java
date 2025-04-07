package com.rexvit.browser.view.customView;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;


public class SearchView extends AppCompatAutoCompleteTextView {
    private boolean mIsBeingClicked;
    @Nullable
    private PreFocusListener mListener;
    private long mTimePressed;

    
    public interface PreFocusListener {
        void onPreFocus();
    }

    public SearchView(Context context) {
        super(context);
    }

    public SearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SearchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setOnPreFocusListener(@Nullable PreFocusListener preFocusListener) {
        this.mListener = preFocusListener;
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        PreFocusListener preFocusListener;
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mTimePressed = System.currentTimeMillis();
            this.mIsBeingClicked = true;
        } else if (action != 1) {
            if (action == 3) {
                this.mIsBeingClicked = false;
            }
        } else if (this.mIsBeingClicked && !isLongPress() && (preFocusListener = this.mListener) != null) {
            preFocusListener.onPreFocus();
        }
        return super.onTouchEvent(motionEvent);
    }

    private boolean isLongPress() {
        return System.currentTimeMillis() - this.mTimePressed >= ((long) ViewConfiguration.getLongPressTimeout());
    }
}
