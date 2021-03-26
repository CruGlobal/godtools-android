package org.cru.godtools.article.aem.db

import androidx.annotation.AnyThread
import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.model.Resource

@Dao
abstract class ResourceRepository internal constructor(private val db: ArticleRoomDatabase) {
    @AnyThread
    @Transaction
    open suspend fun storeResources(article: Article, resources: List<Resource>) {
        val articleDao = db.articleDao()
        val resourceDao = db.resourceDao()

        resources.forEach {
            resourceDao.insertOrIgnore(it)
            articleDao.insertOrIgnore(Article.ArticleResource(article, it))
        }
        articleDao.removeOldResources(article.uri, resources.map { it.uri })
    }
}
