package com.rexvit.browser.dialog;

import android.app.Activity;
import android.text.Spanned;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rexvit.browser.R;
import com.rexvit.browser.activities.BaseActivity;
import com.rexvit.browser.utils.DialogUtility;


public final class MessageBox {
    private BaseActivity activity;
    public OnOkListener clickListener;
    public MaterialDialog materialDialog;

    
    public interface OnOkListener {
        void onClick();
    }

    public MessageBox(BaseActivity activity) {
        this.activity = activity;
        this.materialDialog = DialogUtility.getDefaultBuilder(activity).build();
        this.materialDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        this.materialDialog.getBuilder().positiveText(R.string.okay).callback(new MaterialDialog.ButtonCallback() { 
            @Override 
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                MessageBox.this.close();
                if (MessageBox.this.clickListener != null) {
                    MessageBox.this.clickListener.onClick();
                }
            }
        });
    }

    public void close() {
        if (this.materialDialog != null) {
            this.materialDialog.dismiss();
        }
    }

    public Activity getActivity() {
        return this.activity;
    }

    public void setMessage(String message) {
        this.materialDialog = this.materialDialog.getBuilder().content(message).build();
    }

    public void setMessage(Spanned message) {
        this.materialDialog = this.materialDialog.getBuilder().content(message).build();
    }

    public void setTitle(String title) {
        this.materialDialog = this.materialDialog.getBuilder().title(title).build();
    }

    public void show() {
        if (this.materialDialog != null) {
            this.materialDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            this.materialDialog.show();
        }
    }
}
