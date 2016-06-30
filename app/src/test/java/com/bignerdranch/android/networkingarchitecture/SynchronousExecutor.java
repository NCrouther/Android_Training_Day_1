package com.bignerdranch.android.networkingarchitecture;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class SynchronousExecutor extends AbstractExecutorService {
    @Override
    public void execute(@NonNull Runnable runnable) {
        runnable.run();
    }

    @Override
    public void shutdown() {

    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return false;
    }
}
