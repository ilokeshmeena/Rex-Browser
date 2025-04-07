package com.rexvit.browser.fragment;

import com.squareup.otto.Bus;
import dagger.MembersInjector;
import javax.inject.Provider;


public final class TabsFragment_MembersInjector implements MembersInjector<TabsFragment> {
    private final Provider<Bus> mBusProvider;

    public TabsFragment_MembersInjector(Provider<Bus> provider) {
        this.mBusProvider = provider;
    }

    public static MembersInjector<TabsFragment> create(Provider<Bus> provider) {
        return new TabsFragment_MembersInjector(provider);
    }

    @Override 
    public void injectMembers(TabsFragment tabsFragment) {
        injectMBus(tabsFragment, this.mBusProvider.get());
    }

    public static void injectMBus(TabsFragment tabsFragment, Bus bus) {
        tabsFragment.mBus = bus;
    }
}
