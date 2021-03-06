package com.phantomvk.dag.app.task;

import android.util.Log;

import com.phantomvk.dag.library.meta.ComputeTask;
import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppComputeTask11 extends ComputeTask {

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
