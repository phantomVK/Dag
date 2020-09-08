package com.phantomvk.dag.app.task;

import android.util.Log;

import com.phantomvk.dag.library.meta.ComputeTask;

public class AppComputeTask6 extends ComputeTask {

    @Override
    public void onExecute() {
        try {
            Thread.sleep(10000);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
