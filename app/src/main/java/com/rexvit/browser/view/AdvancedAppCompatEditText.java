package com.rexvit.browser.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;


public class AdvancedAppCompatEditText extends AppCompatEditText {
    private OnTextInteractionListener mOnTextInteractionListener;

    
    public interface OnTextInteractionListener {
        void onCopy();

        void onCut();

        void onPaste(String str);
    }

    public AdvancedAppCompatEditText(Context context) {
        super(context);
    }

    public AdvancedAppCompatEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AdvancedAppCompatEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setOnTextInteractionListener(OnTextInteractionListener onTextInteractionListener) {
        this.mOnTextInteractionListener = onTextInteractionListener;
    }

    @Override 
    public boolean onTextContextMenuItem(int i) {
        super.onTextContextMenuItem(i);
        switch (i) {
            case 16908320:
                onCut();
                return true;
            case 16908321:
                onCopy();
                return true;
            case 16908322:
                onPaste();
                return true;
            default:
                return true;
        }
    }

    public void onCut() {
        OnTextInteractionListener onTextInteractionListener = this.mOnTextInteractionListener;
        if (onTextInteractionListener != null) {
            onTextInteractionListener.onCut();
        }
    }

    public void onCopy() {
        OnTextInteractionListener onTextInteractionListener = this.mOnTextInteractionListener;
        if (onTextInteractionListener != null) {
            onTextInteractionListener.onCopy();
        }
    }

    public void onPaste() {
        if (this.mOnTextInteractionListener != null) {
            this.mOnTextInteractionListener.onPaste(getText() != null ? getText().toString() : "");
        }
    }
}
