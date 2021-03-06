package com.phantomvk.dag.app;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import com.phantomvk.dag.app.task.AppComputeTask0;
import com.phantomvk.dag.app.task.AppComputeTask1;
import com.phantomvk.dag.app.task.AppComputeTask10;
import com.phantomvk.dag.app.task.AppComputeTask11;
import com.phantomvk.dag.app.task.AppComputeTask12;
import com.phantomvk.dag.app.task.AppComputeTask2;
import com.phantomvk.dag.app.task.AppComputeTask3;
import com.phantomvk.dag.app.task.AppComputeTask4;
import com.phantomvk.dag.app.task.AppComputeTask5;
import com.phantomvk.dag.app.task.AppComputeTask6;
import com.phantomvk.dag.app.task.AppComputeTask7;
import com.phantomvk.dag.app.task.AppComputeTask8;
import com.phantomvk.dag.app.task.AppComputeTask9;
import com.phantomvk.dag.app.task.AppComputeTaskMain0;
import com.phantomvk.dag.app.task.AppComputeTaskMain1;
import com.phantomvk.dag.app.task.AppComputeTaskMain2;
import com.phantomvk.dag.library.Dag;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(this.getClass().getName(), "Started.");
        Dag.getInstance(this)
                .addTask(new AppComputeTask0())
                .addTask(new AppComputeTask1())
                .addTask(new AppComputeTask2())
                .addTask(new AppComputeTask3())
                .addTask(new AppComputeTask4())
                .addTask(new AppComputeTask5())
                .addTask(new AppComputeTask6())
                .addTask(new AppComputeTask7())
                .addTask(new AppComputeTask8())
                .addTask(new AppComputeTask9())
                .addTask(new AppComputeTask10())
                .addTask(new AppComputeTask11())
                .addTask(new AppComputeTask12())
                .addTask(new AppComputeTaskMain0())
                .addTask(new AppComputeTaskMain1())
                .addTask(new AppComputeTaskMain2())
                .start();
    }
}
