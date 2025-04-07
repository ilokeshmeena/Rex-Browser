package com.rexvit.browser.search;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.rexvit.browser.Interface.SuggestionsResult;
import com.rexvit.browser.adapter.SuggestionsAdapter;
import com.rexvit.browser.database.HistoryItem;
import com.rexvit.browser.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;


abstract class BaseSuggestionsTask {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final long INTERVAL_DAY = TimeUnit.DAYS.toMillis(1);
    static final int MAX_RESULTS = 10;
    private static final String TAG = "BaseSuggestionsTask";
    private static final String ERROR_TAG = "RunFunctionError";

    @Nullable
    private static String sLanguage;
    @NonNull
    private final Application mApplication;
    @NonNull
    private String mQuery;
    @NonNull
    private final SuggestionsResult mResultCallback;

    protected abstract String getEncoding();

    protected abstract String getQueryUrl(@NonNull String str, @NonNull String str2);

    protected abstract void parseResults(FileInputStream fileInputStream, List<HistoryItem> list) throws Exception;

    
    public BaseSuggestionsTask(@NonNull String str, @NonNull Application application, @NonNull SuggestionsResult suggestionsResult) {
        this.mQuery = str;
        this.mResultCallback = suggestionsResult;
        this.mApplication = application;
    }
    @NonNull
    private static synchronized String getLanguage() {
        if (sLanguage == null) {
            sLanguage = Locale.getDefault().getLanguage();
        }

        if (TextUtils.isEmpty(sLanguage)) {
            sLanguage = DEFAULT_LANGUAGE;
        }

        return sLanguage;
    }





    void run() {
        List<HistoryItem> arrayList = new ArrayList<>(5);

        try {
            this.mQuery = URLEncoder.encode(this.mQuery, getEncoding());
        } catch (UnsupportedEncodingException e) {
            Log.e(ERROR_TAG, "Unable to encode the URL", e);
        }

        File downloadSuggestionsForQuery = downloadSuggestionsForQuery(this.mQuery, getLanguage(), this.mApplication);

        if (downloadSuggestionsForQuery.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(downloadSuggestionsForQuery)) {
                parseResults(fileInputStream, arrayList);
                post(arrayList);
                return;
            } catch (Exception e) {
                Log.e(ERROR_TAG, "Unable to parse results", e);
                post(arrayList);
            }
        } else {
            post(arrayList);
        }
    }
    private void post(@NonNull List<HistoryItem> list) {
        this.mResultCallback.resultReceived(list);
    }


    private File downloadSuggestionsForQuery(@NonNull String str, String str2, @NonNull Application application) {
        InputStream inputStream = null;
        GZIPInputStream gZIPInputStream = null;
        File file = null;

        try {
            String queryUrl = getQueryUrl(str, str2);
            File cacheDir = application.getCacheDir();
            String fileName = queryUrl.hashCode() + SuggestionsAdapter.CACHE_FILE_TYPE;
            file = new File(cacheDir, fileName);

            if (System.currentTimeMillis() - INTERVAL_DAY < file.lastModified()) {
                return file;
            }

            if (!isNetworkConnected(application)) {
                return file;
            }

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(queryUrl).openConnection();
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestProperty("Accept-Encoding", "gzip");
            httpsURLConnection.setRequestProperty("Accept-Charset", getEncoding());
            httpsURLConnection.connect();

            int responseCode = httpsURLConnection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = httpsURLConnection.getInputStream();
                if (inputStream != null) {
                    gZIPInputStream = new GZIPInputStream(inputStream);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    int read;
                    while ((read = gZIPInputStream.read()) != -1) {
                        fileOutputStream.write(read);
                    }

                    fileOutputStream.flush();
                    fileOutputStream.close();
                    gZIPInputStream.close();
                    inputStream.close();

                    httpsURLConnection.disconnect();
                    file.setLastModified(System.currentTimeMillis());
                }
            } else {
                Log.e(TAG, "Search API Responded with code: " + responseCode);
            }
        } catch (Exception e) {
            Log.w(TAG, "Problem getting search suggestions", e);
        } finally {
            Utils.close(gZIPInputStream);
            Utils.close(inputStream);
        }

        return file;
    }
    private static boolean isNetworkConnected(@NonNull Context context) {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo(context);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Nullable
    private static NetworkInfo getActiveNetworkInfo(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return null;
        }
        return connectivityManager.getActiveNetworkInfo();
    }
}
