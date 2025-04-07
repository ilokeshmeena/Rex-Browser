package com.rexvit.browser.activities;

import com.rexvit.browser.database.HistoryDatabase;
import com.squareup.otto.Bus;

import dagger.MembersInjector;
import javax.inject.Provider;


public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
    private final Provider<Bus> mEventBusProvider;
    private final Provider<HistoryDatabase> mHistoryDatabaseProvider;

    public MainActivity_MembersInjector(Provider<Bus> provider, Provider<HistoryDatabase> provider2) {
        this.mEventBusProvider = provider;
        this.mHistoryDatabaseProvider = provider2;
    }

    public static MembersInjector<MainActivity> create(Provider<Bus> provider, Provider<HistoryDatabase> provider2) {
        return new MainActivity_MembersInjector(provider, provider2);
    }

    @Override 
    public void injectMembers(MainActivity mainActivity) {
        injectMEventBus(mainActivity, this.mEventBusProvider.get());
        injectMHistoryDatabase(mainActivity, this.mHistoryDatabaseProvider.get());
    }

    public static void injectMEventBus(MainActivity mainActivity, Bus bus) {
        mainActivity.mEventBus = bus;
    }

    public static void injectMHistoryDatabase(MainActivity mainActivity, HistoryDatabase historyDatabase) {
        mainActivity.mHistoryDatabase = historyDatabase;
    }
}
