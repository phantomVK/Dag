package com.phantomvk.dag.library.meta;

/**
 * For main thread task.
 */
public abstract class MainThreadTask extends CommonTask {

    @Override
    public boolean onMainThread() {
        return true;
    }
}
