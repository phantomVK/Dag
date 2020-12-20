package com.phantomvk.dag.library.meta;

import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class Task {

    /**
     * Task in-degree.
     */
    private int degree;

    /**
     * The list of tasks that depend on this task.
     */
    private final List<Task> children;

    /**
     * CountDownLatch to wait until all parent tasks finished.
     */
    private final CountDownLatch latch;

    {
        List<Class<? extends Task>> tasks = dependsOn();
        degree = (tasks == null) ? 0 : tasks.size();
        latch = new CountDownLatch(degree);
        children = new ArrayList<>();
    }

    /**
     * The list contains all parent tasks this task depends on.
     *
     * @return nullable list
     */
    @Nullable
    public List<Class<? extends Task>> dependsOn() {
        return null;
    }

    public void onPreExecute() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void onExecute();

    public void onPostExecute() {
        for (Task task : children) {
            task.latch.countDown();
        }
    }

    /**
     * Main thread should wait until this task finished.
     */
    public boolean blockMainThread() {
        return false;
    }

    /**
     * This task should run on main thread.
     */
    public boolean onMainThread() {
        return false;
    }

    public int getPriority() {
        return Process.THREAD_PRIORITY_BACKGROUND;
    }

    /**
     * Get all children tasks. The list is NonNull, but it maybe empty.
     */
    @NonNull
    public List<Task> getChildren() {
        return children;
    }

    public int decrementAndGetDegree() {
        return --degree;
    }
}
