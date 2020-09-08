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

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_SECONDS = 3;

    private static Executor sInstance;
    private final ThreadPoolExecutor mComputeExecutor;
    private final ExecutorService mAsyncExecutor;
    private final AtomicInteger mCount = new AtomicInteger(1);

    public static Executor getInstance() {
        if (sInstance == null) {
            sInstance = new Executor();
        }

        return sInstance;
    }

    public static void shutdown() {
        if (sInstance != null) {
            sInstance.doShutdown();
            sInstance = null;
        }
    }

    private Executor() {
        BlockingQueue<Runnable> queue = new SynchronousQueue<>();
        ThreadFactory factory = r -> new Thread(r, "Dag #" + mCount.getAndIncrement());
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        mComputeExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS, SECONDS, queue, factory, handler);
        mAsyncExecutor = Executors.newCachedThreadPool(factory);
    }

    public void doShutdown() {
        mComputeExecutor.shutdown();
        mAsyncExecutor.shutdown();
        sInstance = null;
    }

    public ExecutorService computeExecutor() {
        return mComputeExecutor;
    }

    public ExecutorService asyncExecutor() {
        return mAsyncExecutor;
    }
}
