package org.cru.godtools.base.util;

import android.support.annotation.NonNull;

import com.google.common.primitives.Ints;

import java.util.Comparator;

public abstract class PriorityRunnable implements Comparable<PriorityRunnable>, Runnable {
    private static final int PRIORITY_DEFAULT = 0;

    public static final Comparator<Runnable> COMPARATOR = (r1, r2) -> {
        int p1 = r1 instanceof PriorityRunnable ? ((PriorityRunnable) r1).getPriority() : PRIORITY_DEFAULT;
        int p2 = r2 instanceof PriorityRunnable ? ((PriorityRunnable) r2).getPriority() : PRIORITY_DEFAULT;
        return Ints.compare(p1, p2);
    };

    protected int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public final int compareTo(@NonNull final PriorityRunnable o) {
        return COMPARATOR.compare(this, o);
    }
}
