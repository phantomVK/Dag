package com.phantomvk.dag.library.meta;

import android.os.Process;

import androidx.annotation.NonNull;

import com.phantomvk.dag.library.Dag;

public class TaskWorker implements Runnable {

    private final Task mTask;
    private final Dag mDag;

    public TaskWorker(@NonNull Task task, @NonNull Dag dag) {
        mTask = task;
        mDag = dag;
    }

    @Override
    public void run() {
        Process.setThreadPriority(mTask.priority());
        mTask.doWait();
        mTask.run();
        mDag.notifyChildren(mTask);
        mDag.taskFinished(mTask);
    }
}
