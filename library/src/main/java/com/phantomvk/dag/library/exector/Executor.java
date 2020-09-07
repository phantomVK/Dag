package com.phantomvk.dag.library.exector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executor {

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, PROCESSORS + 1);
    private static final int KEEP_ALIVE = 4;

    private final ThreadPoolExecutor computeExecutor;
    private final ExecutorService asyncExecutor;

    public static Executor getInstance() {
        return new Executor();
    }

    private Executor() {
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        computeExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE,
                KEEP_ALIVE, TimeUnit.SECONDS, queue);
        computeExecutor.allowCoreThreadTimeOut(true);
        asyncExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    }

    public void shutdown() {
        computeExecutor.shutdown();
        asyncExecutor.shutdown();
    }

    public ExecutorService computeExecutor() {
        return computeExecutor;
    }

    public ExecutorService asyncExecutor() {
        return asyncExecutor;
    }
}
