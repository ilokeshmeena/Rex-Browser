package com.rexvit.browser.database;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;


public final class HistoryDatabase_Factory implements Factory<HistoryDatabase> {
    private final Provider<Context> contextProvider;

    public HistoryDatabase_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override 
    public HistoryDatabase get() {
        return provideInstance(this.contextProvider);
    }

    public static HistoryDatabase provideInstance(Provider<Context> provider) {
        return new HistoryDatabase(provider.get());
    }

    public static HistoryDatabase_Factory create(Provider<Context> provider) {
        return new HistoryDatabase_Factory(provider);
    }

    public static HistoryDatabase newHistoryDatabase(Context context) {
        return new HistoryDatabase(context);
    }
}
