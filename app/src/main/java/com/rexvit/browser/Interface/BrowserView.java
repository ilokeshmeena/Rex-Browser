package com.rexvit.browser.Interface;

import android.content.DialogInterface;

import android.view.View;

import androidx.annotation.NonNull;


public interface BrowserView {
    void closeActivity();

    void closeBrowser();

    void notifyTabViewAdded();

    void notifyTabViewChanged(int i);

    void notifyTabViewInitialized();

    void notifyTabViewRemoved(int i);

    void removeTabView();

    void setForwardButtonEnabled(boolean z);

    void setTabView(@NonNull View view);

    void showBlockedLocalFileDialog(DialogInterface.OnClickListener onClickListener);

    void updateProgress(int i);

    void updateTabNumber(int i);

    void updateUrl(String str, boolean z);
}
