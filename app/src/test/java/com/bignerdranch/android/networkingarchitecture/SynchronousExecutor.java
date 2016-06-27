package com.bignerdranch.android.networkingarchitecture;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class SynchronousExecutor implements Executor {
    @Override
    public void execute(@NonNull Runnable runnable) {
        runnable.run();
    }
}
