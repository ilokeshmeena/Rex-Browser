package com.rexvit.browser.utils;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.rexvit.browser.R;


public class FetchTitle {
    @NonNull
    private final Context mContext;
    @Nullable
    private Bitmap mFavicon = null;
    @NonNull
    private String mTitle;

    public FetchTitle(@NonNull Context context) {
        this.mContext = context;
        this.mTitle = context.getString(R.string.action_new_tab);
    }

    public void setFavicon(@Nullable Bitmap bitmap) {
        if (bitmap == null) {
            this.mFavicon = null;
        } else {
            this.mFavicon = Utils.padFavicon(bitmap);
        }
    }

    public void setTitle(@Nullable String str) {
        if (str == null) {
            this.mTitle = "";
        } else {
            this.mTitle = str;
        }
    }

    @NonNull
    public String getTitle() {
        return this.mTitle;
    }
}
