package org.keynote.godtools.android.utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncTaskCompat
{
    private static final Object LOCK_EXECUTOR = new Object();
    private static Executor EXECUTOR;

    @NonNull
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static Executor getExecutor()
    {
        synchronized (LOCK_EXECUTOR)
        {
            if (EXECUTOR == null)
            {
                EXECUTOR = Executors.newFixedThreadPool(1);
                if (EXECUTOR instanceof ThreadPoolExecutor)
                {
                    ((ThreadPoolExecutor) EXECUTOR).setKeepAliveTime(30, TimeUnit.SECONDS);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
                    {
                        ((ThreadPoolExecutor) EXECUTOR).allowCoreThreadTimeOut(true);
                    }
                    else
                    {
                        ((ThreadPoolExecutor) EXECUTOR).setCorePoolSize(0);
                    }
                }
            }

            return EXECUTOR;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void execute(@NonNull final Runnable task)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        {
            getExecutor().execute(task);
        }
        else
        {
            AsyncTask.execute(task);
        }
    }
}