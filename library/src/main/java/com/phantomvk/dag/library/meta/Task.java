package com.phantomvk.dag.library.meta;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public abstract class Task {

    private final CountDownLatch mLatch;

    {
        List<Class<? extends Task>> tasks = dependsOn();
        mLatch = new CountDownLatch(tasks == null ? 0 : tasks.size());
    }

    @Nullable
    public List<Class<? extends Task>> dependsOn() {
        return null;
    }

    public void doAwait() {
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void run();

    public void doNotify() {
        mLatch.countDown();
    }

    public boolean shouldWait() {
        return false;
    }

    public boolean onMainThread() {
        return false;
    }

    public int priority() {
        return THREAD_PRIORITY_BACKGROUND;
    }
}
