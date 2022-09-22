package org.cru.godtools.user.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.model.UserCounter.Companion.VALID_NAME
import org.cru.godtools.sync.GodToolsSyncService

@Singleton
class Counters @Inject internal constructor(
    private val syncService: GodToolsSyncService,
    private val userCountersRepository: UserCountersRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun updateCounterAsync(name: String, change: Int = 1) {
        require(VALID_NAME.matches(name)) { "Invalid counter name: $name" }
        coroutineScope.launch { updateCounter(name, change) }
    }
    private suspend fun updateCounter(name: String, change: Int = 1) {
        userCountersRepository.updateCounter(name, change)
        syncService.syncDirtyUserCounters().sync()
    }
}
