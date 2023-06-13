package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.LanguageEntity
import org.cru.godtools.model.Language

@Dao
internal abstract class LanguagesRoomRepository(private val db: GodToolsRoomDatabase) : LanguagesRepository {
    val dao get() = db.languagesDao

    override suspend fun findLanguage(locale: Locale) = dao.findLanguage(locale)?.toModel()
    override fun findLanguageFlow(locale: Locale) = dao.findLanguageFlow(locale).map { it?.toModel() }
    override suspend fun getLanguages() = dao.getLanguages().map { it.toModel() }
    override fun getLanguagesFlowForLocales(locales: Collection<Locale>): Flow<Collection<Language>> =
        dao.getLanguagesFlow(locales).map { it.map { it.toModel() } }

    override suspend fun storeInitialLanguages(languages: Collection<Language>) {
        dao.insertOrIgnoreLanguages(languages.map { LanguageEntity(it) })
    }

    // region Sync Methods
    override fun storeLanguagesFromSync(languages: Collection<Language>) {
        dao.upsertLanguagesBlocking(languages.map { LanguageEntity(it) })
    }

    @Transaction
    override suspend fun removeLanguagesMissingFromSync(syncedLanguages: Collection<Language>) {
        val syncedLocales = syncedLanguages.map { it.code }.toSet()
        dao.deleteLanguages(dao.getLanguages().filterNot { it.code in syncedLocales })
    }
    // endregion Sync Methods
}
