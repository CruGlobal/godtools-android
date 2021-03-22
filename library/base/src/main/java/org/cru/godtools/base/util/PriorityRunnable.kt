package org.cru.godtools.base.util

private const val PRIORITY_DEFAULT = 0

interface PriorityRunnable : Runnable, Comparable<PriorityRunnable> {
    val priority get() = PRIORITY_DEFAULT
    override fun compareTo(other: PriorityRunnable) = COMPARATOR.compare(this, other)

    companion object {
        @JvmField
        val COMPARATOR = compareBy<Runnable?> { (it as? PriorityRunnable)?.priority ?: PRIORITY_DEFAULT }
    }
}
