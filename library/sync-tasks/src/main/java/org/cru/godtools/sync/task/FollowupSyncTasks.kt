package org.cru.godtools.sync.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.cru.godtools.model.Followup
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowupSyncTasks @Inject internal constructor(private val dao: GodToolsDao, eventBus: EventBus) :
    BaseSyncTasks(eventBus) {
    private val followupMutex = Mutex()

    suspend fun syncFollowups() = withContext(Dispatchers.IO) {
        followupMutex.withLock {
            coroutineScope {
                dao.get(Query.select<Followup>())
                    .map { followup ->
                        async {
                            try {
                                followup.languageCode?.let { followup.setLanguage(dao.find(it)) }
                                followup.stashId()
                                api.followups.subscribe(followup).isSuccessful
                                    .also {
                                        if (it) {
                                            followup.restoreId()
                                            dao.delete(followup)
                                        }
                                    }
                            } catch (e: IOException) {
                                false
                            }
                        }
                    }.all { it.await() }
            }
        }
    }
}
