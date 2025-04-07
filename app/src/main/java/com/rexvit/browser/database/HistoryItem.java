package com.rexvit.browser.database;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.rexvit.browser.utils.Preconditions;


public class HistoryItem implements Comparable<HistoryItem> {
    @Nullable
    private Bitmap mBitmap;
    @NonNull
    private String mFolder;
    private int mImageId;
    private boolean mIsFolder;
    private int mOrder;
    @NonNull
    private String mTitle;
    @NonNull
    private String mUrl;

    public HistoryItem() {
        this.mUrl = "";
        this.mTitle = "";
        this.mFolder = "";
        this.mBitmap = null;
        this.mImageId = 0;
        this.mOrder = 0;
        this.mIsFolder = false;
    }

    public HistoryItem(@NonNull String str, @NonNull String str2) {
        this.mUrl = "";
        this.mTitle = "";
        this.mFolder = "";
        this.mBitmap = null;
        this.mImageId = 0;
        this.mOrder = 0;
        this.mIsFolder = false;
        Preconditions.checkNonNull(str);
        Preconditions.checkNonNull(str2);
        this.mUrl = str;
        this.mTitle = str2;
        this.mBitmap = null;
    }

    public HistoryItem(@NonNull String str, @NonNull String str2, int i) {
        this.mUrl = "";
        this.mTitle = "";
        this.mFolder = "";
        this.mBitmap = null;
        this.mImageId = 0;
        this.mOrder = 0;
        this.mIsFolder = false;
        Preconditions.checkNonNull(str);
        Preconditions.checkNonNull(str2);
        this.mUrl = str;
        this.mTitle = str2;
        this.mBitmap = null;
        this.mImageId = i;
    }

    public int getImageId() {
        return this.mImageId;
    }

    public void setImageId(int i) {
        this.mImageId = i;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Nullable
    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    @NonNull
    public String getUrl() {
        return this.mUrl;
    }

    public void setUrl(@Nullable String str) {
        if (str == null) {
            str = "";
        }
        this.mUrl = str;
    }

    @NonNull
    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(@Nullable String str) {
        if (str == null) {
            str = "";
        }
        this.mTitle = str;
    }

    @NonNull
    public String toString() {
        return this.mTitle;
    }

    @Override 
    public int compareTo(@NonNull HistoryItem historyItem) {
        int compareTo = this.mTitle.compareTo(historyItem.mTitle);
        return compareTo == 0 ? this.mUrl.compareTo(historyItem.mUrl) : compareTo;
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof HistoryItem)) {
            HistoryItem historyItem = (HistoryItem) obj;
            return this.mImageId == historyItem.mImageId && this.mTitle.equals(historyItem.mTitle) && this.mUrl.equals(historyItem.mUrl) && this.mFolder.equals(historyItem.mFolder);
        }
        return false;
    }

    public int hashCode() {
        return (((((((this.mUrl.hashCode() * 31) + this.mImageId) * 31) + this.mTitle.hashCode()) * 32) + this.mFolder.hashCode()) * 31) + this.mImageId;
    }
}
