package com.rexvit.browser.callbacks;

import android.net.NetworkInfo;


public interface DownloadCallback {
    public interface Progress {
        public static final int CONNECT_SUCCESS = 0;
        public static final int ERROR = -1;
        public static final int GET_INPUT_STREAM_SUCCESS = 1;
        public static final int PROCESS_INPUT_STREAM_IN_PROGRESS = 2;
        public static final int PROCESS_INPUT_STREAM_SUCCESS = 3;
    }

    void finishDownloading();

    NetworkInfo getActiveNetworkInfo();

    void onProgressUpdate(int i, int i2);

    void updateFromDownload(String str);
}
