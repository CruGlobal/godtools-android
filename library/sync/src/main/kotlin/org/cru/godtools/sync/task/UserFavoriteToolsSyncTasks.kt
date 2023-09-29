package org.cru.godtools.sync.task

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.retrofit2.model.JsonApiRetrofitObject
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserApi
import org.cru.godtools.api.UserFavoriteToolsApi
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.User
import org.cru.godtools.sync.repository.SyncRepository

@Singleton
internal class UserFavoriteToolsSyncTasks @Inject constructor(
    private val accountManager: GodToolsAccountManager,
    private val favoritesApi: UserFavoriteToolsApi,
    private val syncRepository: SyncRepository,
    private val toolsRepository: ToolsRepository,
    private val userApi: UserApi,
    private val userRepository: UserRepository,
) : BaseSyncTasks() {
    private val favoritesUpdateMutex = Mutex()

    suspend fun syncDirtyFavoriteTools(): Boolean = favoritesUpdateMutex.withLock {
        coroutineScope {
            if (!accountManager.isAuthenticated()) return@coroutineScope true
            val userId = accountManager.userId().orEmpty()

            val user = userRepository.findUser(userId)?.takeIf { it.isInitialFavoriteToolsSynced }
                ?: userApi.getUser().takeIf { it.isSuccessful }
                    ?.body()?.dataSingle
                    ?.also { syncRepository.storeUser(it) }
                ?: return@coroutineScope false

            val favoritesToAdd = toolsRepository.getResources()
                .filter {
                    (it.isFieldChanged(Tool.ATTR_IS_FAVORITE) || !user.isInitialFavoriteToolsSynced) && it.isFavorite
                }

            val params = JsonApiParams().fields(Tool.JSONAPI_TYPE, *Tool.JSONAPI_FIELDS)
            if (favoritesToAdd.isNotEmpty()) {
                favoritesApi.addFavoriteTools(params, favoritesToAdd).takeIf { it.isSuccessful }
                    ?.body()?.data
                    ?.also { syncRepository.storeFavoriteTools(it) }
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

            val favoritesToRemove = toolsRepository.getResources()
                .filter { it.isFieldChanged(Tool.ATTR_IS_FAVORITE) && !it.isFavorite }

            if (favoritesToRemove.isNotEmpty()) {
                favoritesApi.removeFavoriteTools(params, favoritesToRemove).takeIf { it.isSuccessful }
                    ?.body()?.data
                    ?.also { syncRepository.storeFavoriteTools(it) }
                    ?: return@coroutineScope false
            }

            true
        }
    }
}
