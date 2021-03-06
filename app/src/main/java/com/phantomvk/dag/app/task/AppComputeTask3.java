package com.phantomvk.dag.app.task;

import android.util.Log;

import androidx.annotation.Nullable;

import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.meta.ComputeTask;

import java.util.ArrayList;
import java.util.List;

public class AppComputeTask3 extends ComputeTask {

    @Override
    public void onExecute() {
        try {
            Thread.sleep(10000);
            Log.e(this.getClass().getName(), "Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> dependsOn = new ArrayList<>();
        dependsOn.add(AppComputeTask11.class);
        return dependsOn;
    }
}
