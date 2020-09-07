package com.phantomvk.dag.library.meta;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public abstract class CommonTask {

    private final CountDownLatch mLatch;

    {
        List<CommonTask> tasks = dependsOn();
        mLatch = new CountDownLatch(tasks == null ? 0 : tasks.size());
    }

    public List<CommonTask> dependsOn() {
        return null;
    }

    public void doWait() {
        try {
            mLatch.wait();
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
