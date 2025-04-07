package com.rexvit.browser.view.webClient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.constant.Constants;
import com.rexvit.browser.utils.AdBlock;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.utils.animUtils.Preconditions;
import com.rexvit.browser.view.BrowserView;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class WebClient extends WebViewClient {
    @NonNull
    private final Activity mActivity;
    @Inject
    AdBlock mAdBlock;
    @NonNull
    private final BrowserView mBrowserView;
    @NonNull
    private final UIController mUIController;
    private volatile boolean mIsRunning = false;
    private float mZoomScale = 0.0f;

    public WebClient(@NonNull Activity activity, @NonNull BrowserView browserView) {
        BrowserApp.getAppComponent().inject(this);
        Preconditions.checkNonNull(activity);
        Preconditions.checkNonNull(browserView);
        this.mActivity = activity;
        this.mUIController = (UIController) activity;
        this.mBrowserView = browserView;
        this.mAdBlock.updatePreference();
    }

    @NonNull
    private static List<Integer> getAllSslErrorMessageCodes(@NonNull SslError sslError) {
        ArrayList arrayList = new ArrayList(1);
        if (sslError.hasError(4)) {
            arrayList.add(Integer.valueOf((int) R.string.message_certificate_date_invalid));
        }
        if (sslError.hasError(1)) {
            arrayList.add(Integer.valueOf((int) R.string.message_certificate_expired));
        }
        if (sslError.hasError(2)) {
            arrayList.add(Integer.valueOf((int) R.string.message_certificate_domain_mismatch));
        }
        if (sslError.hasError(0)) {
            arrayList.add(Integer.valueOf((int) R.string.message_certificate_not_yet_valid));
        }
        if (sslError.hasError(3)) {
            arrayList.add(Integer.valueOf((int) R.string.message_certificate_untrusted));
        }
        if (sslError.hasError(5)) {
            arrayList.add(Integer.valueOf((int) R.string.message_certificate_invalid));
        }
        return arrayList;
    }


    @Override
    @TargetApi(21)
    public WebResourceResponse shouldInterceptRequest(WebView webView, @NonNull WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (mAdBlock.isAd(url)) {
            return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
        }
        return super.shouldInterceptRequest(webView, request);
    }

    @Override
    @TargetApi(20)
    @Nullable
    public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {
        if (mAdBlock.isAd(url)) {
            return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
        }
        return null;
    }

    @Override
    @TargetApi(19)
    public void onPageFinished(@NonNull WebView webView, String url) {
        SwipeRefreshLayout swipeRefreshLayout = mActivity.findViewById(R.id.swipeMAin);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (webView.isShown()) {
            mUIController.updateUrl(url, true);
            mUIController.setForwardButtonEnabled(webView.canGoForward());
            webView.postInvalidate();
        }
        String pageTitle = webView.getTitle();
        if (pageTitle == null || pageTitle.isEmpty()) {
            mBrowserView.getTitleInfo().setTitle(mActivity.getString(R.string.untitled));
        } else {
            mBrowserView.getTitleInfo().setTitle(pageTitle);
        }
        mUIController.tabChanged(mBrowserView);
    }
    @Override 
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        this.mBrowserView.getTitleInfo().setFavicon(null);
        if (this.mBrowserView.isShown()) {
            this.mUIController.updateUrl(str, false);
        }
        this.mUIController.tabChanged(this.mBrowserView);
    }


    @Override
    public void onReceivedHttpAuthRequest(WebView webView, @NonNull final HttpAuthHandler httpAuthHandler, String host, String realm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final EditText usernameEditText = new EditText(mActivity);
        final EditText passwordEditText = new EditText(mActivity);
        LinearLayout linearLayout = new LinearLayout(mActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(usernameEditText);
        linearLayout.addView(passwordEditText);
        usernameEditText.setHint(mActivity.getString(R.string.hint_username));
        usernameEditText.setSingleLine();
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEditText.setSingleLine();
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordEditText.setHint(mActivity.getString(R.string.hint_password));
        builder.setTitle(mActivity.getString(R.string.title_sign_in));
        builder.setView(linearLayout);
        builder.setCancelable(true)
                .setPositiveButton(mActivity.getString(R.string.title_sign_in), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        httpAuthHandler.proceed(usernameEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
                    }
                })
                .setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        httpAuthHandler.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    @TargetApi(19)
    public void onScaleChanged(@NonNull final WebView webView, float oldScale, final float newScale) {
        if (!webView.isShown() || Build.VERSION.SDK_INT < 19 || mIsRunning || Math.abs(100.0f - ((100.0f / mZoomScale) * newScale)) <= 2.5f || mIsRunning) {
            return;
        }
        mIsRunning = webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mZoomScale = newScale;
                webView.evaluateJavascript(Constants.JAVASCRIPT_TEXT_REFLOW, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String str) {
                        mIsRunning = false;
                    }
                });
            }
        }, 100L);
    }
       @Override
    public void onFormResubmission(WebView webView, @NonNull final Message dontResend, @NonNull final Message resend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.title_form_resubmission))
                .setMessage(mActivity.getString(R.string.message_form_resubmission))
                .setPositiveButton(mActivity.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resend.sendToTarget();
                    }
                })
                .setNegativeButton(mActivity.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dontResend.sendToTarget();
                    }
                });

        builder.show();
    }
    @Override
    @TargetApi(21)
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
        String url = webResourceRequest.getUrl().toString();
        return shouldOverrideLoading(webView, url) || super.shouldOverrideUrlLoading(webView, webResourceRequest);
    }


    @Override 
    public boolean shouldOverrideUrlLoading(@NonNull WebView webView, @NonNull String str) {
        return shouldOverrideLoading(webView, str) || super.shouldOverrideUrlLoading(webView, str);
    }


    private boolean shouldOverrideLoading(WebView webView, String str) {
        Map<String, String> requestHeaders = this.mBrowserView.getRequestHeaders();
        if (requestHeaders.isEmpty()) {
            if (str.startsWith(Constants.ABOUT)) {
                return false;
            }
            return isMailOrIntent(str, webView);
        } else if (Utils.doesSupportHeaders()) {
            webView.loadUrl(str, requestHeaders);
            return true;
        } else if (isMailOrIntent(str, webView)) {
            return true;
        } else {
            if (Utils.doesSupportHeaders()) {
                webView.loadUrl(str, requestHeaders);
                return true;
            }
            return false;
        }
    }

    private boolean isMailOrIntent(@NonNull String str, @NonNull WebView webView) {
        Intent intent;
        if (str.startsWith("mailto:")) {
            MailTo parse = MailTo.parse(str);
            this.mActivity.startActivity(Utils.newEmailIntent(parse.getTo(), parse.getSubject(), parse.getBody(), parse.getCc()));
            webView.reload();
            return true;
        } else if (str.startsWith("intent://")) {
            try {
                intent = Intent.parseUri(str, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException unused) {
                intent = null;
            }
            if (intent != null) {
                intent.addCategory("android.intent.category.BROWSABLE");
                intent.setComponent(null);
                if (Build.VERSION.SDK_INT >= 15) {
                    intent.setSelector(null);
                }
                try {

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.mActivity.startActivity(intent);
                } catch (ActivityNotFoundException unused2) {
                    Log.e(Constants.TAG, "ActivityNotFoundException");
                }
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override 
    @TargetApi(21)
    public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
        super.onReceivedError(webView, webResourceRequest, webResourceError);
    }

    @Override 
    public void onReceivedError(WebView webView, int i, String str, String str2) {
        super.onReceivedError(webView, i, str, str2);
    }
}
