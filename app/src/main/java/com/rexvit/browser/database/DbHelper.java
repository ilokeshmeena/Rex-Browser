package com.rexvit.browser.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rexvit.browser.models.DownloadData;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "idm_turbo.db";
    private static final int DATABASE_VERSION = 1;
    private static DbHelper sInstance;
    private static final String TABLE_NAME = "downloads";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_FILE_NAME = "file_name";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_FILE_EXTENSION = "file_extension";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_PARTS = "parts";
    private static final String COLUMN_CREATED_AT = "created_at";

    public static synchronized DbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_URL + " TEXT UNIQUE, "
                + COLUMN_FILE_NAME + " TEXT, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_FILE_EXTENSION + " TEXT, "
                + COLUMN_FILE_SIZE + " INTEGER, "
                + COLUMN_STATUS + " TEXT, "
                + COLUMN_PARTS + " INTEGER, "
                + COLUMN_CREATED_AT + " DATETIME DEFAULT (DATETIME('now','localtime')))";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Perform database upgrade operations if needed
    }

    public void insertDownload(DownloadData downloadData) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_URL, downloadData.getUrl());
        contentValues.put(COLUMN_FILE_NAME, downloadData.getFileName());
        contentValues.put(COLUMN_FILE_PATH, downloadData.getFilePath());
        contentValues.put(COLUMN_FILE_EXTENSION, downloadData.getFileExtension());
        contentValues.put(COLUMN_FILE_SIZE, downloadData.getFileSize());
        contentValues.put(COLUMN_PARTS, downloadData.getParts());
        contentValues.put(COLUMN_STATUS, downloadData.getStatus());
        db.replace(TABLE_NAME, null, contentValues);
        db.close();
    }

    @SuppressLint("Range")
    public List<DownloadData> getDownloadsByStatus(String status) {
        List<DownloadData> downloads = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_STATUS + " = ?";
        String[] selectionArgs = {status};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToFirst()) {
            do {
                DownloadData downloadData = new DownloadData();
                downloadData.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                downloadData.setFileName(cursor.getString(cursor.getColumnIndex(COLUMN_FILE_NAME)));
                downloadData.setFilePath(cursor.getString(cursor.getColumnIndex(COLUMN_FILE_PATH)));
                downloadData.setFileSize(cursor.getInt(cursor.getColumnIndex(COLUMN_FILE_SIZE)));
                downloadData.setParts(cursor.getInt(cursor.getColumnIndex(COLUMN_PARTS)));
                downloadData.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
                downloadData.setCreatedAt(cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_AT)));
                downloads.add(downloadData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return downloads;
    }

    public void deleteDownloads(List<DownloadData> downloads) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (DownloadData downloadData : downloads) {
                String whereClause = COLUMN_URL + " = ?";
                String[] whereArgs = {downloadData.getUrl()};
                db.delete(TABLE_NAME, whereClause, whereArgs);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}