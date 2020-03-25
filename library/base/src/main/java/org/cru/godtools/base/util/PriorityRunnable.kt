package org.cru.godtools.base.util

import java.util.Comparator

private const val PRIORITY_DEFAULT = 0

interface PriorityRunnable : Comparable<PriorityRunnable>, Runnable {
    @JvmDefault
    val priority get() = PRIORITY_DEFAULT

    @JvmDefault
    override fun compareTo(other: PriorityRunnable) = COMPARATOR.compare(this, other)

    companion object {
        @JvmField
        val COMPARATOR = Comparator { r1: Runnable?, r2: Runnable? ->
            val p1 = (r1 as? PriorityRunnable)?.priority ?: PRIORITY_DEFAULT
            val p2 = (r2 as? PriorityRunnable)?.priority ?: PRIORITY_DEFAULT
            p1.compareTo(p2)
        }
    }
}
