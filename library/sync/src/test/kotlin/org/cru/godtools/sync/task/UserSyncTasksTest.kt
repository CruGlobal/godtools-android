package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.excludeRecords
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserApi
import org.cru.godtools.db.repository.InMemoryLastSyncTimeRepository
import org.cru.godtools.model.User
import org.cru.godtools.sync.repository.SyncRepository
import org.cru.godtools.sync.task.UserSyncTasks.Companion.SYNC_TIME_USER
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

private const val USER_ID = "user_id"

class UserSyncTasksTest {
    private val accountManager: GodToolsAccountManager = mockk {
        coEvery { isAuthenticated() } returns true
        coEvery { userId() } returns USER_ID
    }
    private val userApi: UserApi = mockk()
    private val lastSyncTimeRepository = spyk(InMemoryLastSyncTimeRepository()) {
        excludeRecords { setLastSyncTime(key = anyVararg(), time = any()) }
    }
    private val syncRepository: SyncRepository = mockk()

    private val tasks = UserSyncTasks(
        accountManager,
        lastSyncTimeRepository,
        syncRepository = syncRepository,
        userApi = userApi,
    )

    // region syncCounters()
    @Test
    fun `syncUser() - not authenticated`() = runTest {
        coEvery { accountManager.isAuthenticated() } returns false

        assertTrue(tasks.syncUser(Random.nextBoolean()))
        coVerifyAll {
            accountManager.isAuthenticated()
            userApi wasNot Called
            lastSyncTimeRepository wasNot Called
        }
    }

    @Test
    fun `syncUser(force = false) - already synced`() = runTest {
        coEvery {
            lastSyncTimeRepository.isLastSyncStale(key = anyVararg<String>(), staleAfter = any())
        } returns false

        assertTrue(tasks.syncUser(force = false))
        coVerifyAll {
            accountManager.isAuthenticated()
            accountManager.userId()
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_USER, USER_ID, staleAfter = any())
            userApi wasNot Called
        }
    }

    @Test
    fun `syncUser(force = true)`() = runTest {
        val startTime = System.currentTimeMillis()
        val user = User(id = USER_ID)
        coEvery { userApi.getUser() } returns Response.success(JsonApiObject.of(user))
        coEvery { syncRepository.storeUser(user, UserSyncTasks.INCLUDES_GET_USER) } just Runs

        assertTrue(tasks.syncUser(force = true))
        coVerifyAll {
            accountManager.isAuthenticated()
            accountManager.userId()
            userApi.getUser()
            syncRepository.storeUser(user, UserSyncTasks.INCLUDES_GET_USER)
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_USER, USER_ID)
        }
        assertTrue(lastSyncTimeRepository.getLastSyncTime(SYNC_TIME_USER, USER_ID) >= startTime)
    }

    @Test
    fun `syncUser(force = false) - last sync stale`() = runTest {
        val startTime = System.currentTimeMillis()
        val user = User(id = USER_ID)
        coEvery { lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_USER, USER_ID, staleAfter = any()) } returns true
        coEvery { userApi.getUser() } returns Response.success(JsonApiObject.of(user))
        coEvery { syncRepository.storeUser(user, UserSyncTasks.INCLUDES_GET_USER) } just Runs

        assertTrue(tasks.syncUser(force = true))
        coVerifyAll {
            accountManager.isAuthenticated()
            accountManager.userId()
            userApi.getUser()
            syncRepository.storeUser(user, UserSyncTasks.INCLUDES_GET_USER)
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_USER, USER_ID)
        }
        assertTrue(lastSyncTimeRepository.getLastSyncTime(SYNC_TIME_USER, USER_ID) >= startTime)
    }
    // endregion syncCounters()
}
