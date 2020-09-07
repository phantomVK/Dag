package com.phantomvk.dag.library.exector;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executor {

    private static volatile Executor sExecutor;

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, PROCESSORS - 1);
    private static final int KEEP_ALIVE = 4;

    private final ThreadPoolExecutor mComputePoolExecutor;
    private final ExecutorService mIoPoolExecutor;

    public static Executor getInstance() {
        if (sExecutor == null) {
            synchronized (Executor.class) {
                if (sExecutor == null) {
                    sExecutor = new Executor();
                }
            }
        }

        return sExecutor;
    }

    private Executor() {
        mComputePoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        mComputePoolExecutor.allowCoreThreadTimeOut(true);
        mIoPoolExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    }

    public ExecutorService getComputeExecutor() {
        return mComputePoolExecutor;
    }

    public ExecutorService getIoExecutor() {
        return mIoPoolExecutor;
    }
}
