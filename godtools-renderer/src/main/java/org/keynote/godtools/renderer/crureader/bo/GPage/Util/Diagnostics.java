package org.keynote.godtools.renderer.crureader.bo.GPage.Util;

import android.os.Looper;
import android.util.Log;

import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

public class Diagnostics {


    private static final String TAG = "Diagnostics";

    private static final long UI_THREAD_PROCESS_TIME_ALLOWANCE = 15;

    public static void StartMethodTracingByKey(String key) {
        StartMethodTracingByKeyWithTag(TAG, key);
    }

    public static void StopMethodTracingByKey(String key) {
        StopMethodTracingByKeyWithTag(TAG, key, key);
    }

    public static void StartMethodTracingByKeyWithTag(String Tag, String key) {
        if (RenderSingleton.IS_DEBUG_BUILD) {

            RenderSingleton.getInstance().getMethodTraceMilliSecondsKeyMap().put(key, System.currentTimeMillis());
        }
    }

    public static void StopMethodTracingByKeyWithTag(String Tag, String key,
                                                     String leadingMessage) {
        if (RenderSingleton.IS_DEBUG_BUILD) {
            Log.v(TAG, "Is Diagnostics on UI Thread: " + (Looper.myLooper() == Looper.getMainLooper() ? " true " : " false "));
            if (RenderSingleton.getInstance().getMethodTraceMilliSecondsKeyMap().containsKey(key)) {
                long processTime = System.currentTimeMillis() - RenderSingleton.getInstance().getMethodTraceMilliSecondsKeyMap()
                        .remove(key);
                Log
                        .v(TAG,
                                Tag + "   " + leadingMessage + "   "
                                        + processTime + " milliseconds");

                if (processTime > UI_THREAD_PROCESS_TIME_ALLOWANCE && isOnUIThread()) {
                    alertProcessTooLong(leadingMessage);

                }
            }
        }

    }

/*Very bad for performance*/
//    public static void logMemory(String leadingMessage) {
//        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
//        Debug.getMemoryInfo(memoryInfo);
//
//        String memMessage = String.format("Memory_ + " + leadingMessage + "_: Pss=%.2f MB, Private=%.2f MB, Shared=%.2f MB", memoryInfo.getTotalPss() / 1024.0,
//                memoryInfo.getTotalPrivateDirty() / 1024.0, memoryInfo.getTotalSharedDirty() / 1024.0);
//
//        Log.v(TAG, memMessage);
//
//    }

    public static boolean isOnUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    private static void alertProcessTooLong(String leadingMessage) {
        Log.e(TAG, "ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT " +
                "ALERT ALERT ALERT process (  " + leadingMessage + " ) too long on UI Thread"
                + " ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT ALERT");
    }
}
