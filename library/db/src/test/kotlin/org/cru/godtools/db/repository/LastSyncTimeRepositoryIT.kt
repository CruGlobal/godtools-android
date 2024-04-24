package org.cru.godtools.db.repository

import java.lang.Thread.sleep
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.hamcrest.Matchers.not

private const val KEY = "key"
private val COMPOUND_KEY = arrayOf(KEY, 1, true)

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
    fun `updateLastSyncTime() - Replace entry`() = runTest {
        repository.updateLastSyncTime(KEY)
        @Suppress("BlockingMethodInNonBlockingContext")
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

    // region isLastSyncStale()
    @Test
    fun `isLastSyncStale()`() = runTest {
        repository.updateLastSyncTime(KEY)
        @Suppress("BlockingMethodInNonBlockingContext")
        sleep(10)
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = 0))
        assertFalse(repository.isLastSyncStale(KEY, staleAfter = DAY_IN_MS))
    }

    @Test
    fun `isLastSyncStale() - compound key`() = runTest {
        repository.updateLastSyncTime(*COMPOUND_KEY)
        @Suppress("BlockingMethodInNonBlockingContext")
        sleep(10)
        assertTrue(repository.isLastSyncStale(*COMPOUND_KEY, staleAfter = 0))
        assertFalse(repository.isLastSyncStale(*COMPOUND_KEY, staleAfter = DAY_IN_MS))
    }

    @Test
    fun `isLastSyncStale() - always stale if not tracked yet`() = runTest {
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = 0))
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = System.currentTimeMillis() + DAY_IN_MS))
    }
    // endregion isLastSyncStale()

    // region resetLastSyncTime()
    private suspend fun createMultipleSyncTimes() {
        repository.updateLastSyncTime(KEY)
        @Suppress("BlockingMethodInNonBlockingContext")
        sleep(5)
        repository.updateLastSyncTime(KEY, 1)
    }

    @Test
    fun `resetLastSyncTime() - defaults to isPrefix=false`() = runTest {
        createMultipleSyncTimes()

        repository.resetLastSyncTime(KEY)
        assertEquals(0, repository.getLastSyncTime(KEY))
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = DAY_IN_MS))
        assertFalse(repository.isLastSyncStale(KEY, 1, staleAfter = DAY_IN_MS))
    }

    @Test
    fun `resetLastSyncTime() - isPrefix=false`() = runTest {
        createMultipleSyncTimes()

        repository.resetLastSyncTime(KEY, isPrefix = false)
        assertEquals(0, repository.getLastSyncTime(KEY))
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = DAY_IN_MS))
        assertFalse(repository.isLastSyncStale(KEY, 1, staleAfter = DAY_IN_MS))
    }

    @Test
    fun `resetLastSyncTime() - isPrefix=true`() = runTest {
        createMultipleSyncTimes()

        repository.resetLastSyncTime("%", isPrefix = true)

        repository.resetLastSyncTime(KEY, isPrefix = true)
        assertEquals(0, repository.getLastSyncTime(KEY))
        assertEquals(0, repository.getLastSyncTime(KEY, 1))
        assertTrue(repository.isLastSyncStale(KEY, staleAfter = DAY_IN_MS))
        assertTrue(repository.isLastSyncStale(KEY, 1, staleAfter = DAY_IN_MS))
    }

    @Test
    fun `resetLastSyncTime() - Prevent SQL injection attack`() = runTest {
        createMultipleSyncTimes()

        // % is the LIKE wildcard, ensure that this doesn't actually match anything
        repository.resetLastSyncTime("%", isPrefix = true)
        assertFalse(repository.isLastSyncStale(KEY, staleAfter = DAY_IN_MS))
        assertFalse(repository.isLastSyncStale(KEY, 1, staleAfter = DAY_IN_MS))
    }
    // endregion resetLastSyncTime()
}
