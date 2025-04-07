package com.rexvit.browser.taskUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.webkit.WebView;
import com.rexvit.browser.R;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.utils.ViewUnit;


public class ScreenshotTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private WebView webView;
    private ProgressDialog dialog = null;
    private int windowWidth = 0;
    private float contentHeight = 0.0f;
    private String title = null;
    private String path = null;

    public ScreenshotTask(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    @Override
    protected void onPreExecute() {
        this.dialog = new ProgressDialog(this.context, R.style.AppThemeDialog);
        this.dialog.setCancelable(false);
        this.dialog.setProgressStyle(0);
        this.dialog.setMessage(this.context.getString(R.string.toast_wait_a_minute));
        this.dialog.show();
        this.windowWidth = ViewUnit.getWindowWidth(this.context);
        this.contentHeight = this.webView.getContentHeight() * ViewUnit.getDensity(this.context);
        this.title = this.webView.getTitle();
    }


    @Override
    public Boolean doInBackground(Void... voidArr) {
        boolean z = false;
        try {
            this.path = ViewUnit.screenshot(this.context, ViewUnit.capture(this.webView, this.windowWidth, this.contentHeight, false, Bitmap.Config.ARGB_8888), this.title);
        } catch (Exception unused) {
            this.path = null;
        }
        String str = this.path;
        if (str != null && !str.isEmpty()) {
            z = true;
        }
        return Boolean.valueOf(z);
    }


    @Override
    public void onPostExecute(Boolean bool) {
        this.dialog.hide();
        this.dialog.dismiss();
        if (bool.booleanValue()) {
            Utils.msgLong(this.context.getString(R.string.save_screenshot) + this.path, this.context);
            return;
        }
        Utils.msg(this.context.getString(R.string.no_save_sacreenshot), this.context);
    }
}
