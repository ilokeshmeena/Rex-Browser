package com.rexvit.browser.utils.schedulerUtils;

import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.rexvit.browser.Interface.Scheduler;
import java.util.concurrent.Executor;


public final class Schedulers {
    @Nullable
    private static Scheduler sIoScheduler;
    @Nullable
    private static Scheduler sMainScheduler;
    @Nullable
    private static Scheduler sWorkerScheduler;

    private Schedulers() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    @NonNull
    public static Scheduler from(@NonNull Executor executor) {
        return new ExecutorScheduler(executor);
    }

    @NonNull
    public static Scheduler current() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        return new ThreadScheduler(Looper.myLooper());
    }

    @NonNull
    public static Scheduler newSingleThreadedScheduler() {
        return new SingleThreadedScheduler();
    }

    @NonNull
    public static Scheduler worker() {
        if (sWorkerScheduler == null) {
            sWorkerScheduler = new WorkerScheduler();
        }
        return sWorkerScheduler;
    }

    @NonNull
    public static Scheduler main() {
        if (sMainScheduler == null) {
            sMainScheduler = new ThreadScheduler(Looper.getMainLooper());
        }
        return sMainScheduler;
    }

    @NonNull
    public static Scheduler io() {
        if (sIoScheduler == null) {
            sIoScheduler = new SingleThreadedScheduler();
        }
        return sIoScheduler;
    }
}
