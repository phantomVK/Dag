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
            Thread.sleep(5000);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> list = new ArrayList<>(2);
        list.add(AppComputeTask0.class);
        return list;
    }
}
