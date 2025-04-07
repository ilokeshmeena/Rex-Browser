package com.rexvit.browser.activities;

import static com.rexvit.browser.activities.MainActivity.mActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.rexvit.browser.Interface.UIController;
import com.rexvit.browser.R;
import com.rexvit.browser.adapter.BookmarksAdapter;
import com.rexvit.browser.database.BookmarksDb;
import com.rexvit.browser.utils.Task;
import com.rexvit.browser.utils.Utils;

import java.util.List;

public class BookmarksActivity extends AppCompatActivity {
    List<BookmarksDb> mList;
    LinearLayout mEmptyView ;
    ListView mListView;
    private UIController mUiController;
    private ClipboardManager mClipboardManager;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        this.mUiController = (UIController) mActivity;
        this.toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setTitle(getString(R.string.bookmarks));
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);

        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEmptyView = findViewById(R.id.fragmentEmptyView);
        mListView = findViewById(R.id.listViewFragment);
        try {
            this.mList = BookmarksDb.listAll(BookmarksDb.class);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        final BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(this, this.mList);
        this.mListView.setAdapter((ListAdapter) bookmarksAdapter);
        this.mListView.setEmptyView(this.mEmptyView);
        bookmarksAdapter.notifyDataSetChanged();
        new Handler().post(new Runnable() {


            @Override
            public void run() {
                bookmarksAdapter.notifyDataSetChanged();
            }
        });
        onListClick();
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
               openLink(i);
            }
        });
    }

    private void onListClick() {
        this.mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long j) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BookmarksActivity.this);
                builder.setTitle(BookmarksActivity.this.mList.get(i).getTitle());
                builder.setItems(R.array.bookmarks_choice, new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialogInterface, int i2) {
                        if (i2 == 0) {
                            BookmarksActivity.this.openLink(i);
                        } else if (i2 == 1) {
                            String title = BookmarksActivity.this.mList.get(i).getTitle();
                            String bookmarks = BookmarksActivity.this.mList.get(i).getBookmarks();
                            Activity activity = BookmarksActivity.this;
                            Task.ShareUrl(activity, title + "\n" + bookmarks);
                        } else if (i2 == 2) {
                            Task.CopyText(BookmarksActivity.this, BookmarksActivity.this.mList.get(i).getBookmarks());
                        } else if (i2 == 3) {
                            new AlertDialog.Builder(BookmarksActivity.this).setTitle(BookmarksActivity.this.getString(R.string.delet_item)).setMessage("Are you sure you want to delete this item?").setPositiveButton(BookmarksActivity.this.getString(R.string.yes), new DialogInterface.OnClickListener() {


                                public void onClick(DialogInterface dialogInterface, int i3) {
                                    BookmarksActivity.this.deleteBookmark(i);
                                    Utils.msg(BookmarksActivity.this.getString(R.string.deleted), BookmarksActivity.this);
                                }
                            }).setNegativeButton(BookmarksActivity.this.getString(R.string.cancel), (DialogInterface.OnClickListener) null).show();
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    private void deleteBookmark(int i) {
        this.mList.get(i).delete();
        new Handler().postDelayed(new Runnable() {


            public void run() {
                try {
                    BookmarksActivity.this.mList = BookmarksDb.listAll(BookmarksDb.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BookmarksActivity.this.mListView.setAdapter((ListAdapter) new BookmarksAdapter(BookmarksActivity.this, BookmarksActivity.this.mList));
            }
        }, (long) 800);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);


    }

    private void openLink(int i) {
        String bookmarks = this.mList.get(i).getBookmarks();
        if (bookmarks != null) {
            this.mUiController.newtab(bookmarks, true);
            this.mUiController.mainuigone();
            finish();
        }
    }
}