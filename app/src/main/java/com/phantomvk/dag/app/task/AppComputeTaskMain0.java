package com.phantomvk.dag.app.task;

import android.util.Log;

import com.phantomvk.dag.library.meta.ComputeTask;

public class AppComputeTaskMain0 extends ComputeTask {

    @Override
    public void onExecute() {
        Log.e(this.getClass().getName(), "Finished");
    }
}
