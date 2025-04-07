package com.rexvit.browser.database;

import com.tonyodev.fetch2.Download;


public class DataUpdatedEvent {
    public static class NewDownloadAdded {
    }
    public static class DownloadCompleted {
        public Download download;

        public DownloadCompleted(Download download) {
            this.download = download;
        }
    }

    
    public static class DirectorySelected {
        public String selectedDirectory;

        public DirectorySelected(String str) {
            this.selectedDirectory = str;
        }
    }
}
