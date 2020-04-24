package org.cru.godtools.sync.task

import android.os.Bundle
import androidx.collection.SimpleArrayMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.db.Expression.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.cru.godtools.model.Language
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_TIME_LANGUAGES = "last_synced.languages"
private const val STALE_DURATION_LANGUAGES = TimeConstants.WEEK_IN_MS

@Singleton
class LanguagesSyncTasks @Inject internal constructor(dao: GodToolsDao, eventBus: EventBus) :
    BaseDataSyncTasks(dao, eventBus) {
    private val languagesMutex = Mutex()

    suspend fun syncLanguages(args: Bundle) = withContext(Dispatchers.IO) {
        languagesMutex.withLock {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!isForced(args) &&
                System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_LANGUAGES) < STALE_DURATION_LANGUAGES
            ) return@withContext true

            // fetch & store languages
            api.languages.list(JsonApiParams()).takeIf { it.isSuccessful }?.body()?.let { json ->
                val events = SimpleArrayMap<Class<*>, Any>()
                dao.transaction {
                    val existing = dao.get(Query.select<Language>())
                        .groupingBy { it.code }
                        .reduce { _, lang1, lang2 ->
                            Timber.tag("LanguagesSyncTask").d(
                                RuntimeException("Duplicate Language sync error"),
                                "Duplicate languages detected: %s %s", lang1, lang2
                            )
                            dao.delete(
                                Language::class.java,
                                LanguageTable.FIELD_ID.`in`(*constants(lang1.id, lang2.id))
                            )
                            lang1
                        }
                        .toMutableMap()
                    storeLanguages(events, json.data, existing)
                }

                sendEvents(events)
                dao.updateLastSyncTime(SYNC_TIME_LANGUAGES)
            } ?: return@withContext false
        }
        return@withContext true
    }
}
