package com.rexvit.browser.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public abstract class OnSubscribe<T> {
    public void onComplete() {
    }

    public void onNext(@Nullable T t) {
    }

    public void onStart() {
    }

    public void onError(@NonNull Throwable th) {
        throw new RuntimeException("Exception thrown: override onError to handle it", th);
    }
}
