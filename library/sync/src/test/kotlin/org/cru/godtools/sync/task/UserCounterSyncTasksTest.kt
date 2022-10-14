package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coExcludeRecords
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.base.TimeConstants.WEEK_IN_MS
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserCountersApi
import org.cru.godtools.db.repository.InMemoryLastSyncTimeRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.sync.task.UserCounterSyncTasks.Companion.SYNC_TIME_COUNTERS
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

private const val USER_ID = "user_id"
private const val USER_ID_OTHER = "user_id_other"

@OptIn(ExperimentalCoroutinesApi::class)
class UserCounterSyncTasksTest {
    private val accountManager: GodToolsAccountManager = mockk {
        coEvery { isAuthenticated() } returns true
        coEvery { userId() } returns USER_ID
    }
    private val countersApi: UserCountersApi = mockk {
        coEvery { getCounters() } returns Response.success(JsonApiObject.of())
    }
    private val lastSyncTimeRepository = spyk(InMemoryLastSyncTimeRepository())
    private val userCountersRepository: UserCountersRepository = mockk {
        coEvery { transaction(any<suspend () -> Unit>()) } answers { callOriginal() }

        coExcludeRecords { transaction(any()) }
    }

    private val tasks =
        UserCounterSyncTasks(accountManager, countersApi, lastSyncTimeRepository, userCountersRepository)

    // region syncCounters()
    @Test
    fun `syncCounters() - not authenticated`() = runTest {
        coEvery { accountManager.isAuthenticated() } returns false

        assertTrue(tasks.syncCounters(false))
        coVerifyAll {
            accountManager.isAuthenticated()
            countersApi wasNot Called
            lastSyncTimeRepository wasNot Called
        }
    }

    @Test
    fun `syncCounters(force = false) - already synced`() = runTest {
        coEvery {
            lastSyncTimeRepository.isLastSyncStale(key = anyVararg<String>(), staleAfter = any())
        } returns false

        assertTrue(tasks.syncCounters(force = false))
        coVerifyAll {
            accountManager.isAuthenticated()
            accountManager.userId()
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_COUNTERS, USER_ID, staleAfter = any())
            countersApi wasNot Called
        }
    }

    @Test
    fun `syncCounters(force = true)`() = runTest {
        assertTrue(tasks.syncCounters(force = true))
        coVerifyAll {
            accountManager.isAuthenticated()
            accountManager.userId()
            countersApi.getCounters()
            lastSyncTimeRepository.resetLastSyncTime(any(), isPrefix = true)
            lastSyncTimeRepository.updateLastSyncTime(any(), any())
        }
    }

    @Test
    fun `syncCounters() - reset LastSyncTime for other users`() = runTest {
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_COUNTERS, USER_ID_OTHER)
        assertTrue(lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_COUNTERS, USER_ID, staleAfter = WEEK_IN_MS))
        assertFalse(lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_COUNTERS, USER_ID_OTHER, staleAfter = WEEK_IN_MS))

        assertTrue(tasks.syncCounters(false))
        coVerify { countersApi.getCounters() }
        assertFalse(lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_COUNTERS, USER_ID, staleAfter = WEEK_IN_MS))
        assertTrue(lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_COUNTERS, USER_ID_OTHER, staleAfter = WEEK_IN_MS))
    }
    // endregion syncCounters()
}
