package org.cru.godtools.sync.task

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ccci.gto.android.common.db.find
import org.cru.godtools.api.FollowupApi
import org.cru.godtools.db.repository.FollowupsRepository
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class FollowupSyncTasks @Inject internal constructor(
    private val dao: GodToolsDao,
    private val followupApi: FollowupApi,
    private val followupsRepository: FollowupsRepository,
) : BaseSyncTasks() {
    private val followupsMutex = Mutex()

    suspend fun syncFollowups() = followupsMutex.withLock {
        coroutineScope {
            followupsRepository.getFollowups()
                .map { followup ->
                    async {
                        try {
                            followup.languageCode?.let { followup.setLanguage(dao.find(it)) }
                            followupApi.subscribe(followup).isSuccessful
                                .also {
                                    if (it) {
                                        followupsRepository.deleteFollowup(followup)
                                    }
                                }
                        } catch (e: IOException) {
                            false
                        }
                    }
                }
                .all { it.await() }
        }
    }
}
