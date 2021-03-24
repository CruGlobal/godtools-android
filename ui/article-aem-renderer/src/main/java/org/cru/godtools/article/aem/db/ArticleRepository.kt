package org.cru.godtools.article.aem.db

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.article.aem.model.Article

@Dao
abstract class ArticleRepository internal constructor(private val db: ArticleRoomDatabase) {
    @AnyThread
    @Transaction
    open suspend fun updateContent(article: Article) {
        db.articleDao().updateContent(article.uri, article.contentUuid, article.content)
        db.resourceRepository().storeResources(article, article.resources)
    }

    @Transaction
    @WorkerThread
    open fun removeOrphanedArticles() {
        db.articleDao().removeOrphanedArticles()
        db.resourceDao().removeOrphanedResources()
    }
}
