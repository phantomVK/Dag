package com.phantomvk.dag.library;

import android.content.Context;
import android.os.Looper;
import android.os.Process;

import com.phantomvk.dag.library.exector.Executor;
import com.phantomvk.dag.library.meta.AsyncTask;
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

/**
 * A directed acyclic graphs scheduler to organize tasks in Android.
 */
public final class Dag {

    private static volatile Dag sInstance;

    /**
     * CountDownLatch to block MainThread.
     */
    private CountDownLatch latch;

    /**
     * Count down to shutdown ExecuteService until {@param taskCount} is 0.
     */
    private AtomicInteger taskCount;

    /**
     * Auto shutdown ExecuteService after all tasks finished.
     */
    private volatile boolean autoShutdown = true;

    /**
     * Task list, for more information, see {@link Task}.
     */
    private final List<Task> tasks;
    private final boolean inMainProcess;

    // Dag global timeout.
    private long timeout = 60 * 1000; // 60s
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
        tasks = new ArrayList<>();
        inMainProcess = ProcessUtility.inMainProcess(context);
    }

    /**
     * Add task to Dag.
     *
     * @param task see {@link AsyncTask} and {@link ComputeTask}.
     * @return Dag instance
     */
    public Dag addTask(Task task) {
        if (task == null) {
            throw new NullPointerException("Task should not be null.");
        }

        tasks.add(task);

        return this;
    }

    /**
     * Start to execute tasks, this method can be called only once.
     */
    public void start() {
        // Process scope.
        if (!inMainProcess) {
            throw new RuntimeException("Dag::start() must run on main process.");
        }

        // Thread scope.
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("Dag::start() must run on MainThread.");
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

        // The count of tasks that MainThread should block and wait.
        int blockCount = 0;
        for (Task t : tasks) if (t.blockMainThread()) blockCount++;
        latch = new CountDownLatch(blockCount);
    }

    private void onDispatch() {
        List<Task> subThreadTasks = new ArrayList<>();
        List<Task> mainThreadTasks = new ArrayList<>();

        for (Task task : tasks) {
            List<Task> l = (task.inMainThread() ? mainThreadTasks : subThreadTasks);
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
            new TaskWorker(task).run();
        }

        tasks.clear();
    }

    private void await() {
        try {
            latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void notifyMainThread(Task task) {
        if (task.blockMainThread()) {
            latch.countDown();
        }
    }

    private void shutdown() {
        if (autoShutdown && taskCount.decrementAndGet() == 0) {
            Executor.shutdown();
            sInstance = null;
        }
    }

    /**
     * Auto shutdown ExecuteService after all tasks finished.
     *
     * @param value boolean
     * @return Dag instance
     */
    public Dag setAutoShutdown(boolean value) {
        autoShutdown = value;
        return this;
    }

    /**
     * Set dag global timeout.
     *
     * @param timeout  long-integer timeout.
     * @param timeUnit see {@link TimeUnit}.
     * @return Dag instance
     */
    public Dag setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    private class TaskWorker implements Runnable {

        private final Task mTask;

        public TaskWorker(Task task) {
            mTask = task;
        }

        @Override
        public void run() {
            Process.setThreadPriority(mTask.getPriority());
            mTask.onPreExecute();
            mTask.onExecute();
            mTask.onPostExecute();
            notifyMainThread(mTask);
            shutdown();
        }
    }
}
