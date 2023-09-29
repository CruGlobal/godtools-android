package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coExcludeRecords
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserApi
import org.cru.godtools.api.UserFavoriteToolsApi
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.User
import org.cru.godtools.model.trackChanges
import org.cru.godtools.sync.repository.SyncRepository
import retrofit2.Response

class UserFavoriteToolsSyncTasksTest {
    private val userId = UUID.randomUUID().toString()

    private val accountManager: GodToolsAccountManager = mockk {
        coEvery { isAuthenticated() } returns true
        coEvery { userId() } returns userId
    }
    private val favoritesApi: UserFavoriteToolsApi = mockk {
        coEvery { addFavoriteTools(any(), any()) } returns Response.success(JsonApiObject.of())
        coEvery { removeFavoriteTools(any(), any()) } returns Response.success(JsonApiObject.of())
    }
    private val syncRepository: SyncRepository = mockk {
        coEvery { storeUser(any(), any()) } just Runs
        coEvery { storeFavoriteTools(any(), any()) } just Runs
    }
    private val toolsRepository: ToolsRepository = mockk {
        coEvery { getResources() } returns emptyList()
    }
    private val userApi: UserApi = mockk {
        coEvery { getUser(any()) }
            .returns(Response.success(JsonApiObject.single(User(userId, isInitialFavoriteToolsSynced = true))))
        coEvery { updateUser(any()) } returns Response.success(JsonApiObject.single(User(userId)))
    }
    private val userRepository: UserRepository = mockk {
        coEvery { findUser(userId) } returns User(userId, isInitialFavoriteToolsSynced = true)
    }

    private val tasks = UserFavoriteToolsSyncTasks(
        accountManager = accountManager,
        favoritesApi = favoritesApi,
        syncRepository = syncRepository,
        toolsRepository = toolsRepository,
        userApi = userApi,
        userRepository = userRepository,
    )

    // region syncDirtyFavoriteTools()
    @Test
    fun `syncDirtyFavoriteTools() - add new favorites`() = runTest {
        val tools = listOf(
            Tool("1") {
                id = 1
                isFavorite = true
            },
            Tool("2") {
                id = 2
                trackChanges { isFavorite = true }
            },
            Tool("3") {
                id = 3
                isFavorite = false
            },
        )
        val responseTool = Tool("resp")

        coEvery { toolsRepository.getResources() } returns tools
        coEvery { favoritesApi.addFavoriteTools(any(), any()) } returns Response.success(JsonApiObject.of(responseTool))

        assertTrue(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            favoritesApi.addFavoriteTools(any(), listOf(tools[1]))
            syncRepository.storeFavoriteTools(listOf(responseTool), any())

            userApi wasNot Called
        }
    }

    @Test
    fun `syncDirtyFavoriteTools() - initial favorites`() = runTest {
        val user = User(userId, isInitialFavoriteToolsSynced = false)
        val tools = listOf(
            Tool("1") {
                id = 1
                isFavorite = true
            },
            Tool("2") {
                id = 2
                trackChanges { isFavorite = true }
            },
            Tool("3") {
                id = 3
                isFavorite = false
            },
        )
        val responseTool = Tool("resp")

        coEvery { userRepository.findUser(userId) } returns null
        coEvery { userApi.getUser(any()) } returns Response.success(JsonApiObject.single(user))
        coEvery { toolsRepository.getResources() } returns tools
        coEvery { favoritesApi.addFavoriteTools(any(), any()) } returns Response.success(JsonApiObject.of(responseTool))
        coExcludeRecords {
            userApi.getUser(any())
            syncRepository.storeUser(any(), any())
        }

        assertTrue(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            favoritesApi.addFavoriteTools(any(), listOf(tools[0], tools[1]))
            syncRepository.storeFavoriteTools(listOf(responseTool), any())
            userApi.updateUser(match { it.dataSingle == User(userId, isInitialFavoriteToolsSynced = true) })
        }
    }

    @Test
    fun `syncDirtyFavoriteTools() - remove old favorites`() = runTest {
        val tools = listOf(
            Tool("1") {
                id = 1
                isFavorite = true
            },
            Tool("2") {
                id = 2
                isFavorite = false
            },
            Tool("3") {
                id = 3
                isFavorite = true
                trackChanges { isFavorite = false }
            },
        )
        val responseTool = Tool("resp")

        coEvery { toolsRepository.getResources() } returns tools
        coEvery { favoritesApi.removeFavoriteTools(any(), any()) }
            .returns(Response.success(JsonApiObject.of(responseTool)))

        assertTrue(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            favoritesApi.removeFavoriteTools(any(), listOf(tools[2]))
            syncRepository.storeFavoriteTools(listOf(responseTool), any())

            userApi wasNot Called
        }
    }

    @Test
    fun `syncDirtyFavoriteTools() - not authenticated`() = runTest {
        coEvery { accountManager.isAuthenticated() } returns false

        assertTrue(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            accountManager.isAuthenticated()

            userRepository wasNot Called
            userApi wasNot Called
            syncRepository wasNot Called
            toolsRepository wasNot Called
            favoritesApi wasNot Called
        }
    }

    @Test
    fun `syncDirtyFavoriteTools() - user not found`() = runTest {
        coEvery { userRepository.findUser(userId) } returns null
        coEvery { userApi.getUser(any()) } returns Response.success(JsonApiObject.of())

        assertFalse(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            userRepository.findUser(userId)
            userApi.getUser(any())

            syncRepository wasNot Called
            toolsRepository wasNot Called
            favoritesApi wasNot Called
        }
    }

    @Test
    fun `syncDirtyFavoriteTools() - user resolved from API - user not found`() = runTest {
        coEvery { userRepository.findUser(userId) } returns null

        assertTrue(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            userRepository.findUser(userId)
            userApi.getUser(any())
            syncRepository.storeUser(any(), any())
        }
    }

    @Test
    fun `syncDirtyFavoriteTools() - user resolved from API - user hasn't synced initial favorites`() = runTest {
        coEvery { userRepository.findUser(userId) } returns User(userId, isInitialFavoriteToolsSynced = false)

        assertTrue(tasks.syncDirtyFavoriteTools())
        coVerifySequence {
            userRepository.findUser(userId)
            userApi.getUser(any())
            syncRepository.storeUser(any(), any())
        }
    }
    // endregion syncDirtyFavoriteTools()
}
