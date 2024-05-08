package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.room.changeFlow
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.TranslationEntity
import org.cru.godtools.db.room.entity.partial.SyncTranslation
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey

@Dao
internal abstract class TranslationsRoomRepository(private val db: GodToolsRoomDatabase) : TranslationsRepository {
    private inline val dao get() = db.translationsDao

    override suspend fun findTranslation(id: Long) = dao.findTranslation(id)?.toModel()

    override suspend fun findLatestTranslation(code: String?, locale: Locale?, downloadedOnly: Boolean) = when {
        code == null || locale == null -> null
        else -> dao.getLatestTranslations(code, locale).firstOrNull { !downloadedOnly || it.isDownloaded }?.toModel()
    }

    override fun findLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        downloadedOnly: Boolean,
    ): Flow<Translation?> = when {
        code == null || locale == null -> flowOf(null)
        else -> dao.getLatestTranslationsFlow(code, locale)
            .map { it.firstOrNull { !downloadedOnly || it.isDownloaded }?.toModel() }
    }

    override suspend fun getTranslations() = dao.getTranslations().map { it.toModel() }
    override suspend fun getTranslationsForTool(tool: String) =
        dao.getTranslationsForToolBlocking(tool).map { it.toModel() }
    override suspend fun getTranslationsForLanguages(languages: Collection<Locale>) =
        dao.getTranslationsForLanguages(languages).map { it.toModel() }

    override fun getTranslationsFlow() = dao.getTranslationsFlow().map { it.map { it.toModel() } }
    override fun getTranslationsFlowForTools(tools: Collection<String>) =
        dao.getTranslationsFlowForTools(tools).map { it.map { it.toModel() } }
    override fun getTranslationsFlowForToolsAndLocales(tools: Collection<String>, locales: Collection<Locale>) =
        dao.getTranslationsFlowForToolsAndLocales(tools, locales).map { it.map { it.toModel() } }

    override fun translationsChangeFlow(): Flow<Any?> = db.changeFlow("translations")

    // region DownloadManager Methods
    override suspend fun markTranslationDownloaded(id: Long, isDownloaded: Boolean) =
        dao.updateTranslationDownloaded(id, isDownloaded)

    @Transaction
    override suspend fun markStaleTranslationsAsNotDownloaded() = dao.getTranslations()
        .filter { it.isDownloaded }
        .groupBy { TranslationKey(it.tool, it.locale) }.entries
        .flatMap { (_, translations) -> translations.sortedByDescending { it.version }.drop(1) }
        .onEach { markTranslationDownloaded(it.id, false) }
        .isNotEmpty()
    // endregion DownloadManager Methods

    // region Initial Content Methods
    @Transaction
    override suspend fun storeInitialTranslations(translations: Collection<Translation>) {
        val tools = db.toolsDao.getTools().mapTo(mutableSetOf()) { it.code }
        val languages = db.languagesDao.getLanguages().mapTo(mutableSetOf()) { it.code }
        dao.insertOrIgnoreTranslations(
            translations
                .filter { it.toolCode in tools }
                .filter { it.languageCode in languages }
                .map { TranslationEntity(it) }
        )
    }
    // endregion Initial Content Methods

    // region ManifestManager Methods
    @Transaction
    override suspend fun markBrokenManifestNotDownloaded(manifestName: String) = dao.getTranslations()
        .filter { it.manifestFileName == manifestName && it.isDownloaded }
        .forEach { markTranslationDownloaded(it.id, false) }
    // endregion ManifestManager Methods

    // region Sync Methods
    override suspend fun storeTranslationFromSync(translation: Translation) = dao.upsert(SyncTranslation(translation))
    @Transaction
    override suspend fun deleteTranslationIfNotDownloaded(id: Long) {
        val translation = dao.findTranslation(id)?.takeUnless { it.isDownloaded } ?: return
        dao.delete(translation)
    }
    // endregion Sync Methods
}
