package org.cru.godtools.base.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

public class WeakTask<T> implements Runnable {
    @NonNull
    private final Reference<T> mObj;
    @NonNull
    private final Task<T> mTask;

    public WeakTask(@NonNull final T obj, @NonNull final Task<T> task) {
        mObj = new WeakReference<>(obj);
        mTask = task;
    }

    @Override
    @WorkerThread
    public void run() {
        final T obj = mObj.get();
        if (obj != null) {
            mTask.run(obj);
        }
    }

    @FunctionalInterface
    public interface Task<T> {
        void run(@NonNull T obj);
    }
}
