package com.phantomvk.dag.library.exector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Executor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT + 1;

    private static volatile Executor sInstance;

    private ExecutorService mComputeExecutor;
    private ExecutorService mAsyncExecutor;

    private final AtomicInteger mCount = new AtomicInteger(1);
    private final ThreadFactory mFactory = r -> new Thread(r, "Dag #" + mCount.getAndIncrement());

    private Executor() {
    }

    public static Executor getInstance() {
        return (sInstance == null) ? (sInstance = new Executor()) : sInstance;
    }

    public static void shutdown() {
        if (sInstance != null) {
            sInstance.doShutdown();
            sInstance = null;
        }
    }

    public void doShutdown() {
        if (mComputeExecutor != null && !mComputeExecutor.isShutdown()) {
            mComputeExecutor.shutdown();
        }

        if (mAsyncExecutor != null && !mAsyncExecutor.isShutdown()) {
            mAsyncExecutor.shutdown();
        }

        sInstance = null;
    }

    public ExecutorService computeExecutor() {
        if (mComputeExecutor == null) {
            mComputeExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());
        }

        return mComputeExecutor;
    }

    public ExecutorService asyncExecutor() {
        if (mAsyncExecutor == null) {
            mAsyncExecutor = Executors.newCachedThreadPool(mFactory);
        }

        return mAsyncExecutor;
    }
}
