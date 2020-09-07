package com.phantomvk.dag.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;

import androidx.annotation.MainThread;

import com.phantomvk.dag.library.exector.Executor;
import com.phantomvk.dag.library.meta.CommonTask;
import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.TaskWorker;
import com.phantomvk.dag.library.utility.ProcessUtility;
import com.phantomvk.dag.library.utility.SortUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Dag {

    private static final int TIMEOUT_MS = 60 * 1000;

    @SuppressLint("StaticFieldLeak")
    private static volatile Dag sDag;

    private final boolean isMainProcess;

    private List<CommonTask> mTaskList;
    private final Map<CommonTask, List<CommonTask>> mTaskChildren;
    private final List<CommonTask> mMainThreadList;
    private final List<CommonTask> mThreadPoolList;

    private int mWaitCount;
    private CountDownLatch mLatch;
    private int timeout;

    public final Dag getInstance(Context context) {
        if (context == null) throw new NullPointerException("Context should be null.");

        if (sDag == null) {
            synchronized (Dag.class) {
                if (sDag == null) {
                    sDag = new Dag(context);
                }
            }
        }

        return sDag;
    }

    private Dag(Context context) {
        if (context == null) {
            throw new NullPointerException("Context should not be null.");
        }

        isMainProcess = ProcessUtility.isMainThread(context);

        mTaskChildren = new HashMap<>();
        mMainThreadList = new ArrayList<>();
        mThreadPoolList = new ArrayList<>();
    }

    public Dag addTask(CommonTask task) {
        if (task == null) throw new NullPointerException("Task should not be null");

        mTaskList.add(task);

        if (needWaiting(task)) {
            mWaitCount++;
        }

        return this;
    }

    @MainThread
    public void start() {
        if (!isMainProcess) {
            throw new RuntimeException("Dag::start() must run on main process.");
        }

        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("Dag::start() must run on MainThread.");
        }

        List<CommonTask> sorted = SortUtility.sort(mTaskList, mTaskChildren);
        for (CommonTask task : sorted) {
            List<CommonTask> l = task.onMainThread() ? mMainThreadList : mThreadPoolList;
            l.add(task);
        }

        mLatch = new CountDownLatch(mWaitCount);
        timeout = (timeout == 0) ? TIMEOUT_MS : timeout;

        dispatch();
        await();
    }

    private void dispatch() {
        Executor executor = Executor.getInstance();
        for (CommonTask task : mThreadPoolList) {
            TaskWorker worker = new TaskWorker(task, this);

            if (task instanceof ComputeTask) {
                executor.getComputeExecutor().execute(worker);
            } else {
                executor.getIoExecutor().execute(worker);
            }
        }

        for (CommonTask task : mMainThreadList) {
            task.run();
        }
    }

    private void await() {
        try {
            mLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void notifyChildren(CommonTask task) {
        List<? extends CommonTask> list = mTaskChildren.get(task);
        if (list == null) return;

        for (CommonTask t : list) {
            t.doNotify();
        }
    }

    public void taskFinished(CommonTask task) {
        if (needWaiting(task)) {
            mLatch.countDown();
        }
    }

    private boolean needWaiting(CommonTask task) {
        return !task.onMainThread() && task.shouldWait();
    }
}
