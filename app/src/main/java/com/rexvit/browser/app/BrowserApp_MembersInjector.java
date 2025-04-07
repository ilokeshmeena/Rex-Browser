package com.rexvit.browser.app;

import com.squareup.otto.Bus;
import dagger.MembersInjector;
import javax.inject.Provider;


public final class BrowserApp_MembersInjector implements MembersInjector<BrowserApp> {
    private final Provider<Bus> mBusProvider;

    public BrowserApp_MembersInjector(Provider<Bus> provider) {
        this.mBusProvider = provider;
    }

    public static MembersInjector<BrowserApp> create(Provider<Bus> provider) {
        return new BrowserApp_MembersInjector(provider);
    }

    @Override 
    public void injectMembers(BrowserApp browserApp) {
        injectMBus(browserApp, this.mBusProvider.get());
    }

    public static void injectMBus(BrowserApp browserApp, Bus bus) {
        browserApp.mBus = bus;
    }
}
