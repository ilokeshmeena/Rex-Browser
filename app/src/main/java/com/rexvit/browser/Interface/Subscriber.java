package com.rexvit.browser.Interface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface Subscriber<T> extends Subscription {
    boolean isUnsubscribed();

    void onComplete();

    void onError(@NonNull Throwable th);

    void onNext(@Nullable T t);

    void onStart();
}
