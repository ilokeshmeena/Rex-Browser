package com.rexvit.browser.adapter;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.rexvit.browser.Interface.Action;
import com.rexvit.browser.Interface.Scheduler;
import com.rexvit.browser.Interface.Subscriber;
import com.rexvit.browser.R;
import com.rexvit.browser.app.BrowserApp;
import com.rexvit.browser.database.HistoryDatabase;
import com.rexvit.browser.database.HistoryItem;
import com.rexvit.browser.search.SuggestionsManager;
import com.rexvit.browser.utils.Observable;
import com.rexvit.browser.utils.OnSubscribe;
import com.rexvit.browser.utils.ThemeUtils;
import com.rexvit.browser.utils.schedulerUtils.Schedulers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


public class SuggestionsAdapter extends BaseAdapter implements Filterable {
    public static final String CACHE_FILE_TYPE = ".sgg";
    private static final Scheduler FILTER_SCHEDULER = Schedulers.newSingleThreadedScheduler();
    private static final int MAX_SUGGESTIONS = 10;
    @NonNull
    private final Context mContext;
    @Inject
    HistoryDatabase mDatabaseHandler;
    @NonNull
    private final Drawable mHistoryDrawable;
    @NonNull
    private final Drawable mSearchDrawable;
    private final List<HistoryItem> mFilteredList = new ArrayList(10);
    private final List<HistoryItem> mHistory = new ArrayList(10);
    private final List<HistoryItem> mSuggestions = new ArrayList(10);
    private final Comparator<HistoryItem> mFilterComparator = new SuggestionsComparator();
    private final List<HistoryItem> mAllBookmarks = new ArrayList(10);
    private boolean mIsIncognito = true;

    
    public boolean shouldRequestNetwork() {
        return true;
    }

    @Override 
    public long getItemId(int i) {
        return 0L;
    }

    public SuggestionsAdapter(@NonNull Context context) {
        BrowserApp.getAppComponent().inject(this);
        this.mContext = context;
        this.mSearchDrawable = ThemeUtils.getThemedDrawable(context, R.drawable.ic_search_home);
        this.mHistoryDrawable = ThemeUtils.getThemedDrawable(context, R.drawable.ic_add_black);
    }

    public void clearCache() {
        Schedulers.io().execute(new ClearCacheRunnable(BrowserApp.get(this.mContext)));
    }

    @Override 
    public int getCount() {
        return this.mFilteredList.size();
    }

    @Override 
    public Object getItem(int i) {
        if (i > this.mFilteredList.size() || i < 0) {
            return null;
        }
        return this.mFilteredList.get(i);
    }

    
    private static class SuggestionHolder {
        final ImageView mImage;
        final TextView mTitle;
        final TextView mUrl;

        SuggestionHolder(@NonNull View view) {
            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mUrl = (TextView) view.findViewById(R.id.url);
            this.mImage = (ImageView) view.findViewById(R.id.suggestionIcon);
        }
    }

    @Override 
    public View getView(int i, View view, ViewGroup viewGroup) {
        SuggestionHolder suggestionHolder;
        Drawable drawable;
        if (view == null) {
            view = LayoutInflater.from(this.mContext).inflate(R.layout.two_line_autocomplete, viewGroup, false);
            suggestionHolder = new SuggestionHolder(view);
            view.setTag(suggestionHolder);
        } else {
            suggestionHolder = (SuggestionHolder) view.getTag();
        }
        HistoryItem historyItem = this.mFilteredList.get(i);
        suggestionHolder.mTitle.setText(historyItem.getTitle());
        suggestionHolder.mUrl.setText(historyItem.getUrl());
        int imageId = historyItem.getImageId();
        if (imageId == R.drawable.ic_add_black) {
            drawable = this.mHistoryDrawable;
            suggestionHolder.mUrl.setVisibility(View.VISIBLE);
        } else if (imageId == R.drawable.ic_search_home) {
            drawable = this.mSearchDrawable;
            suggestionHolder.mUrl.setVisibility(View.GONE);
        } else {
            drawable = this.mSearchDrawable;
        }
        suggestionHolder.mImage.setImageDrawable(drawable);
        return view;
    }

    @Override 
    public Filter getFilter() {
        return new SearchFilter(this);
    }

    
    public synchronized void publishResults(List<HistoryItem> list) {
        this.mFilteredList.clear();
        this.mFilteredList.addAll(list);
        notifyDataSetChanged();
    }

    
    public void clearSuggestions() {
        Observable.create(new Action<Void>() {
            @Override 
            public void onSubscribe(@NonNull Subscriber<Void> subscriber) {
                SuggestionsAdapter.this.mHistory.clear();
                SuggestionsAdapter.this.mSuggestions.clear();
                subscriber.onComplete();
            }
        }).subscribeOn(FILTER_SCHEDULER).observeOn(Schedulers.main()).subscribe();
    }

    
    public void combineResults(@Nullable List<HistoryItem> list, @Nullable final List<HistoryItem> list2, @Nullable final List<HistoryItem> list3) {
        Observable.create(new Action<List<HistoryItem>>() { 
            @Override 
            public void onSubscribe(@NonNull Subscriber<List<HistoryItem>> subscriber) {
                List<HistoryItem> arrayList = new ArrayList<>(5);
                if (list2 != null) {
                    SuggestionsAdapter.this.mHistory.clear();
                    SuggestionsAdapter.this.mHistory.addAll(list2);
                }
                if (list3 != null) {
                    SuggestionsAdapter.this.mSuggestions.clear();
                    SuggestionsAdapter.this.mSuggestions.addAll(list3);
                }
                Iterator it = SuggestionsAdapter.this.mHistory.iterator();
                ListIterator listIterator = SuggestionsAdapter.this.mSuggestions.listIterator();
                while (arrayList.size() < 10 && (listIterator.hasNext() || it.hasNext())) {
                    if (listIterator.hasNext() && arrayList.size() < 10) {
                        arrayList.add((HistoryItem) listIterator.next());
                    }
                    if (it.hasNext() && arrayList.size() < 10) {
                        arrayList.add((HistoryItem) it.next());
                    }
                }
                Collections.sort(arrayList, SuggestionsAdapter.this.mFilterComparator);
                subscriber.onNext(arrayList);
                subscriber.onComplete();
            }
        }).subscribeOn(FILTER_SCHEDULER).observeOn(Schedulers.main()).subscribe(new OnSubscribe<List<HistoryItem>>() {
            @Override 
            public void onNext(@Nullable List<HistoryItem> list4) {
                SuggestionsAdapter.this.publishResults(list4);
            }
        });
    }

    @NonNull
    private Observable<List<HistoryItem>> getBookmarksForQuery(@NonNull final String str) {
        return Observable.create(new Action<List<HistoryItem>>() { 
            @Override 
            public void onSubscribe(@NonNull Subscriber<List<HistoryItem>> subscriber) {
                List<HistoryItem> arrayList = new ArrayList<>(5);
                int i = 0;
                for (int i2 = 0; i2 < SuggestionsAdapter.this.mAllBookmarks.size() && i < 5; i2++) {
                    if (((HistoryItem) SuggestionsAdapter.this.mAllBookmarks.get(i2)).getTitle().toLowerCase(Locale.getDefault()).startsWith(str)) {
                        arrayList.add(SuggestionsAdapter.this.mAllBookmarks.get(i2));
                    } else if (((HistoryItem) SuggestionsAdapter.this.mAllBookmarks.get(i2)).getUrl().contains(str)) {
                        arrayList.add(SuggestionsAdapter.this.mAllBookmarks.get(i2));
                    }
                    i++;
                }
                subscriber.onNext(arrayList);
                subscriber.onComplete();
            }
        });
    }

    
    @NonNull
    public Observable<List<HistoryItem>> getSuggestionsForQuery(@NonNull String str) {
        return SuggestionsManager.getObservable(str, this.mContext, SuggestionsManager.Source.GOOGLE);
    }

    
    @NonNull
    public Observable<List<HistoryItem>> getHistoryForQuery(@NonNull final String str) {
        return Observable.create(new Action<List<HistoryItem>>() { 
            @Override 
            public void onSubscribe(@NonNull Subscriber<List<HistoryItem>> subscriber) {
                subscriber.onNext(SuggestionsAdapter.this.mDatabaseHandler.findItemsContaining(str));
                subscriber.onComplete();
            }
        });
    }

    
    
    public static class SearchFilter extends Filter {
        @NonNull
        private final SuggestionsAdapter mSuggestionsAdapter;

        SearchFilter(@NonNull SuggestionsAdapter suggestionsAdapter) {
            this.mSuggestionsAdapter = suggestionsAdapter;
        }

        @Override 
        protected Filter.FilterResults performFiltering(CharSequence charSequence) {
            Filter.FilterResults filterResults = new Filter.FilterResults();
            if (charSequence == null || charSequence.length() == 0) {
                this.mSuggestionsAdapter.clearSuggestions();
                return filterResults;
            }
            String trim = charSequence.toString().toLowerCase(Locale.getDefault()).trim();
            if (this.mSuggestionsAdapter.shouldRequestNetwork() && !SuggestionsManager.isRequestInProgress()) {
                this.mSuggestionsAdapter.getSuggestionsForQuery(trim).subscribeOn(Schedulers.worker()).observeOn(Schedulers.main()).subscribe(new OnSubscribe<List<HistoryItem>>() { 
                    @Override 
                    public void onNext(@Nullable List<HistoryItem> list) {
                        SearchFilter.this.mSuggestionsAdapter.combineResults(null, null, list);
                    }
                });
            }
            this.mSuggestionsAdapter.getHistoryForQuery(trim).subscribeOn(Schedulers.io()).observeOn(Schedulers.main()).subscribe(new OnSubscribe<List<HistoryItem>>() { 
                @Override 
                public void onNext(@Nullable List<HistoryItem> list) {
                    SearchFilter.this.mSuggestionsAdapter.combineResults(null, list, null);
                }
            });
            filterResults.count = 1;
            return filterResults;
        }

        @Override 
        public CharSequence convertResultToString(Object obj) {
            return ((HistoryItem) obj).getUrl();
        }

        @Override 
        protected void publishResults(CharSequence charSequence, Filter.FilterResults filterResults) {
            this.mSuggestionsAdapter.combineResults(null, null, null);
        }
    }

    
    

    private static class ClearCacheRunnable implements Runnable {
        @NonNull
        private final Application app;

        private static class NameFilter implements FilenameFilter {
            private NameFilter() {
            }

            public boolean accept(File file, @NonNull String str) {
                return str.endsWith(CACHE_FILE_TYPE);
            }
        }

        ClearCacheRunnable(@NonNull Application application) {
            this.app = application;
        }

        public void run() {
            File file = new File(this.app.getCacheDir().toString());
            String[] list = file.list(new NameFilter());
            long currentTimeMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
            int length = list.length;
            int i = 0;
            while (i < length) {
                String str = list[i];
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(file.getPath());
                stringBuilder.append(str);
                File file2 = new File(stringBuilder.toString());
                if (currentTimeMillis > file2.lastModified()) {
                    file2.delete();
                }
                i++;
            }
        }
    }



    
    private static class SuggestionsComparator implements Comparator<HistoryItem> {
        private SuggestionsComparator() {
        }

        @Override 
        public int compare(@NonNull HistoryItem historyItem, @NonNull HistoryItem historyItem2) {
            if (historyItem.getImageId() == historyItem2.getImageId()) {
                return 0;
            }
            return historyItem.getImageId() == R.drawable.ic_add_black ? -1 : 1;
        }
    }
}
