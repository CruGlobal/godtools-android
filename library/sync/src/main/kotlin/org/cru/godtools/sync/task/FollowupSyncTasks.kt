package org.cru.godtools.sync.task

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.cru.godtools.api.FollowupApi
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.db.repository.LanguagesRepository

@Singleton
internal class FollowupSyncTasks @Inject internal constructor(
    private val followupApi: FollowupApi,
    private val followupsRepository: FollowupsRepository,
    private val languagesRepository: LanguagesRepository,
) : BaseSyncTasks() {
    private val followupsMutex = Mutex()

    suspend fun syncFollowups() = followupsMutex.withLock {
        coroutineScope {
            followupsRepository.getFollowups()
                .map { followup ->
                    async {
                        try {
                            followup.setLanguage(languagesRepository.findLanguage(followup.languageCode))
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
