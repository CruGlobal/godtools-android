package org.cru.godtools.base.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test

class PriorityRunnableTest {
    private val negative = TestPriorityRunnable(-1)
    private val one = TestPriorityRunnable(1)
    private val ten = TestPriorityRunnable(10)

    private val runnable = Runnable { }

    @Test
    fun verifyComparableInterface() {
        assertThat(listOf(one, ten, negative).sorted(), contains(negative, one, ten))
    }

    @Test
    fun verifyPriorityRunnableComparator() {
        assertThat(listOf(one, ten, negative).sortedWith(PriorityRunnable.COMPARATOR), contains(negative, one, ten))
    }

    @Test
    fun verifyPriorityRunnableComparatorMixed() {
        assertThat(
            listOf(one, runnable, ten, negative).sortedWith(PriorityRunnable.COMPARATOR),
            contains(negative, runnable, one, ten)
        )
    }

    class TestPriorityRunnable(override val priority: Int) : PriorityRunnable {
        override fun run() = Unit
    }
}
