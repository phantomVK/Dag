package com.phantomvk.dag.library;

import android.content.Context;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;

import com.phantomvk.dag.library.exector.Executor;
import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.utility.ProcessUtility;
import com.phantomvk.dag.library.utility.DagSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Dag {

    private static final int TIMEOUT_MS = 60 * 1000; // 60s

    private static volatile Dag sInstance;

    private final boolean isMainProcess;
    private boolean isStarted;

    private int waitCount;
    private CountDownLatch latch;
    private AtomicInteger taskCount;

    private final List<Task> taskList;
    private final Map<Class<? extends Task>, List<Task>> taskChildren;

    // Task list to execute.
    private final List<Task> mainThreadList;
    private final List<Task> threadPoolList;

    private long timeout = TIMEOUT_MS;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public static Dag getInstance(Context context) {
        if (context == null) throw new NullPointerException("Context should be null.");

        if (sInstance == null) {
            synchronized (Dag.class) {
                if (sInstance == null) {
                    sInstance = new Dag(context);
                }
            }
        }

        return sInstance;
    }

    private Dag(Context context) {
        isMainProcess = ProcessUtility.isMainProcess(context);

        taskList = new ArrayList<>();
        taskChildren = new HashMap<>();
        mainThreadList = new ArrayList<>();
        threadPoolList = new ArrayList<>();
    }

    public Dag addTask(Task task) {
        if (task == null) {
            throw new NullPointerException("Task should not be null");
        }

        taskList.add(task);

        if (isBlockMainThread(task)) {
            waitCount++;
        }

        return this;
    }

    public void start() {
        // Process scope.
        if (!isMainProcess) {
            throw new RuntimeException("Dag::start() must run on main process.");
        }

        // Thread scope.
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("Dag::start() must run on MainThread.");
        }

        // Method scope.
        if (isStarted) {
            throw new RuntimeException("Dag::start() should not be called more than once.");
        } else {
            isStarted = true;
        }

        onVerify();
        onPrepare();
        onDispatch();
        await();
    }

    private void onVerify() {
        Set<Class<? extends Task>> set = new HashSet<>();
        for (Task task : taskList) {
            Class<? extends Task> clazz = task.getClass();

            if (set.contains(clazz)) {
                throw new RuntimeException("Duplicate class type found: " + clazz.getName());
            } else {
                set.add(clazz);
            }
        }
    }

    private void onPrepare() {
        DagSolver.solve(taskList, taskChildren);

        taskCount = new AtomicInteger(taskList.size());
        latch = new CountDownLatch(waitCount);

        for (Task task : taskList) {
            List<Task> l = (task.onMainThread() ? mainThreadList : threadPoolList);
            l.add(task);
        }
    }

    private void onDispatch() {
        Executor executor = Executor.getInstance();
        ExecutorService compute = executor.computeExecutor();
        ExecutorService async = executor.asyncExecutor();

        for (Task task : threadPoolList) {
            ExecutorService service = (task instanceof ComputeTask) ? compute : async;
            service.execute(new TaskWorker(task));
        }

        for (Task task : mainThreadList) {
            task.run();
        }
    }

    private void await() {
        try {
            latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void notifyChildren(Task task) {
        List<Task> list = taskChildren.get(task.getClass());
        if (list == null || list.size() == 0) return;

        for (Task t : list) {
            t.doNotify();
        }
    }

    private void notifyMainThread(Task task) {
        if (isBlockMainThread(task)) {
            latch.countDown();
        }
    }

    private void shutdown() {
        if (taskCount.decrementAndGet() == 0) {
            Executor.getInstance().shutdown();
            sInstance = null;
        }
    }

    public Dag setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    private boolean isBlockMainThread(Task task) {
        return !task.onMainThread() && task.blockMainThread();
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
            notifyMainThread(mTask);
            shutdown();
        }
    }
}
