package com.rexvit.browser.app;

import com.squareup.otto.Bus;
import dagger.MembersInjector;
import javax.inject.Provider;


public final class BrowserPresenter_MembersInjector implements MembersInjector<BrowserPresenter> {
    private final Provider<Bus> mEventBusProvider;

    public BrowserPresenter_MembersInjector(Provider<Bus> provider) {
        this.mEventBusProvider = provider;
    }

    public static MembersInjector<BrowserPresenter> create(Provider<Bus> provider) {
        return new BrowserPresenter_MembersInjector(provider);
    }

    @Override 
    public void injectMembers(BrowserPresenter browserPresenter) {
        injectMEventBus(browserPresenter, this.mEventBusProvider.get());
    }

    public static void injectMEventBus(BrowserPresenter browserPresenter, Bus bus) {
        browserPresenter.mEventBus = bus;
    }
}
