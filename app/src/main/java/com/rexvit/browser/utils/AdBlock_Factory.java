package com.rexvit.browser.utils;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;


public final class AdBlock_Factory implements Factory<AdBlock> {
    private final Provider<Context> contextProvider;

    public AdBlock_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override 
    public AdBlock get() {
        return provideInstance(this.contextProvider);
    }

    public static AdBlock provideInstance(Provider<Context> provider) {
        return new AdBlock(provider.get());
    }

    public static AdBlock_Factory create(Provider<Context> provider) {
        return new AdBlock_Factory(provider);
    }

    public static AdBlock newAdBlock(Context context) {
        return new AdBlock(context);
    }
}
