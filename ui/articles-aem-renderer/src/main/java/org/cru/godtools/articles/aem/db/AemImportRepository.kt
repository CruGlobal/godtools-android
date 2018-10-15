package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
import android.support.annotation.WorkerThread
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
    }

    @Transaction
    @WorkerThread
    open fun removeOrphanedAemImports() {
        db.aemImportDao().removeOrphanedAemImports()
        db.articleDao().removeOrphanedArticles()
    }
}
