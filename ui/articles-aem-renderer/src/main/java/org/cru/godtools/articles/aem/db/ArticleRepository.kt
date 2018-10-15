package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.Article

@Dao
abstract class ArticleRepository internal constructor(private val db: ArticleRoomDatabase) {
    @Transaction
    open fun updateContent(article: Article) {
        db.articleDao().updateContent(article.uri, article.contentUuid, article.content)
        db.resourceRepository().storeResources(article, article.mResources)
    }

    @Transaction
    @WorkerThread
    fun removeOrphanedArticles() {
        db.articleDao().removeOrphanedArticles()
        db.resourceDao().removeOrphanedResources()
    }
}
