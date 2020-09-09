package com.phantomvk.dag.library.meta;

import android.os.Process;

/**
 * For io-intensive task.
 */
public abstract class AsyncTask extends Task {

    @Override
    public int getPriority() {
        return Process.THREAD_PRIORITY_LOWEST;
    }
}
