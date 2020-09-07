package com.phantomvk.dag.library;

import android.content.Context;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;

import com.phantomvk.dag.library.exector.Executor;
import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.utility.DagSolver;
import com.phantomvk.dag.library.utility.ProcessUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Dag {

    private static final int TIMEOUT_MS = 60 * 1000; // 60s

    private static volatile Dag sInstance;

    private int blockCount;
    private boolean running;
    private CountDownLatch latch;
    private AtomicInteger taskCount;

    private final List<Task> tasks;
    private final boolean isMainProcess;

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
        tasks = new ArrayList<>();
    }

    public Dag addTask(Task task) {
        if (task == null) return this;

        tasks.add(task);

        if (task.blockMainThread()) {
            blockCount++;
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
        if (running) {
            throw new RuntimeException("Dag::start() should not be called more than once.");
        } else {
            running = true;
        }

        onVerify();
        onPrepare();
        onDispatch();
        await();
    }

    private void onVerify() {
        Set<Class<? extends Task>> set = new HashSet<>();
        for (Task task : tasks) {
            Class<? extends Task> clazz = task.getClass();

            if (set.contains(clazz)) {
                throw new RuntimeException("Duplicate class type found: " + clazz.getName());
            } else {
                set.add(clazz);
            }
        }
    }

    private void onPrepare() {
        DagSolver.solve(tasks);
        taskCount = new AtomicInteger(tasks.size());
        latch = new CountDownLatch(blockCount);
    }

    private void onDispatch() {
        List<Task> subThreadTasks = new ArrayList<>();
        List<Task> mainThreadTasks = new ArrayList<>();

        for (Task task : tasks) {
            List<Task> l = (task.onMainThread() ? mainThreadTasks : subThreadTasks);
            l.add(task);
        }

        Executor executor = Executor.getInstance();
        ExecutorService compute = executor.computeExecutor();
        ExecutorService async = executor.asyncExecutor();

        for (Task task : subThreadTasks) {
            ExecutorService service = (task instanceof ComputeTask) ? compute : async;
            service.execute(new TaskWorker(task));
        }

        for (Task task : mainThreadTasks) {
            task.onExecute();
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
        for (Task t : task.getChildren()) {
            t.doNotify();
        }
    }

    private void notifyMainThread(Task task) {
        if (task.blockMainThread()) {
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

    public class TaskWorker implements Runnable {

        private final Task mTask;

        public TaskWorker(@NonNull Task task) {
            mTask = task;
        }

        @Override
        public void run() {
            Process.setThreadPriority(mTask.priority());
            mTask.doAwait();
            mTask.onExecute();
            mTask.onPostExecute();
            notifyMainThread(mTask);
            shutdown();
        }
    }
}
