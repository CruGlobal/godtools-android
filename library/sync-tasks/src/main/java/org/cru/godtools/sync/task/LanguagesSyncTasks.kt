package org.cru.godtools.sync.task

import android.content.Context
import android.os.Bundle
import androidx.collection.SimpleArrayMap
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.db.Expression.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract.LanguageTable
import timber.log.Timber
import java.io.IOException

private const val SYNC_TIME_LANGUAGES = "last_synced.languages"
private const val STALE_DURATION_LANGUAGES = TimeConstants.WEEK_IN_MS
private val LOCK_SYNC_LANGUAGES = Any()

class LanguagesSyncTasks private constructor(context: Context) : BaseDataSyncTasks(context) {
    companion object : SingletonHolder<LanguagesSyncTasks, Context>(::LanguagesSyncTasks)

    @Throws(IOException::class)
    fun syncLanguages(args: Bundle): Boolean {
        synchronized(LOCK_SYNC_LANGUAGES) {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!BaseSyncTasks.isForced(args) &&
                System.currentTimeMillis() - mDao.getLastSyncTime(SYNC_TIME_LANGUAGES) < STALE_DURATION_LANGUAGES
            ) return true

            // fetch languages from the API, short-circuit if this response is invalid
            val response = mApi.languages.list(JsonApiParams()).execute()
            if (response.code() != 200) return false

            // store languages
            val events = SimpleArrayMap<Class<*>, Any>()
            response.body()?.let { json ->
                mDao.transaction {
                    val existing = mDao.get(Query.select<Language>())
                        .groupingBy { it.code }
                        .reduce { _, lang1, lang2 ->
                            Timber.tag("LanguagesSyncTask").d(
                                RuntimeException("Duplicate Language sync error"),
                                "Duplicate languages detected: %s %s", lang1, lang2
                            )
                            mDao.delete(
                                Language::class.java,
                                LanguageTable.FIELD_ID.`in`(*constants(lang1.id, lang2.id))
                            )
                            lang1
                        }
                    storeLanguages(events, json.data, existing)
                }

                sendEvents(events)
                mDao.updateLastSyncTime(SYNC_TIME_LANGUAGES)
            }
        }
        return true
    }
}
