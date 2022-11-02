package org.cru.godtools.user.activity

import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.model.UserCounter
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.sync.GodToolsSyncService

@Singleton
class UserActivityManager internal constructor(
    private val syncService: Lazy<GodToolsSyncService>,
    private val userCountersRepository: UserCountersRepository,
    private val coroutineScope: CoroutineScope,
) {
    @Inject
    internal constructor(
        syncService: Lazy<GodToolsSyncService>,
        userCountersRepository: UserCountersRepository,
    ) : this(syncService, userCountersRepository, CoroutineScope(SupervisorJob()))

    // region Counters
    fun isValidCounterName(name: String) = UserCounter.VALID_NAME.matches(name)

    private val countersFlow = userCountersRepository.getCountersFlow()
        .map { it.associateBy({ it.id }, { it.count }) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)
        .distinctUntilChanged()

    suspend fun updateCounter(name: String, change: Int = 1) {
        require(isValidCounterName(name)) { "Invalid counter name: $name" }
        userCountersRepository.updateCounter(name, change)
        coroutineScope.launch { syncService.get().syncDirtyUserCounters() }
    }
    // endregion Counters

    val userActivityFlow = countersFlow
        .map { UserActivity(it) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)
        .distinctUntilChanged()
}
