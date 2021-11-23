package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.model.TranslationRef.Key
import org.cru.godtools.article.aem.model.TranslationRef.TranslationAemImport
import org.cru.godtools.article.aem.model.toTranslationRefKey
import org.cru.godtools.model.Translation

@Dao
internal abstract class TranslationRepository internal constructor(private val db: ArticleRoomDatabase) {
    private suspend fun find(key: Key?) = key?.run { db.translationDao().find(tool, language, version) }
    suspend fun isProcessed(translation: Translation) = find(translation.toTranslationRefKey())?.processed ?: false

    private suspend fun markProcessed(key: Key?, processed: Boolean) {
        key?.apply { db.translationDao().markProcessed(tool, language, version, processed) }
    }

    @Transaction
    open suspend fun addAemImports(translation: Translation, uris: List<Uri>): Boolean {
        val translationKey = translation.toTranslationRefKey() ?: return false

        // create translation ref if it doesn't exist already
        db.translationDao().insertOrIgnore(TranslationRef(translationKey))

        // create and link all AEM Import objects
        val imports = uris.map { AemImport(it) }
        val translationImports = imports.map { TranslationAemImport(translationKey, it) }
        db.aemImportDao().insertOrIgnore(imports)
        db.translationDao().insertOrIgnore(translationImports)

        // mark translation as processed
        markProcessed(translationKey, true)

        return true
    }

    @Transaction
    open suspend fun removeMissingTranslations(translationsToKeep: List<Translation>) {
        val valid = translationsToKeep.map { it.toTranslationRefKey() }.toSet()
        db.translationDao().getAll()
            .filterNot { valid.contains(it.key) }
            .apply { db.translationDao().remove(this) }
        db.aemImportRepository().removeOrphanedAemImports()
    }
}
