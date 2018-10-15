package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
import android.net.Uri
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.AemImport
import org.cru.godtools.articles.aem.model.TranslationRef
import org.cru.godtools.articles.aem.model.TranslationRef.Key
import org.cru.godtools.articles.aem.model.TranslationRef.TranslationAemImport
import org.cru.godtools.model.Translation

@Dao
abstract class TranslationRepository internal constructor(private val db: ArticleRoomDatabase) {
    @WorkerThread
    fun find(key: Key?): TranslationRef? {
        return key?.run { db.translationDao().find(tool, language, version) }
    }

    @WorkerThread
    fun isProcessed(translation: Translation): Boolean {
        return find(Key.from(translation))?.processed ?: false
    }

    @WorkerThread
    fun markProcessed(key: Key?, processed: Boolean) {
        key?.apply { db.translationDao().markProcessed(tool, language, version, processed) }
    }

    @Transaction
    @WorkerThread
    open fun addAemImports(translation: Translation, uris: List<Uri>): Boolean {
        val translationKey = Key.from(translation) ?: return false

        // create translation ref if it doesn't exist already
        db.translationDao().insertOrIgnore(TranslationRef(translationKey))

        // create and link all AEM Import objects
        uris.map { AemImport(it) }
                .apply { db.aemImportDao().insertOrIgnore(this) }
                .map { TranslationAemImport(translationKey, it) }
                .apply { db.translationDao().insertOrIgnore(this) }

        // mark translation as processed
        markProcessed(translationKey, true)

        return true
    }

    @Transaction
    @WorkerThread
    open fun removeMissingTranslations(translationsToKeep: List<Translation>) {
        val valid = translationsToKeep.map { Key.from(it) }
        db.translationDao().all
                .filterNot { valid.contains(it.key) }
                .apply { db.translationDao().remove(this) }
        db.aemImportDao().removeOrphanedAemImports()
    }
}
