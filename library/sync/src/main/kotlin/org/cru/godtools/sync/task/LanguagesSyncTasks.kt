package org.cru.godtools.sync.task

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.cru.godtools.api.LanguagesApi
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository

private const val SYNC_TIME_LANGUAGES = "last_synced.languages"
private const val STALE_DURATION_LANGUAGES = TimeConstants.WEEK_IN_MS

@Singleton
internal class LanguagesSyncTasks @Inject constructor(
    private val languagesApi: LanguagesApi,
    private val languagesRepository: LanguagesRepository,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
) : BaseSyncTasks() {
    private val languagesMutex = Mutex()

    suspend fun syncLanguages(force: Boolean = false) = withContext(Dispatchers.IO) {
        languagesMutex.withLock {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!force &&
                !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_LANGUAGES, staleAfter = STALE_DURATION_LANGUAGES)
            ) {
                return@withContext true
            }

            // fetch languages from the API
            val json = languagesApi.list(JsonApiParams()).takeIf { it.isSuccessful }?.body() ?: return@withContext false

            // store languages
            val languages = json.data.filter { it.isValid }
            languagesRepository.removeLanguagesMissingFromSync(languages)
            languagesRepository.storeLanguagesFromSync(languages)
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_LANGUAGES)
        }
        return@withContext true
    }
}
