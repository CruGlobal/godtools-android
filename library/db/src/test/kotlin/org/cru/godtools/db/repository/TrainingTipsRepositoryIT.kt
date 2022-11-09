package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.TrainingTip
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class TrainingTipsRepositoryIT {
    companion object {
        private const val TOOL = "tool"
        private const val TOOL2 = "tool2"
        private const val TIPID = "tip"
        private const val TIPID2 = "tip2"
    }

    abstract val repository: TrainingTipsRepository

    @Test
    fun `markTipComplete()`() = runTest {
        assertFalse(repository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIPID).first())
        assertFalse(repository.isTipCompleteFlow(TOOL2, Locale.ENGLISH, TIPID).first())
        assertFalse(repository.isTipCompleteFlow(TOOL, Locale.FRENCH, TIPID).first())
        assertFalse(repository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIPID2).first())

        repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID)
        assertTrue(repository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIPID).first())
        assertFalse(repository.isTipCompleteFlow(TOOL2, Locale.ENGLISH, TIPID).first())
        assertFalse(repository.isTipCompleteFlow(TOOL, Locale.FRENCH, TIPID).first())
        assertFalse(repository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIPID2).first())
    }

    @Test
    fun `isTipCompleteFlow()`() = runTest {
        repository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIPID).test {
            assertFalse(awaitItem())

            repository.markTipComplete(TOOL2, Locale.ENGLISH, TIPID)
            repository.markTipComplete(TOOL, Locale.FRENCH, TIPID)
            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID2)
            expectNoEvents()

            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID)
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `getCompletedTipsFlow()`() = runTest {
        repository.getCompletedTipsFlow().test {
            assertThat(awaitItem(), empty())

            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID)
            val tip1 = TrainingTip(TOOL, Locale.ENGLISH, TIPID, true)
            assertThat(awaitItem(), containsInAnyOrder(tip1))

            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID2)
            val tip2 = TrainingTip(TOOL, Locale.ENGLISH, TIPID2, true)
            assertThat(awaitItem(), containsInAnyOrder(tip1, tip2))

            repository.markTipComplete(TOOL, Locale.FRENCH, TIPID)
            val tip3 = TrainingTip(TOOL, Locale.FRENCH, TIPID, true)
            assertThat(awaitItem(), containsInAnyOrder(tip1, tip2, tip3))

            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID)
            assertThat(awaitItem(), containsInAnyOrder(tip1, tip2, tip3))
        }
    }
}
