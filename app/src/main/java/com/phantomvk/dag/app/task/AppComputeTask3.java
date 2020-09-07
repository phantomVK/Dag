package com.phantomvk.dag.app.task;

import android.util.Log;

import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.meta.ComputeTask;

import java.util.ArrayList;
import java.util.List;

public class AppComputeTask3 extends ComputeTask {

    @Override
    public void run() {
        try {
            Thread.sleep(3000);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> list = new ArrayList<>(2);
        list.add(AppComputeTask0.class);
        list.add(AppComputeTask2.class);
        return list;
    }

    @Override
    public boolean shouldWait() {
        return true;
    }
}
