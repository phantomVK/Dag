package com.phantomvk.dag.app.task;

import com.phantomvk.dag.library.meta.ComputeTask;

public class AppComputeTask0 extends ComputeTask {

    @Override
    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
