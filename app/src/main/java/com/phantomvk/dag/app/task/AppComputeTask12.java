package com.phantomvk.dag.app.task;

import android.util.Log;

import androidx.annotation.Nullable;

import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayList;
import java.util.List;

public class AppComputeTask12 extends ComputeTask {

    @Override
    public void onExecute() {
        try {
            Thread.sleep(10000);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean blockMainThread() {
        return true;
    }

    @Override
    public boolean onMainThread() {
        return true;
    }

    @Nullable
    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> dependsOn = new ArrayList<>();
        dependsOn.add(AppComputeTask3.class);
        return dependsOn;
    }
}
