package com.phantomvk.dag.library.exector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executor {

    private static Executor sInstance;

    private final ThreadPoolExecutor computeExecutor;
    private final ExecutorService asyncExecutor;

    public static Executor getInstance() {
        if (sInstance == null) {
            sInstance = new Executor();
        }

        return sInstance;
    }

    private Executor() {
        int size = Math.max(2, Runtime.getRuntime().availableProcessors() + 1);
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();

        computeExecutor = new ThreadPoolExecutor(size, size, 4, TimeUnit.SECONDS, queue);
        computeExecutor.allowCoreThreadTimeOut(true);
        asyncExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    }

    public void shutdown() {
        computeExecutor.shutdown();
        asyncExecutor.shutdown();
        sInstance = null;
    }

    public ExecutorService computeExecutor() {
        return computeExecutor;
    }

    public ExecutorService asyncExecutor() {
        return asyncExecutor;
    }
}
