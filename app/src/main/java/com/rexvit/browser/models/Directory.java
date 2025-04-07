package com.rexvit.browser.models;


public class Directory {
    private int items;
    private String name;
    private String path;

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public int getItems() {
        return this.items;
    }

    public void setItems(int i) {
        this.items = i;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String str) {
        this.path = str;
    }
}
