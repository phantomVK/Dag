package com.phantomvk.dag.library.meta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;

public abstract class Task {

    private int degree;
    private final List<Task> children;
    private final CountDownLatch latch;

    {
        List<Class<? extends Task>> tasks = dependsOn();
        degree = (tasks == null) ? 0 : tasks.size();
        latch = new CountDownLatch(degree);
        children = new ArrayList<>();
    }

    @Nullable
    public List<Class<? extends Task>> dependsOn() {
        return null;
    }

    public void doAwait() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void onExecute();

    public void doNotify() {
        latch.countDown();
    }

    public boolean blockMainThread() {
        return false;
    }

    public boolean onMainThread() {
        return false;
    }

    public int priority() {
        return THREAD_PRIORITY_DEFAULT;
    }

    public int getDegree() {
        return degree;
    }

    public int decreaseDegree() {
        return --degree;
    }

    @NonNull
    public List<Task> getChildren() {
        return children;
    }

    public void onPostExecute() {
        for (Task task : children) {
            task.doNotify();
        }
    }
}
