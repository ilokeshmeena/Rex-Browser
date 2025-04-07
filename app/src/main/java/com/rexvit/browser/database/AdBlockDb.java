package com.rexvit.browser.database;

import com.orm.SugarRecord;


public class AdBlockDb extends SugarRecord {
    private int color;
    private String icon;
    private String name;
    private String url;

    public int getColor() {
        return this.color;
    }

    public void setColor(int i) {
        this.color = i;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public AdBlockDb(String str, String str2, String str3, int i) {
        this.icon = str;
        this.name = str2;
        this.url = str3;
        this.color = i;
    }

    public AdBlockDb() {
    }

    public String toString() {
        return this.name;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String str) {
        this.icon = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }
}
