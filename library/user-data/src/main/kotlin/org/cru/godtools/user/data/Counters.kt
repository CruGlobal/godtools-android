package org.cru.godtools.user.data

import android.database.sqlite.SQLiteDatabase
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cru.godtools.model.UserCounter
import org.cru.godtools.sync.GodToolsSyncService
import org.keynote.godtools.android.db.GodToolsDao

class Counters @Inject internal constructor(
    private val dao: GodToolsDao,
    private val syncService: GodToolsSyncService
) {
    suspend fun updateCounter(id: String, change: Int = 1) = withContext(Dispatchers.IO) {
        with(dao) {
            transaction {
                insert(UserCounter(id), SQLiteDatabase.CONFLICT_IGNORE)
                updateUserCounterDelta(id, change)
            }
        }
        syncService.syncDirtyUserCounters().sync()
    }
}
