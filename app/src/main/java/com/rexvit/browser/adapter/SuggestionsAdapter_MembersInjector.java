package com.rexvit.browser.adapter;

import com.rexvit.browser.database.HistoryDatabase;

import dagger.MembersInjector;
import javax.inject.Provider;


public final class SuggestionsAdapter_MembersInjector implements MembersInjector<SuggestionsAdapter> {
    private final Provider<HistoryDatabase> mDatabaseHandlerProvider;

    public SuggestionsAdapter_MembersInjector(Provider<HistoryDatabase> provider) {
        this.mDatabaseHandlerProvider = provider;
    }

    public static MembersInjector<SuggestionsAdapter> create(Provider<HistoryDatabase> provider) {
        return new SuggestionsAdapter_MembersInjector(provider);
    }

    @Override 
    public void injectMembers(SuggestionsAdapter suggestionsAdapter) {
        injectMDatabaseHandler(suggestionsAdapter, this.mDatabaseHandlerProvider.get());
    }

    public static void injectMDatabaseHandler(SuggestionsAdapter suggestionsAdapter, HistoryDatabase historyDatabase) {
        suggestionsAdapter.mDatabaseHandler = historyDatabase;
    }
}
