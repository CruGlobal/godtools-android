package org.keynote.godtools.android.db.repository

import android.database.sqlite.SQLiteDatabase
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyLanguagesRepository @Inject constructor(private val dao: GodToolsDao) : LanguagesRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val languagesCache = WeakLruCache<Locale, Flow<Language?>>(3)

    override fun getLanguageFlow(locale: Locale) = languagesCache.getOrPut(locale) {
        dao.findAsFlow<Language>(it)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    }

    override suspend fun getLanguages() = dao.getAsync(Query.select<Language>()).await()

    override fun getLanguagesForLocalesFlow(locales: Collection<Locale>) = Query.select<Language>()
        .where(LanguageTable.FIELD_CODE.`in`(*Expression.constants(*locales.toTypedArray())))
        .getAsFlow(dao)

    override suspend fun storeInitialLanguages(languages: Collection<Language>) = dao.transaction {
        languages.filter { it.isValid }.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
    }

    override fun storeLanguagesFromSync(languages: Collection<Language>) = dao.transaction {
        languages.filter { it.isValid }.forEach {
            dao.updateOrInsert(
                it, SQLiteDatabase.CONFLICT_REPLACE,
                LanguageTable.COLUMN_ID, LanguageTable.COLUMN_NAME
            )
        }
    }

    override suspend fun removeLanguagesMissingFromSync(syncedLanguages: Collection<Language>) = dao.transaction {
        val syncedLocales = syncedLanguages.filter { it.isValid }.map { it.code }
        dao.get(Query.select<Language>())
            .filterNot { it.code in syncedLocales }
            .forEach { dao.delete(it) }
    }
}
