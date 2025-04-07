package com.rexvit.browser.services;

import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import java.util.Set;

import okhttp3.OkHttpClient;


public class CustomDownloader extends OkHttpDownloader {
    public CustomDownloader() {
        this(null);
    }

    public CustomDownloader(OkHttpClient okHttpClient) {
        super(okHttpClient);
    }

    @Override
    public Downloader.FileDownloaderType getRequestFileDownloaderType(Downloader.ServerRequest serverRequest, Set<? extends Downloader.FileDownloaderType> set) {
        return Downloader.FileDownloaderType.PARALLEL;
    }

    @Override
    public Integer getFileSlicingCount(Downloader.ServerRequest serverRequest, long j) {
        return 20;
    }
}
