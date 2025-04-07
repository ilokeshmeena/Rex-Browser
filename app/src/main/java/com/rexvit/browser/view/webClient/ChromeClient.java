package com.rexvit.browser.view.webClient;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.utils.animUtils.Preconditions;
import com.rexvit.browser.view.BrowserView;


public class ChromeClient extends WebChromeClient {
    @NonNull
    private final Activity mActivity;
    @NonNull
    private final BrowserView mBrowserView;
    @NonNull
    private final UIController mUIController;

    public ChromeClient(@NonNull Activity activity, @NonNull BrowserView browserView) {
        Preconditions.checkNonNull(activity);
        Preconditions.checkNonNull(browserView);
        this.mActivity = activity;
        this.mUIController = (UIController) activity;
        this.mBrowserView = browserView;
    }

    @Override 
    public void onProgressChanged(WebView webView, int i) {
        if (this.mBrowserView.isShown()) {
            this.mUIController.updateProgress(i);
        }
    }

    @Override 
    public void onReceivedIcon(@NonNull WebView webView, Bitmap bitmap) {
        this.mBrowserView.getTitleInfo().setFavicon(bitmap);
        this.mUIController.tabChanged(this.mBrowserView);
    }

    @Override 
    public void onReceivedTitle(@Nullable WebView webView, @Nullable String str) {
        if (str != null && !str.isEmpty()) {
            this.mBrowserView.getTitleInfo().setTitle(str);
        } else {
            this.mBrowserView.getTitleInfo().setTitle(this.mActivity.getString(R.string.untitled));
        }
        this.mUIController.tabChanged(this.mBrowserView);
        if (webView == null || webView.getUrl() == null) {
            return;
        }
        this.mUIController.updateHistory(str, webView.getUrl());
    }

    @Override 
    public void onGeolocationPermissionsShowPrompt(@NonNull String str, @NonNull GeolocationPermissions.Callback callback) {
        callback.invoke(str, false, true);
    }

    @Override 
    public boolean onCreateWindow(WebView webView, boolean z, boolean z2, Message message) {
        this.mUIController.onCreateWindow(message);
        return true;
    }

    @Override 
    public void onCloseWindow(WebView webView) {
        this.mUIController.onCloseWindow(this.mBrowserView);
    }

    public void openFileChooser(ValueCallback<Uri> valueCallback) {
        this.mUIController.openFileChooser(valueCallback);
    }

    public void openFileChooser(ValueCallback<Uri> valueCallback, String str) {
        this.mUIController.openFileChooser(valueCallback);
    }

    public void openFileChooser(ValueCallback<Uri> valueCallback, String str, String str2) {
        this.mUIController.openFileChooser(valueCallback);
    }

    @Override 
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        this.mUIController.showFileChooser(valueCallback);
        return true;
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return BitmapFactory.decodeResource(this.mActivity.getResources(), R.drawable.default_video_poster);


    }

    @Override 
    public View getVideoLoadingProgressView() {
        return LayoutInflater.from(this.mActivity).inflate(R.layout.video_loading_progress, (ViewGroup) null);
    }

    @Override 
    public void onHideCustomView() {
        this.mUIController.onHideCustomView();
    }

    @Override 
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback) {
        this.mUIController.onShowCustomView(view, customViewCallback);
    }

    @Override 
    public void onShowCustomView(View view, int i, WebChromeClient.CustomViewCallback customViewCallback) {
        this.mUIController.onShowCustomView(view, customViewCallback, i);
    }



    private void JsAlert(String url, String message, final JsResult result, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(Utils.getTitleFromUrl(url))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.confirm();
                    }
                });

        builder.show();
    }

    @Override
    public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
        JsAlert(url, message, result, mActivity);
        return true;
    }
    @Override
    public boolean onJsConfirm(WebView webView, String url, String message, final JsResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                .setTitle(Utils.getTitleFromUrl(url))
                .setMessage(message)
                .setPositiveButton(mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.confirm();
                    }
                })
                .setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.cancel();
                    }
                });

        builder.show();
        return true;
    }
}
