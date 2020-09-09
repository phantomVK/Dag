package com.phantomvk.dag.library.exector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Executor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 2 * CPU_COUNT + 1;
    private static final int KEEP_ALIVE_SECONDS = 3;

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
            BlockingQueue<Runnable> queue = new SynchronousQueue<>();
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            mComputeExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_SECONDS, SECONDS, queue, mFactory, handler);
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
