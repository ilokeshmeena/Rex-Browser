package com.rexvit.browser.database;

import com.orm.SugarRecord;


public class BookmarksDb extends SugarRecord {
    private String bookmarks;
    private String icon;
    private int iconColor;
    private String time;
    private String title;

    public int getIconColor() {
        return this.iconColor;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String str) {
        this.time = str;
    }

    public void setIconColor(int i) {
        this.iconColor = i;
    }

    
    public String getIcon() {
        return this.icon;
    }

    public void setIcon( String str) {
        this.icon = str;
    }

    
    public String getTitle() {
        return this.title;
    }

    public void setTitle( String str) {
        this.title = str;
    }

    public String toString() {
        return this.bookmarks;
    }

    public BookmarksDb() {
    }

    public BookmarksDb( String str,  String str2,  String str3, int i, String str4) {
        this.bookmarks = str;
        this.title = str2;
        this.icon = str3;
        this.iconColor = i;
        this.time = str4;
    }

    
    public String getBookmarks() {
        return this.bookmarks;
    }

    public void setBookmarks( String str) {
        this.bookmarks = str;
    }
}
