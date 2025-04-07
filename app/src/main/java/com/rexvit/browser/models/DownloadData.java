package com.rexvit.browser.models;


public class DownloadData {
    private String createdAt;
    private String fileExtension;
    private String fileName;
    private String filePath;
    private int fileSize;
    private int parts;
    private String status;
    private String url;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public int getParts() {
        return this.parts;
    }

    public void setParts(int i) {
        this.parts = i;
    }

    public String getStatus() {
        return this.status;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String str) {
        this.filePath = str;
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(int i) {
        this.fileSize = i;
    }

    public void setStatus(String str) {
        this.status = str;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getFileExtension() {
        return this.fileExtension;
    }

    public void setFileExtension(String str) {
        this.fileExtension = str;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String str) {
        this.fileName = str;
    }

    public void setCreatedAt(String str) {
        this.createdAt = str;
    }
}
