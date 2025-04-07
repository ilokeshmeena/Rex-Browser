package com.rexvit.browser.Interface;

import android.net.Uri;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import com.rexvit.browser.manager.TabsManager;
import com.rexvit.browser.view.BrowserView;


public interface UIController {
    void ASK_ENGINE();

    void BAIDU_ENGINE();

    void BING_ENGINE();

    void DUCKDUCKGO_ENGINE();

    void GOOGLE_ENGINE();

    void YAHOO_ENGINE();

    void YANDEX_ENGINE();
    void LUKAYN_ENGINE();

    void autocomplete();

    void closealltabs();

    void closeapp();
    void showActionBar();

    void desktopSet();

    TabsManager getTabModel();

    void imageOffSet();

    void imageOnSet();

    void mainuigone();

    void newTabButtonClicked();

    void newtab(String str, boolean z);

    void onCloseWindow(BrowserView browserView);

    void onCreateWindow(Message message);

    void onHideCustomView();

    void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback);

    void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback, int i);

    void openFileChooser(ValueCallback<Uri> valueCallback);

    void openTabFromVoice(String str);

    void phoneSet();

    void refreshOrStop();

    void reloadPage();

    String searchtext();

    void serachWebUrl(String str);

    void setForwardButtonEnabled(boolean z);

    void showFileChooser(ValueCallback<Uri[]> valueCallback);

    void showtablayout();

    void tabChanged(BrowserView browserView);

    void tabClicked(int i);

    void tabCloseClicked(int i);

    void updateHistory(@Nullable String str, @NonNull String str2);

    void updateProgress(int i);

    void updateUrl(@Nullable String str, boolean z);
}
