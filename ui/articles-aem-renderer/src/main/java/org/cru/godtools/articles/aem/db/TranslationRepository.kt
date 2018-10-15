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
    fun isProcessed(translation: Translation): Boolean {
        val transRef = db.translationDao().find(Key.from(translation))
        return transRef?.processed ?: false
    }

    @Transaction
    @WorkerThread
    open fun addAemImports(translation: Translation, importUris: List<Uri>): Boolean {
        val translationKey = Key.from(translation) ?: return false

        // create translation ref if it doesn't exist already
        db.translationDao().insertOrIgnore(TranslationRef(translationKey))

        // create and link all AEM Import objects
        val imports = importUris.map { AemImport(it) }
        val relations = imports.map { TranslationAemImport(translationKey, it) }
        db.aemImportDao().insertOrIgnore(imports, relations)

        // mark translation as processed
        db.translationDao().markProcessed(translationKey, true)

        return true
    }

    @Transaction
    @WorkerThread
    open fun removeMissingTranslations(translationsToKeep: List<Translation>) {
        val valid = translationsToKeep.map { Key.from(it) }
        db.translationDao().all
                .filterNot { valid.contains(it.key) }
                .apply { db.translationDao().remove(this) }
    }
}
