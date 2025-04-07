package com.rexvit.browser.downloads;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.rexvit.browser.R;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.utils.schedulerUtils.Schedulers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


class FetchUrlMimeType extends Thread {
    private static final String TAG = "FetchUrlMimeType";
    private final Activity mContext;
    private final String mCookies;
    private final DownloadManager.Request mRequest;
    private final String mUri;
    private final String mUserAgent;

    public FetchUrlMimeType(Activity activity, DownloadManager.Request request, String str, String str2, String str3) {
        this.mContext = activity;
        this.mRequest = request;
        this.mUri = str;
        this.mCookies = str2;
        this.mUserAgent = str3;
    }


    class StartDown implements Runnable {
        private final String file;

        StartDown(String file) {
            this.file = file;
        }

        public void run() {
            Context context = mContext;
            String message = context.getString(R.string.starting_download) + " " + this.file;
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void run() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(this.mUri).openConnection();
            if (this.mCookies != null && !this.mCookies.isEmpty()) {
                connection.addRequestProperty("Cookie", this.mCookies);
            }
            connection.setRequestProperty("User-Agent", this.mUserAgent);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String contentType = connection.getHeaderField("Content-Type");
                String contentDisposition = connection.getHeaderField("Content-Disposition");
                String fileName = null;
                if (contentDisposition != null) {
                    fileName = contentDisposition.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
                }
                if (fileName == null) {
                    fileName = URLUtil.guessFileName(this.mUri, null, contentType);
                }
                this.mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                if (contentType != null) {
                    if (contentType.equalsIgnoreCase("text/plain") || contentType.equalsIgnoreCase("application/octet-stream")) {
                        String fileExtension = Utils.guessFileExtension(this.mUri);
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                        if (mimeType != null) {
                            this.mRequest.setMimeType(mimeType);
                        }
                    }
                }
                DownloadManager downloadManager = (DownloadManager) this.mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                downloadManager.enqueue(this.mRequest);
                Schedulers.main().execute(new StartDown(fileName));
            } else {
                
                throw new Exception("Failed to download file. Response code: " + responseCode);
            }
        } catch (MalformedURLException e) {
            
            e.printStackTrace();
        } catch (IOException e) {
            
            e.printStackTrace();
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }


}
