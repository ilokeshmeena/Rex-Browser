package com.rexvit.browser.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rexvit.browser.R;
import com.rexvit.browser.app.BrowserApp;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HistoryDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "historyManager";
    private static final int DATABASE_VERSION = 2;
    private static final String KEY_ID = "id";
    private static final String KEY_TIME_VISITED = "time";
    private static final String KEY_TITLE = "title";
    private static final String KEY_URL = "url";
    private static final String TABLE_HISTORY = "history";
    @Nullable
    private SQLiteDatabase mDatabase;

    @Inject
    public HistoryDatabase(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        initialize();
    }

    private void initialize() {
        BrowserApp.getTaskThread().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (HistoryDatabase.this) {
                    try {
                        HistoryDatabase.this.mDatabase = HistoryDatabase.this.getWritableDatabase();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_URL + " TEXT," +
                KEY_TITLE + " TEXT," +
                KEY_TIME_VISITED + " INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public synchronized void deleteHistory() {
        this.mDatabase = openIfNecessary();
        this.mDatabase.delete(TABLE_HISTORY, null, null);
        this.mDatabase.close();
        this.mDatabase = getWritableDatabase();
    }

    @Override
    public synchronized void close() {
        if (this.mDatabase != null) {
            this.mDatabase.close();
            this.mDatabase = null;
        }
        super.close();
    }

    @NonNull
    private SQLiteDatabase openIfNecessary() {
        SQLiteDatabase sqLiteDatabase = this.mDatabase;
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            this.mDatabase = getWritableDatabase();
        }
        return this.mDatabase;
    }

    public synchronized void visitHistoryItem(@NonNull String url, @Nullable String title) {
        this.mDatabase = openIfNecessary();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TITLE, title == null ? "" : title);
        contentValues.put(KEY_TIME_VISITED, System.currentTimeMillis());
        Cursor query = this.mDatabase.query(false, TABLE_HISTORY, new String[]{KEY_URL}, KEY_URL + " = ?", new String[]{url}, null, null, null, "1");
        if (query.getCount() > 0) {
            this.mDatabase.update(TABLE_HISTORY, contentValues, KEY_URL + " = ?", new String[]{url});
        } else {
            addHistoryItem(new HistoryItem(url, title == null ? "" : title));
        }
        query.close();
    }

    private synchronized void addHistoryItem(@NonNull HistoryItem historyItem) {
        this.mDatabase = openIfNecessary();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_URL, historyItem.getUrl());
        contentValues.put(KEY_TITLE, historyItem.getTitle());
        contentValues.put(KEY_TIME_VISITED, System.currentTimeMillis());
        this.mDatabase.insert(TABLE_HISTORY, null, contentValues);
    }

    @SuppressLint("Range")
    @NonNull
    public synchronized List<HistoryItem> findItemsContaining(@Nullable String query) {
        this.mDatabase = openIfNecessary();
        ArrayList<HistoryItem> arrayList = new ArrayList<>(5);
        if (query == null) {
            return arrayList;
        }
        String sqlEscapeString = DatabaseUtils.sqlEscapeString('%' + query + '%');
        Cursor cursor = this.mDatabase.rawQuery("SELECT * FROM " + TABLE_HISTORY +
                " WHERE " + KEY_TITLE + " LIKE " + sqlEscapeString +
                " OR " + KEY_URL + " LIKE " + sqlEscapeString +
                " ORDER BY " + KEY_TIME_VISITED + " DESC LIMIT 5", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                HistoryItem historyItem = new HistoryItem();
                historyItem.setUrl(cursor.getString(cursor.getColumnIndex(KEY_URL)));
                historyItem.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                historyItem.setImageId(R.drawable.ic_add_black);
                arrayList.add(historyItem);
                count++;
                if (count >= 5) {
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }
}