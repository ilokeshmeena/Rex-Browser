package com.rexvit.browser.utils;

import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import com.rexvit.browser.Interface.Action;
import com.rexvit.browser.Interface.Scheduler;
import com.rexvit.browser.Interface.Subscriber;
import com.rexvit.browser.Interface.Subscription;
import com.rexvit.browser.utils.animUtils.Preconditions;
import com.rexvit.browser.utils.runnableUtils.OnCompleteRunnable;
import com.rexvit.browser.utils.runnableUtils.OnErrorRunnable;
import com.rexvit.browser.utils.runnableUtils.OnNextRunnable;
import com.rexvit.browser.utils.runnableUtils.OnStartRunnable;
import com.rexvit.browser.utils.schedulerUtils.ThreadScheduler;


public class Observable<T> {
    private static final String TAG = "Observable";
    @NonNull
    private final Action<T> mAction;
    @NonNull
    private final Scheduler mDefault;
    @Nullable
    private Scheduler mObserverThread;
    @Nullable
    private Scheduler mSubscriberThread;

    private Observable(@NonNull Action<T> action) {
        this.mAction = action;
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Looper myLooper = Looper.myLooper();
        Preconditions.checkNonNull(myLooper);
        this.mDefault = new ThreadScheduler(myLooper);
    }

    @NonNull
    public static <T> Observable<T> create(@NonNull Action<T> action) {
        Preconditions.checkNonNull(action);
        return new Observable<>(action);
    }

    @NonNull
    public static <T> Observable<T> empty() {
        return new Observable<>(new Action<T>() { 
            @Override 
            public void onSubscribe(@NonNull Subscriber<T> subscriber) {
                subscriber.onComplete();
            }
        });
    }

    public Observable<T> subscribeOn(@NonNull Scheduler scheduler) {
        this.mSubscriberThread = scheduler;
        return this;
    }

    public Observable<T> observeOn(@NonNull Scheduler scheduler) {
        this.mObserverThread = scheduler;
        return this;
    }

    public void subscribe() {
        executeOnSubscriberThread(new Runnable() { 
            @Override 
            public void run() {
                Observable.this.mAction.onSubscribe(new SubscriberImpl(null, Observable.this));
            }
        });
    }

    public Subscription subscribe(@NonNull OnSubscribe<T> onSubscribe) {
        Preconditions.checkNonNull(onSubscribe);
        final SubscriberImpl subscriberImpl = new SubscriberImpl(onSubscribe, this);
        subscriberImpl.onStart();
        executeOnSubscriberThread(new Runnable() { 
            @Override 
            public void run() {
                try {
                    Observable.this.mAction.onSubscribe(subscriberImpl);
                } catch (Exception e) {
                    subscriberImpl.onError(e);
                }
            }
        });
        return subscriberImpl;
    }

    
    public void executeOnObserverThread(@NonNull Runnable runnable) {
        Scheduler scheduler = this.mObserverThread;
        if (scheduler != null) {
            scheduler.execute(runnable);
        } else {
            this.mDefault.execute(runnable);
        }
    }

    private void executeOnSubscriberThread(@NonNull Runnable runnable) {
        Scheduler scheduler = this.mSubscriberThread;
        if (scheduler != null) {
            scheduler.execute(runnable);
        } else {
            this.mDefault.execute(runnable);
        }
    }

    
    
    public static class SubscriberImpl<T> implements Subscriber<T> {
        @NonNull
        private final Observable<T> mObservable;
        private boolean mOnCompleteExecuted = false;
        private boolean mOnError = false;
        @Nullable
        private volatile OnSubscribe<T> mOnSubscribe;

        SubscriberImpl(@Nullable OnSubscribe<T> onSubscribe, @NonNull Observable<T> observable) {
            this.mOnSubscribe = onSubscribe;
            this.mObservable = observable;
        }

        @Override 
        public void unsubscribe() {
            this.mOnSubscribe = null;
        }

        @Override 
        public void onComplete() {
            OnSubscribe<T> onSubscribe = this.mOnSubscribe;
            if (!this.mOnCompleteExecuted && onSubscribe != null && !this.mOnError) {
                this.mOnCompleteExecuted = true;
                this.mObservable.executeOnObserverThread(new OnCompleteRunnable(onSubscribe));
            } else if (!this.mOnError && this.mOnCompleteExecuted) {
                Log.e(Observable.TAG, "onComplete called more than once");
                throw new RuntimeException("onComplete called more than once");
            }
            unsubscribe();
        }

        @Override 
        public void onStart() {
            OnSubscribe<T> onSubscribe = this.mOnSubscribe;
            if (onSubscribe != null) {
                this.mObservable.executeOnObserverThread(new OnStartRunnable(onSubscribe));
            }
        }

        @Override 
        public void onError(@NonNull Throwable th) {
            OnSubscribe<T> onSubscribe = this.mOnSubscribe;
            if (onSubscribe != null) {
                this.mOnError = true;
                this.mObservable.executeOnObserverThread(new OnErrorRunnable(onSubscribe, th));
            }
            unsubscribe();
        }

        @Override 
        public void onNext(T t) {
            OnSubscribe<T> onSubscribe = this.mOnSubscribe;
            if (!this.mOnCompleteExecuted && onSubscribe != null && !this.mOnError) {
                this.mObservable.executeOnObserverThread(new OnNextRunnable(onSubscribe, t));
            } else if (this.mOnCompleteExecuted) {
                Log.e(Observable.TAG, "onComplete has been already called, onNext should not be called");
                throw new RuntimeException("onNext should not be called after onComplete has been called");
            }
        }

        @Override 
        public boolean isUnsubscribed() {
            return this.mOnSubscribe == null;
        }
    }
}
