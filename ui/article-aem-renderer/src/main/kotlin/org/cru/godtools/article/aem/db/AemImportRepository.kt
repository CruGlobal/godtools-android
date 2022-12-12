package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Transaction
import java.util.Date
import org.ccci.gto.android.common.base.TimeConstants.WEEK_IN_MS
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.Article

@Dao
internal abstract class AemImportRepository(private val db: ArticleRoomDatabase) {
    private val aemImportDao get() = db.aemImportDao()

    @Transaction
    open suspend fun processAemImportSync(aemImportUri: Uri, articles: List<Article>) {
        // insert/update any supplied articles
        with(db.articleDao()) {
            articles.forEach {
                insertOrIgnore(it)
                update(it.uri, it.uuid, it.title, it.canonicalUri)

                // replace all tags
                removeAllTags(it.uri)
                insertOrIgnoreTags(it.tagObjects)
            }
        }

        // update associations between the AemImport and the articles
        val aemImport = aemImportDao.find(aemImportUri)
        if (aemImport != null) {
            aemImportDao.insertOrIgnoreArticles(articles.map { AemImport.AemImportArticle(aemImport, it) })
            aemImportDao.removeOldArticles(aemImportUri, articles.map { it.uri })

            // update the last processed time
            aemImportDao.updateLastProcessed(aemImportUri, Date())
        }

        // remove any orphaned articles
        db.articleRepository().removeOrphanedArticles()
    }

    @Transaction
    open suspend fun accessAemImport(uri: Uri) {
        with(db.aemImportDao()) {
            insertOrIgnore(AemImport(uri))
            updateLastAccessed(uri, Date())
        }
    }

    @Transaction
    open suspend fun removeOrphanedAemImports() {
        db.aemImportDao().removeOrphanedAemImports(Date(System.currentTimeMillis() - WEEK_IN_MS))
        db.articleRepository().removeOrphanedArticles()
    }
}
