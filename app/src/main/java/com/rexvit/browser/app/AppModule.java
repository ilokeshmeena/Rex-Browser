package com.rexvit.browser.app;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;

@Module

public class AppModule {
    private final BrowserApp mApp;
    @NonNull
    private final Bus mBus = new Bus();

    public AppModule(BrowserApp browserApp) {
        this.mApp = browserApp;
    }

    @Provides
    public Application provideApplication() {
        return this.mApp;
    }

    @Provides
    public Context provideContext() {
        return this.mApp.getApplicationContext();
    }

    @Provides
    @NonNull
    public Bus provideBus() {
        return this.mBus;
    }
}
