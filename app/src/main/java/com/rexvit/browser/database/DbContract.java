package com.rexvit.browser.database;

import android.provider.BaseColumns;


public class DbContract {

    
    public static class DownloadsEntry implements BaseColumns {
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_FILE_EXTENSION = "file_extension";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String COLUMN_FILE_PATH = "file_path";
        public static final String COLUMN_FILE_SIZE = "file_size";
        public static final String COLUMN_PARTS = "parts";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_URL = "url";
        public static final String TABLE_NAME = "downloads";
    }
}
