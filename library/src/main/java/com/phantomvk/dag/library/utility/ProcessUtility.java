package com.phantomvk.dag.library.utility;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Process;

import java.util.List;

public final class ProcessUtility {

    public static boolean inMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    public static String getProcessName(Context context) {
        ActivityManager m = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> inf = m.getRunningAppProcesses();

        if (inf == null || inf.size() == 0) return null;

        int myPid = Process.myPid();
        for (RunningAppProcessInfo i : inf) {
            if (i.pid == myPid) return i.processName;
        }

        return null;
    }
}
