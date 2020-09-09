package com.phantomvk.dag.library.meta;

import android.os.Process;

/**
 * For compute-intensive task.
 */
public abstract class ComputeTask extends Task {

    @Override
    public int getPriority() {
        return Process.THREAD_PRIORITY_DEFAULT;
    }
}
