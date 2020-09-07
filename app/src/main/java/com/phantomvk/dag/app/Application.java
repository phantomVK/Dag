package com.phantomvk.dag.app;

import com.phantomvk.dag.app.task.AppComputeTask0;
import com.phantomvk.dag.app.task.AppComputeTask1;
import com.phantomvk.dag.app.task.AppComputeTask2;
import com.phantomvk.dag.app.task.AppComputeTask3;
import com.phantomvk.dag.library.Dag;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Dag.getInstance(this)
                .addTask(new AppComputeTask0())
                .addTask(new AppComputeTask1())
                .addTask(new AppComputeTask2())
                .addTask(new AppComputeTask3())
                .start();
    }
}
