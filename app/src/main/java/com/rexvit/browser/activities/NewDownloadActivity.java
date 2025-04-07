package com.rexvit.browser.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonObject;
import com.rexvit.browser.R;
import com.rexvit.browser.database.DataUpdatedEvent;
import com.rexvit.browser.network.RequestService;
import com.rexvit.browser.utils.FileUtils;
import com.rexvit.browser.utils.Utils;
import com.rexvit.browser.view.AdvancedAppCompatEditText;
import com.tonyodev.fetch2core.FetchCoreUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class NewDownloadActivity extends BaseActivity {
    public static final String DEFAULT_DOWNLOAD_PATH = "prefs_default_download_path";
    public static final String DOWNLOAD_FILE_NAME = "com.rexvit.browser.filename";
    public static final String DOWNLOAD_PATH = "com.rexvit.browser.parts";
    public static final String DOWNLOAD_URL = "com.rexvit.browser.url";
    String downloadFileName;
    String downloadPath;
    String downloadUrl;
    private EditText editTextDownloadPath;
    private static AutoCompleteTextView editTextFileName;
    private static AdvancedAppCompatEditText editTextUrl;
    private MaterialTextView fabNewDownload;
    private Context mContext;
    public String remoteFileName;
    private String remoteFileSize;
    private TextView textInputLayoutFileName;
    public final String TAG = "NewDownloadActivity";
    boolean fromBrowser;
    String instaDirectVideoUrl = "";
    public static String link;
    AdvancedAppCompatEditText.OnTextInteractionListener listener ;
    @Override
    public int getLayoutResId() {
        return R.layout.activity_new_download;
    }

    @Override
    protected void onExit() {
        finish();
    }

    LinearLayout lrLoadPro;
    MaterialTextView  btnCancel;
    View.OnClickListener addNewDownloadListener = new View.OnClickListener() { 
        @Override 
        public void onClick(View view) {
            NewDownloadActivity newDownloadActivity = NewDownloadActivity.this;
            newDownloadActivity.downloadUrl = newDownloadActivity.editTextUrl.getText().toString();
            NewDownloadActivity newDownloadActivity2 = NewDownloadActivity.this;
            if (newDownloadActivity2.isInstagramVideoUrl(newDownloadActivity2.downloadUrl)) {
                NewDownloadActivity newDownloadActivity3 = NewDownloadActivity.this;
                newDownloadActivity3.downloadUrl = newDownloadActivity3.instaDirectVideoUrl;
            }
            NewDownloadActivity newDownloadActivity4 = NewDownloadActivity.this;
            newDownloadActivity4.downloadFileName = newDownloadActivity4.editTextFileName.getText().toString();
            NewDownloadActivity newDownloadActivity5 = NewDownloadActivity.this;
            newDownloadActivity5.downloadPath = newDownloadActivity5.editTextDownloadPath.getText().toString();


            if (FileUtils.isFileExists(downloadFileName,downloadPath)) {
                downloadFileName = FileUtils.autoRenameFile(downloadFileName,downloadPath);
            }




            Intent intent = new Intent(NewDownloadActivity.this.mContext, DownloadsActivity.class);
            intent.putExtra(NewDownloadActivity.DOWNLOAD_URL, NewDownloadActivity.this.downloadUrl);
            intent.putExtra(NewDownloadActivity.DOWNLOAD_FILE_NAME, NewDownloadActivity.this.downloadFileName);
            intent.putExtra(NewDownloadActivity.DOWNLOAD_PATH, NewDownloadActivity.this.downloadPath);
            if (NewDownloadActivity.this.validateNewDownloadInputs()) {
                if (NewDownloadActivity.this.fromBrowser) {
                    NewDownloadActivity.this.startActivity(intent);
                } else {
                    NewDownloadActivity.this.setResult(-1, intent);
                }
                NewDownloadActivity.this.finish();
            }
        }
    };
    View.OnClickListener downloadPathListener = new View.OnClickListener() { 
        @Override 
        public void onClick(View view) {
            NewDownloadActivity.this.startActivity(new Intent(NewDownloadActivity.this.mContext, StorageListActivity.class));
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        link = getLink();
        if (id == R.id.edit_webv){
             String fixedUrl = fixAndValidateUrl(link);
            if (fixedUrl != null) {
                performSearch(fixedUrl);
            } else {
                String inputUrl = editTextUrl.getText().toString();
                String fixedLink = Utils.fixLink(inputUrl);
                performSearch(fixedLink);

            }
        }
    }


    public interface OnUrlGeneratedCallBack {
        void urlGenerated(String str);
    }
    ImageView mIvFolder;
    ImageView OpenBrowser;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        overridePendingTransition(R.anim.onesignal_fade_in, R.anim.activity_fade_out);
        setContentView(R.layout.activity_new_download);
        this.mContext = this;

        link = null;
        btnCancel = findViewById(R.id.btnCancel);
        this.lrLoadPro = findViewById(R.id.lrLoadPro);
        OpenBrowser = (ImageView) findViewById(R.id.edit_webv);

        this.editTextUrl = (AdvancedAppCompatEditText) findViewById(R.id.edit_text_url);
        this.editTextDownloadPath = (EditText) findViewById(R.id.edit_text_download_path);
        this.editTextFileName = (AutoCompleteTextView) findViewById(R.id.edit_text_file_name);
        this.textInputLayoutFileName = (TextView) findViewById(R.id.edit_size);
        this.fabNewDownload = (MaterialTextView) findViewById(R.id.fab_new_download);

        this.mIvFolder = findViewById(R.id.ivFolder);
        findViewById(R.id.edit_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NewDownloadActivity.this, "The feature will be added soon", Toast.LENGTH_LONG).show();
            }
        });



        this.OpenBrowser.setOnClickListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
        this.editTextUrl.setOnTextInteractionListener(this.listener);

        setDownloadDetails(bundle);
        EventBus.getDefault().register(this);
        String str = FileUtils.getDownloadDirectory();
        if (Build.VERSION.SDK_INT >= 28) {
            str = FileUtils.getDownloadDirectory();
        } else {
            mIvFolder.setVisibility(View.GONE);
            this.editTextDownloadPath.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, AppCompatResources.getDrawable(this, R.drawable.ic_folder_dow), (Drawable) null);
            this.editTextDownloadPath.setOnClickListener(this.downloadPathListener);

        }
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.fabNewDownload.setOnClickListener(this.addNewDownloadListener);
        this.editTextDownloadPath.setText(defaultSharedPreferences.getString(DEFAULT_DOWNLOAD_PATH, str));

    }

    public NewDownloadActivity() {
        this.fromBrowser = false;
        this.instaDirectVideoUrl = HttpUrl.FRAGMENT_ENCODE_SET;
        listener = new AdvancedAppCompatEditText.OnTextInteractionListener() {
            @Override
            public void onCopy() {

            }

            @Override
            public void onCut() {

            }

            @Override
            public void onPaste(String str) {
                if (NewDownloadActivity.this.isInstagramVideoUrl(str)) {
                    NewDownloadActivity.this.getDirectVideoUrl(str, new OnUrlGeneratedCallBack() {
                        @Override
                        public final void urlGenerated(String str2) {
                            NewDownloadActivity.this.instaDirectVideoUrl = str;
                            new GetRemoteFileDetails(str).execute(new Void[0]);
                        }
                    });
                } else {
                    new GetRemoteFileDetails(str).execute(new Void[0]);
                }
            }
        };

    }


        @Override
    public void onPause() {
        super.onPause();
    }


    public void setDownloadDetails(Bundle bundle) {
        String dataString = getIntent().getDataString();
        this.downloadUrl = dataString;

        editTextUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String link = NewDownloadActivity.getLink();
                if (link.length() != 0 && NewDownloadActivity.getName().length() == 0) {
                    NewDownloadActivity.setName(Utils.getName(link));
                }

                if (link.length() < 7 || !Utils.FindLink(link)) {
                    OpenBrowser.setVisibility(View.GONE);
                } else {
                    OpenBrowser.setVisibility(View.VISIBLE);
                }
            }
        });

        if (!TextUtils.isEmpty(dataString)) {
            editTextUrl.setText(downloadUrl);
            fromBrowser = true;

            if (isInstagramVideoUrl(downloadUrl)) {
                getDirectVideoUrl(downloadUrl, new OnUrlGeneratedCallBack() {
                    @Override
                    public void urlGenerated(String url) {
                        new GetRemoteFileDetails(url).execute();
                        instaDirectVideoUrl = url;
                    }
                });
            } else {
                new GetRemoteFileDetails(downloadUrl).execute();
            }

            String tag = this.TAG;
            Log.d(tag, "Download path from browser: " + downloadUrl);
        }

        if (bundle == null && "android.intent.action.SEND".equals(getIntent().getAction()) && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            String stringExtra = getIntent().getStringExtra("android.intent.extra.TEXT");
            downloadUrl = stringExtra;

            if (stringExtra != null) {
                if (Patterns.WEB_URL.matcher(downloadUrl).matches()) {
                    editTextUrl.setText(downloadUrl);

                    if (isInstagramVideoUrl(downloadUrl)) {
                        getDirectVideoUrl(downloadUrl, new OnUrlGeneratedCallBack() {
                            @Override
                            public void urlGenerated(String url) {
                                instaDirectVideoUrl = url;
                                new GetRemoteFileDetails(url).execute();
                            }
                        });
                    } else {
                        new GetRemoteFileDetails(downloadUrl).execute();
                    }

                    fromBrowser = true;
                    return;
                }

                Toast.makeText(this, "Invalid download link", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "Invalid download link", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validateNewDownloadInputs() {
        boolean z;
        boolean z2;
        if (Patterns.WEB_URL.matcher(this.downloadUrl).matches()) {
            z = true;
        } else {
            this.editTextUrl.setError(getString(R.string.invalid_url));
            z = false;
        }
        if (FileUtils.isFileNameValid(this, this.downloadFileName)) {
            if (TextUtils.isEmpty(this.downloadFileName)) {
                this.downloadFileName = FileUtils.getFileNameFromUri(this.downloadUrl);
            }
            z2 = true;
        } else {
            this.editTextFileName.setError(getString(R.string.invalid_file_name));
            z2 = false;
        }
        return z && z2;
    }
    public class GetRemoteFileDetails extends AsyncTask<Void, Void, Void> {
        private String mDownloadUrl;

        public GetRemoteFileDetails(String downloadUrl) {
            this.mDownloadUrl = downloadUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lrLoadPro.setVisibility(View.VISIBLE);
            showDownButton(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Connection.Response execute = Jsoup.connect(mDownloadUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .ignoreContentType(true)
                        .execute();

                String header = execute.header("Content-Disposition");
                String header2 = execute.header("Content-Type");
                NewDownloadActivity.this.remoteFileSize = execute.header(FetchCoreUtils.HEADER_CONTENT_LENGTH_LEGACY);
                NewDownloadActivity.this.remoteFileName = URLUtil.guessFileName(mDownloadUrl, header, header2);

                String str = NewDownloadActivity.this.TAG;
                Log.d(str, "Content disposition " + header);
                String str2 = NewDownloadActivity.this.TAG;
                Log.d(str2, "Content type " + header2);
                String str3 = NewDownloadActivity.this.TAG;
                Log.d(str3, "Content length " + NewDownloadActivity.this.remoteFileSize);
                String str4 = NewDownloadActivity.this.TAG;
                Log.d(str4, "Guessed file name " + NewDownloadActivity.this.remoteFileName);

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lrLoadPro.setVisibility(View.GONE);
            showDownButton(true);
            NewDownloadActivity newDownloadActivity = NewDownloadActivity.this;
            newDownloadActivity.downloadFileName = !TextUtils.isEmpty(newDownloadActivity.remoteFileName) ? NewDownloadActivity.this.remoteFileName : URLUtil.guessFileName(mDownloadUrl, null, null);
            NewDownloadActivity.this.editTextFileName.setText(NewDownloadActivity.this.downloadFileName);

            try {
                NewDownloadActivity newDownloadActivity2 = NewDownloadActivity.this;
                newDownloadActivity2.remoteFileSize = newDownloadActivity2.remoteFileSize != null ? Formatter.formatShortFileSize(NewDownloadActivity.this.mContext, Long.valueOf(NewDownloadActivity.this.remoteFileSize).longValue()) : "Unknown";
            } catch (Exception e) {
                NewDownloadActivity.this.remoteFileSize = "Unknown";
                e.printStackTrace();
            }

            TextView textInputLayout = NewDownloadActivity.this.textInputLayoutFileName;
            textInputLayout.setText(" (" + NewDownloadActivity.this.remoteFileSize + ")");
        }
    }
    

    
    
    

    @Subscribe
    public void onDirectorySelected(DataUpdatedEvent.DirectorySelected directorySelected) {
        this.editTextDownloadPath.setText(directorySelected.selectedDirectory);
    }

    
    public boolean isInstagramVideoUrl(String str) {
        return Pattern.matches("https://www\\.instagram\\.com/(p|tv|reel)/[a-zA-Z0-9_-]+/.+", str);
    }

    
    public void getDirectVideoUrl(String str, final OnUrlGeneratedCallBack onUrlGeneratedCallBack) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.instagram.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestService requestService = retrofit.create(RequestService.class);

        Call<JsonObject> call = requestService.getVideoUrl(Uri.parse(str).getPathSegments().get(1));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonObject = response.body();
                    JsonObject graphqlObject = jsonObject.getAsJsonObject("graphql");
                    JsonObject shortcodeMediaObject = graphqlObject.getAsJsonObject("shortcode_media");

                    String mediaType = shortcodeMediaObject.get("__typename").getAsString();
                    String videoUrl = "";

                    switch (mediaType) {
                        case "GraphSidecar":
                        case "GraphImage":
                            videoUrl = shortcodeMediaObject.get("display_url").getAsString();
                            break;
                        case "GraphVideo":
                            videoUrl = shortcodeMediaObject.get("video_url").getAsString();
                            break;
                    }

                    Log.d(TAG, "Media type: " + mediaType);
                    Log.d(TAG, "Final URL: " + videoUrl);

                    onUrlGeneratedCallBack.urlGenerated(videoUrl);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (t.getMessage() != null) {
                    Log.d(TAG, "Error: " + t.getMessage());
                }
            }
        });
    }
    public void showDownButton(boolean bool) {
        if (bool) {
            NewDownloadActivity.this.fabNewDownload.setClickable(true);
            fabNewDownload.setTextColor(getResources().getColor(R.color.colorAccent));
            return;
        }

        NewDownloadActivity.this.fabNewDownload.setClickable(false);
        fabNewDownload.setTextColor(getResources().getColor(R.color.tint_off));
    }

    public static String getLink() {
        return editTextUrl.getText().toString().trim();
    }
    public static void setLink(String text) {
        try {
            editTextUrl.setText(text);
            editTextUrl.setSelection(editTextUrl.length());
        } catch (Throwable th) {
        }
    }
    public static String getName() {
        return editTextFileName.getText().toString().trim();
    }
    public static void setName(String text) {
        try {
            editTextFileName.setText(text);
            editTextFileName.setSelection(editTextFileName.length());
        } catch (Throwable th) {
        }
    }

    private void performSearch(String link) {
        Intent intent = new Intent("android.intent.action.WEB_SEARCH", Uri.parse(link));
        intent.setPackage(getPackageName());
        startActivity(intent);
    }

    public static String fixAndValidateUrl(String inputUrl) {

        String fixedUrl = fixUrl(inputUrl);


        if (isValidUrl(fixedUrl)) {
            return fixedUrl;
        } else {

            return null;
        }
    }
    private static String fixUrl(String url) {
        if (!Patterns.WEB_URL.matcher(url).matches()) {

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
        }
        return url;
    }
    private static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }


}
