package com.rexvit.browser.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rexvit.browser.R;
import com.rexvit.browser.activities.BaseActivity;


public class DialogUtility {
    public static MaterialDialog.Builder getDefaultBuilder(BaseActivity activity) {
        return new MaterialDialog.Builder(activity).typeface(Font.LatoMedium, Font.LatoRegular).contentColor(activity.getColorFrom(R.color.black_light)).negativeColor(activity.getColorFrom(R.color.black_lighter)).neutralColor(activity.getColorFrom(R.color.black_lighter));
    }

    public static void setDialogWindowAnimation(MaterialDialog dialog) {
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public static Dialog generateNewDialog(Activity activity, int layout, boolean enableAnimation) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(layout);
        fillParent(dialog);
        return dialog;
    }

    public static void fillParent(Dialog dialog) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = -1;
        dialog.getWindow().setAttributes(params);
    }
}
