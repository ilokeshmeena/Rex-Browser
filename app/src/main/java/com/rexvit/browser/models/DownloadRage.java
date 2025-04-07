package com.rexvit.browser.models;


public class DownloadRage {
    private int end;
    private int partFileSize;
    private int star;

    public DownloadRage() {
    }

    public DownloadRage(int i, int i2, int i3) {
        this.star = i;
        this.end = i2;
        this.partFileSize = i3;
    }

    public int getStar() {
        return this.star;
    }

    public void setStar(int i) {
        this.star = i;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int i) {
        this.end = i;
    }

    public int getPartFileSize() {
        return this.partFileSize;
    }

    public void setPartFileSize(int i) {
        this.partFileSize = i;
    }
}
