package com.rexvit.browser.downloads;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.format.Formatter;
import android.webkit.DownloadListener;

import androidx.appcompat.app.AlertDialog;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.rexvit.browser.activities.NewDownloadActivity;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.utils.FileUtils;


public class DownloadStart implements DownloadListener {
    
    boolean isAllPermissionDailogeShow = false;
    AlertDialog.Builder AllFilePermission;
    private final Activity mActivity;
    private static final String TAG = "DownloadStart";

    public DownloadStart(Activity activity) {
        BrowserApp.getAppComponent().inject(this);
        this.mActivity = activity;
    }
    @Override
    @TargetApi(16)
    public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
        
        if (Build.VERSION.SDK_INT >= 31) {
            String fileName = FileUtils.getFileNameFromUri(url); 
            String downloadPath = FileUtils.getDownloadDirectory(); 

            if (FileUtils.isFileExists(fileName, downloadPath)) {
                
                showConfirmationDialog(fileName, downloadPath, url);
            } else {
                
                
                try {
                    startDownloadActivity(url);
                } catch (Exception e) {
                    DownloadHandler.onDownloadStart(mActivity, url, userAgent, contentDisposition, mimetype, contentLength > 0 ? Formatter.formatFileSize(mActivity, contentLength) : "Unknown size");
                    throw new RuntimeException(e);
                }
            }

        }
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this.mActivity, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, new PermissionsResultAction() {
            @Override
            public void onDenied(String permission) {
                
            }

            @Override
            public void onGranted() {
                
                String fileName = FileUtils.getFileNameFromUri(url); 
                String downloadPath = FileUtils.getDownloadDirectory(); 

                if (FileUtils.isFileExists(fileName, downloadPath)) {
                    
                    showConfirmationDialog(fileName, downloadPath, url);
                } else {
                    
                    

                    try {
                        startDownloadActivity(url);
                    } catch (Exception e) {
                        DownloadHandler.onDownloadStart(mActivity, url, userAgent, contentDisposition, mimetype, contentLength > 0 ? Formatter.formatFileSize(mActivity, contentLength) : "Unknown size");
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void showConfirmationDialog(final String fileName, final String downloadPath, final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Confirmation for Redownload")
                .setMessage("The file has already been downloaded. Do you want to redownload it?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    
                    startDownloadActivity(url);
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    
                    
                    dialog.dismiss();
                })
                .show();
    }

    private void startDownloadActivity(String url) {
        
        try {
            Intent intent = new Intent(DownloadStart.this.mActivity, NewDownloadActivity.class);
            intent.setData(Uri.parse(url));
            DownloadStart.this.mActivity.startActivity(intent);
        } catch (Exception e) {
            

            
            throw new RuntimeException(e);
        }
    }



}
