package org.cru.godtools.db.repository

import java.lang.Thread.sleep
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.base.TimeConstants.MIN_IN_MS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val KEY = "key"
private val COMPOUND_KEY = arrayOf(KEY, 1, true)

@OptIn(ExperimentalCoroutinesApi::class)
abstract class LastSyncTimeRepositoryIT {
    internal abstract val repository: LastSyncTimeRepository

    @Test
    fun `updateLastSyncTime() - Initial entry`() = runTest {
        val before = System.currentTimeMillis()
        repository.updateLastSyncTime(KEY)
        val after = System.currentTimeMillis()
        assertThat(repository.getLastSyncTime(KEY), allOf(greaterThanOrEqualTo(before), lessThanOrEqualTo(after)))
    }

    @Test
    @Suppress("BlockingMethodInNonBlockingContext")
    fun `updateLastSyncTime() - Replace entry`() = runTest {
        repository.updateLastSyncTime(KEY)
        sleep(10)
        val before = System.currentTimeMillis()
        assertThat(repository.getLastSyncTime(KEY), allOf(not(equalTo(0)), lessThan(before)))
        repository.updateLastSyncTime(KEY)
        val after = System.currentTimeMillis()
        assertThat(repository.getLastSyncTime(KEY), allOf(greaterThanOrEqualTo(before), lessThanOrEqualTo(after)))
        val time = repository.getLastSyncTime(KEY)
        assertTrue(time >= before)
        assertTrue(time <= after)
    }

    @Test
    fun `getLastSyncTime() - Initially 0`() = runTest {
        assertEquals(0, repository.getLastSyncTime(UUID.randomUUID()))
    }

    @Test
    fun `getLastSyncTime() - Compound Key`() = runTest {
        val before = System.currentTimeMillis()
        repository.updateLastSyncTime(*COMPOUND_KEY)
        val after = System.currentTimeMillis()
        assertThat(
            repository.getLastSyncTime(*COMPOUND_KEY),
            allOf(greaterThanOrEqualTo(before), lessThanOrEqualTo(after))
        )
    }

    @Test
    @Suppress("BlockingMethodInNonBlockingContext")
    fun `isLastSyncStale()`() = runTest {
        repository.updateLastSyncTime(KEY)
        sleep(10)
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = 0))
        assertFalse(repository.isLastSyncStale(KEY, staleAfter = MIN_IN_MS))
    }

    @Test
    @Suppress("BlockingMethodInNonBlockingContext")
    fun `isLastSyncStale() - compound key`() = runTest {
        repository.updateLastSyncTime(*COMPOUND_KEY)
        sleep(10)
        assertTrue(repository.isLastSyncStale(*COMPOUND_KEY, staleAfter = 0))
        assertFalse(repository.isLastSyncStale(*COMPOUND_KEY, staleAfter = MIN_IN_MS))
    }

    @Test
    fun `isLastSyncStale() - always stale if not tracked yet`() = runTest {
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = 0))
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = System.currentTimeMillis() + MIN_IN_MS))
    }
}
