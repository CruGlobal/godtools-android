package org.cru.godtools.sync.task

import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.retrofit2.model.JsonApiRetrofitObject
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserApi
import org.cru.godtools.api.UserFavoriteToolsApi
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.User
import org.cru.godtools.sync.repository.SyncRepository

@Singleton
internal class UserFavoriteToolsSyncTasks @Inject constructor(
    private val accountManager: GodToolsAccountManager,
    private val favoritesApi: UserFavoriteToolsApi,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
    private val syncRepository: SyncRepository,
    private val toolsRepository: ToolsRepository,
    private val userApi: UserApi,
    private val userRepository: UserRepository,
) : BaseSyncTasks() {
    companion object {
        @VisibleForTesting
        internal const val SYNC_TIME_FAVORITE_TOOLS = "last_synced.favorite_tools"
        private const val STALE_DURATION_FAVORITE_TOOLS = TimeConstants.DAY_IN_MS
    }

    private val favoriteToolsMutex = Mutex()
    private val favoritesUpdateMutex = Mutex()

    suspend fun syncFavoriteTools(force: Boolean) = favoriteToolsMutex.withLock {
        if (!accountManager.isAuthenticated) return true
        val userId = accountManager.userId.orEmpty()

        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!force &&
            !lastSyncTimeRepository.isLastSyncStale(
                SYNC_TIME_FAVORITE_TOOLS,
                userId,
                staleAfter = STALE_DURATION_FAVORITE_TOOLS
            )
        ) {
            return true
        }

        val includes = Includes("${User.JSON_FAVORITE_TOOLS}.${Tool.JSON_METATOOL}.${Tool.JSON_DEFAULT_VARIANT}")
        val params = JsonApiParams()
            .includes(includes)
            .fields(Tool.JSONAPI_TYPE, *Tool.JSONAPI_FIELDS)
        val user = userApi.getUser(params).takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors }
            ?.dataSingle ?: return false

        syncRepository.storeUser(user, includes)
        lastSyncTimeRepository.resetLastSyncTime(SYNC_TIME_FAVORITE_TOOLS, isPrefix = true)
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_FAVORITE_TOOLS, user.id)

        true
    }

    suspend fun syncDirtyFavoriteTools(): Boolean = favoritesUpdateMutex.withLock {
        coroutineScope {
            if (!accountManager.isAuthenticated) return@coroutineScope true
            val userId = accountManager.userId.orEmpty()

            val user = userRepository.findUser(userId)?.takeIf { it.isInitialFavoriteToolsSynced }
                ?: userApi.getUser().takeIf { it.isSuccessful }
                    ?.body()?.dataSingle
                    ?.also { syncRepository.storeUser(it) }
                ?: return@coroutineScope false

            val favoritesToAdd = toolsRepository.getAllTools()
                .filter {
                    (it.isFieldChanged(Tool.ATTR_IS_FAVORITE) || !user.isInitialFavoriteToolsSynced) && it.isFavorite
                }

            val includes = Includes("${Tool.JSON_METATOOL}.${Tool.JSON_DEFAULT_VARIANT}")
            val params = JsonApiParams()
                .includes(includes)
                .fields(Tool.JSONAPI_TYPE, *Tool.JSONAPI_FIELDS)
            if (favoritesToAdd.isNotEmpty()) {
                favoritesApi.addFavoriteTools(params, favoritesToAdd).takeIf { it.isSuccessful }
                    ?.body()?.data
                    ?.also { syncRepository.storeFavoriteTools(it, includes) }
                    ?: return@coroutineScope false

                if (!user.isInitialFavoriteToolsSynced) {
                    launch {
                        val update = JsonApiRetrofitObject.single(User(userId, isInitialFavoriteToolsSynced = true))
                            .apply {
                                options = JsonApiConverter.Options.Builder()
                                    .fields(User.JSONAPI_TYPE, User.JSON_INITIAL_FAVORITE_TOOLS_SYNCED)
                                    .build()
                            }

                        userApi.updateUser(update).takeIf { it.isSuccessful }
                            ?.body()?.dataSingle
                            ?.also { syncRepository.storeUser(it) }
                            ?: return@launch
                    }
                }
            }

            val favoritesToRemove = toolsRepository.getAllTools()
                .filter { it.isFieldChanged(Tool.ATTR_IS_FAVORITE) && !it.isFavorite }

            if (favoritesToRemove.isNotEmpty()) {
                favoritesApi.removeFavoriteTools(params, favoritesToRemove).takeIf { it.isSuccessful }
                    ?.body()?.data
                    ?.also { syncRepository.storeFavoriteTools(it, includes) }
                    ?: return@coroutineScope false
            }

            true
        }
    }
}
