package org.cru.godtools.articles.aem.db

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.articles.aem.model.AemImport
import org.cru.godtools.articles.aem.model.Article
import java.util.Date

@Dao
abstract class AemImportRepository internal constructor(private val db: ArticleRoomDatabase) {
    @Transaction
    @WorkerThread
    open fun processAemImportSync(aemImport: AemImport, articles: List<Article>) {
        // insert/update any supplied articles
        with(db.articleDao()) {
            articles.forEach {
                insertOrIgnore(it)
                update(it.uri, it.uuid, it.title)

                // replace all tags
                removeAllTags(it.uri)
                insertOrIgnoreTags(it.tagObjects)
            }
        }

        // update associations between the AemImport and the articles
        with(db.aemImportDao()) {
            insertOrIgnoreArticles(articles.map { AemImport.AemImportArticle(aemImport, it) })
            removeOldArticles(aemImport.uri, articles.map { it.uri })

            // update the last processed time
            updateLastProcessed(aemImport.uri, Date())
        }

        // remove any orphaned articles
        db.articleRepository().removeOrphanedArticles()
    }

    @Transaction
    @WorkerThread
    open fun removeOrphanedAemImports() {
        db.aemImportDao().removeOrphanedAemImports()
        db.articleRepository().removeOrphanedArticles()
    }
}
