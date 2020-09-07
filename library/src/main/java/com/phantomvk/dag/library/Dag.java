package com.phantomvk.dag.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.phantomvk.dag.library.exector.Executor;
import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.utility.ProcessUtility;
import com.phantomvk.dag.library.utility.DagSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Dag {

    private static final int TIMEOUT_MS = 60 * 1000;

    @SuppressLint("StaticFieldLeak")
    private static volatile Dag sDag;

    private final boolean isMainProcess;
    private boolean isStarted;

    private final List<Task> mTaskList;
    private final Map<Class<? extends Task>, Task> mTaskMap;
    private final Map<Class<? extends Task>, List<Class<? extends Task>>> mTaskChildren;
    private final List<Task> mMainThreadList;
    private final List<Task> mThreadPoolList;

    private int mWaitCount;
    private CountDownLatch mLatch;

    private long timeout = TIMEOUT_MS;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public static Dag getInstance(Context context) {
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

        mTaskList = new ArrayList<>();
        mTaskMap = new HashMap<>();
        mTaskChildren = new HashMap<>();
        mMainThreadList = new ArrayList<>();
        mThreadPoolList = new ArrayList<>();
    }

    public Dag addTask(Task task) {
        if (task == null) throw new NullPointerException("Task should not be null");

        mTaskList.add(task);

        if (needWaiting(task)) {
            mWaitCount++;
        }

        return this;
    }

    public void start() {
        if (!isMainProcess) {
            throw new RuntimeException("Dag::start() must run on main process.");
        }

        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("Dag::start() must run on MainThread.");
        }

        if (isStarted) {
            throw new RuntimeException("Dag::start() should not be called more than once.");
        } else {
            isStarted = true;
        }

        List<Task> sorted = DagSolver.solve(mTaskList, mTaskMap, mTaskChildren);
        for (Task task : sorted) {
            List<Task> l = (task.onMainThread() ? mMainThreadList : mThreadPoolList);
            l.add(task);
        }

        mLatch = new CountDownLatch(mWaitCount);

        dispatch();
        await();
    }

    private void dispatch() {

        Log.e(Dag.class.getName(), "start.");

        Executor executor = Executor.getInstance();
        ExecutorService compute = executor.getComputeExecutor();
        ExecutorService async = executor.getAsyncExecutor();

        for (Task task : mThreadPoolList) {
            ExecutorService service = (task instanceof ComputeTask) ? compute : async;
            service.execute(new TaskWorker(task));
        }

        for (Task task : mMainThreadList) {
            task.run();
        }
    }

    private void await() {
        try {
            mLatch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void notifyChildren(Task task) {
        List<Class<? extends Task>> list = mTaskChildren.get(task.getClass());
        if (list == null) return;

        for (Class<? extends Task> t : list) {
            Task currTask = mTaskMap.get(t);
            if (currTask != null) {
                currTask.doNotify();
            }
        }
    }

    private void taskFinished(Task task) {
        if (needWaiting(task)) {
            mLatch.countDown();
        }
    }

    public Dag setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    private boolean needWaiting(Task task) {
        return !task.onMainThread() && task.shouldWait();
    }

    public class TaskWorker implements Runnable {

        private final Task mTask;

        public TaskWorker(@NonNull Task task) {
            mTask = task;
        }

        @Override
        public void run() {
            Process.setThreadPriority(mTask.priority());
            mTask.doAwait();
            mTask.run();
            notifyChildren(mTask);
            taskFinished(mTask);
        }
    }
}
