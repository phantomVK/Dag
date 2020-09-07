package com.phantomvk.dag.app.task;

import android.util.Log;

import com.phantomvk.dag.library.meta.ComputeTask;

public class AppComputeTask0 extends ComputeTask {

    @Override
    public void run() {
        try {
            Thread.sleep(300);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}