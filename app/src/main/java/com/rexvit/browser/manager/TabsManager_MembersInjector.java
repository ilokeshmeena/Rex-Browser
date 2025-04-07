package com.rexvit.browser.manager;

import android.app.Application;
import com.rexvit.browser.database.HistoryDatabase;
import com.squareup.otto.Bus;

import dagger.MembersInjector;
import javax.inject.Provider;


public final class TabsManager_MembersInjector implements MembersInjector<TabsManager> {
    private final Provider<Application> mAppProvider;
    private final Provider<Bus> mEventBusProvider;
    private final Provider<HistoryDatabase> mHistoryManagerProvider;

    public TabsManager_MembersInjector(Provider<HistoryDatabase> provider, Provider<Bus> provider2, Provider<Application> provider3) {
        this.mHistoryManagerProvider = provider;
        this.mEventBusProvider = provider2;
        this.mAppProvider = provider3;
    }

    public static MembersInjector<TabsManager> create(Provider<HistoryDatabase> provider, Provider<Bus> provider2, Provider<Application> provider3) {
        return new TabsManager_MembersInjector(provider, provider2, provider3);
    }

    @Override 
    public void injectMembers(TabsManager tabsManager) {
        injectMHistoryManager(tabsManager, this.mHistoryManagerProvider.get());
        injectMEventBus(tabsManager, this.mEventBusProvider.get());
        injectMApp(tabsManager, this.mAppProvider.get());
    }

    public static void injectMHistoryManager(TabsManager tabsManager, HistoryDatabase historyDatabase) {
        tabsManager.mHistoryManager = historyDatabase;
    }

    public static void injectMEventBus(TabsManager tabsManager, Bus bus) {
        tabsManager.mEventBus = bus;
    }

    public static void injectMApp(TabsManager tabsManager, Application application) {
        tabsManager.mApp = application;
    }
}
