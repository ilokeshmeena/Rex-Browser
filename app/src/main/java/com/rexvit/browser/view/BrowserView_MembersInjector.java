package com.rexvit.browser.view;

import com.squareup.otto.Bus;
import dagger.MembersInjector;
import javax.inject.Provider;


public final class BrowserView_MembersInjector implements MembersInjector<BrowserView> {
    private final Provider<Bus> mEventBusProvider;

    public BrowserView_MembersInjector(Provider<Bus> provider) {
        this.mEventBusProvider = provider;
    }

    public static MembersInjector<BrowserView> create(Provider<Bus> provider) {
        return new BrowserView_MembersInjector(provider);
    }

    @Override 
    public void injectMembers(BrowserView browserView) {
        injectMEventBus(browserView, this.mEventBusProvider.get());
    }

    public static void injectMEventBus(BrowserView browserView, Bus bus) {
        browserView.mEventBus = bus;
    }
}
