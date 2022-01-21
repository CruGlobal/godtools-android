package org.cru.godtools.user.data

import android.database.sqlite.SQLiteDatabase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.model.UserCounter
import org.cru.godtools.model.UserCounter.Companion.VALID_NAME
import org.cru.godtools.sync.GodToolsSyncService
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class Counters @Inject internal constructor(
    private val dao: GodToolsDao,
    private val syncService: GodToolsSyncService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun updateCounterAsync(name: String, change: Int = 1) {
        require(VALID_NAME.matches(name)) { "Invalid counter name: $name" }
        coroutineScope.launch { updateCounter(name, change) }
    }
    private suspend fun updateCounter(name: String, change: Int = 1) {
        withContext(Dispatchers.IO) {
            with(dao) {
                transaction {
                    insert(UserCounter(name), SQLiteDatabase.CONFLICT_IGNORE)
                    updateUserCounterDelta(name, change)
                }
            }
        }
        syncService.syncDirtyUserCounters().sync()
    }
}
