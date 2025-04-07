package com.rexvit.browser.utils.schedulerUtils;

import androidx.annotation.NonNull;
import com.rexvit.browser.Interface.Scheduler;
import java.util.concurrent.Executor;


public class ExecutorScheduler implements Scheduler {
    @NonNull
    private final Executor mBackingExecutor;

    public ExecutorScheduler(@NonNull Executor executor) {
        this.mBackingExecutor = executor;
    }

    @Override 
    public void execute(@NonNull Runnable runnable) {
        this.mBackingExecutor.execute(runnable);
    }
}
