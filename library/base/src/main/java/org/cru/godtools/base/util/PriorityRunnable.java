package org.cru.godtools.base.util;

import com.google.common.primitives.Ints;

import java.util.Comparator;

import androidx.annotation.NonNull;

public interface PriorityRunnable extends Comparable<PriorityRunnable>, Runnable {
    int PRIORITY_DEFAULT = 0;

    Comparator<Runnable> COMPARATOR = (r1, r2) -> {
        int p1 = r1 instanceof PriorityRunnable ? ((PriorityRunnable) r1).getPriority() : PRIORITY_DEFAULT;
        int p2 = r2 instanceof PriorityRunnable ? ((PriorityRunnable) r2).getPriority() : PRIORITY_DEFAULT;
        return Ints.compare(p1, p2);
    };

    default int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    default int compareTo(@NonNull final PriorityRunnable o) {
        return COMPARATOR.compare(this, o);
    }
}
