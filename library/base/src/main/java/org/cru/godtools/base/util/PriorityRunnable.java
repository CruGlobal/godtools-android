package org.cru.godtools.base.util;

import android.support.annotation.NonNull;

import com.google.common.primitives.Ints;

public abstract class PriorityRunnable implements Comparable<PriorityRunnable>, Runnable {
    protected int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public final int compareTo(@NonNull final PriorityRunnable o) {
        return Ints.compare(getPriority(), o.getPriority());
    }
}
