package com.phantomvk.dag.app.task;

import android.util.Log;

import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.meta.ComputeTask;

import java.util.ArrayList;
import java.util.List;

public class AppComputeTask1 extends ComputeTask {

    @Override
    public void onExecute() {
        try {
            Thread.sleep(1000);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
