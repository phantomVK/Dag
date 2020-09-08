package com.phantomvk.dag.library.utility;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Process;

import java.util.List;

public final class ProcessUtility {

    public static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    public static String getProcessName(Context context) {
        ActivityManager m = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> info = m.getRunningAppProcesses();

        if (info == null || info.size() == 0) return null;

        int myPid = Process.myPid();
        for (RunningAppProcessInfo i : info) {
            if (i.pid == myPid) return i.processName;
        }

        return null;
    }
}
