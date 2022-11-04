package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
        repository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIPID).distinctUntilChanged().test {
            assertFalse(awaitItem())

            repository.markTipComplete(TOOL2, Locale.ENGLISH, TIPID)
            repository.markTipComplete(TOOL, Locale.FRENCH, TIPID)
            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID2)
            expectNoEvents()

            repository.markTipComplete(TOOL, Locale.ENGLISH, TIPID)
            assertTrue(awaitItem())
        }
    }
}
