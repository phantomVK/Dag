package com.phantomvk.dag.app.task;

import android.util.Log;

import androidx.annotation.Nullable;

import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayList;
import java.util.List;

public class AppComputeTaskMain1 extends ComputeTask {

    @Override
    public void onExecute() {
        Log.e(this.getClass().getName(), "Finished");
    }

    @Override
    public boolean onMainThread() {
        return true;
    }

    @Nullable
    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> tasks = new ArrayList<>(1);
        tasks.add(AppComputeTaskMain0.class);
        return tasks;
    }
}
