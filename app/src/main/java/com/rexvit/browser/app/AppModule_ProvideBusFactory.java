package com.rexvit.browser.app;

import com.squareup.otto.Bus;
import dagger.internal.Factory;
import dagger.internal.Preconditions;


public final class AppModule_ProvideBusFactory implements Factory<Bus> {
    private final AppModule module;

    public AppModule_ProvideBusFactory(AppModule appModule) {
        this.module = appModule;
    }

    @Override 
    public Bus get() {
        return provideInstance(this.module);
    }

    public static Bus provideInstance(AppModule appModule) {
        return proxyProvideBus(appModule);
    }

    public static AppModule_ProvideBusFactory create(AppModule appModule) {
        return new AppModule_ProvideBusFactory(appModule);
    }

    public static Bus proxyProvideBus(AppModule appModule) {
        return (Bus) Preconditions.checkNotNull(appModule.provideBus(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
