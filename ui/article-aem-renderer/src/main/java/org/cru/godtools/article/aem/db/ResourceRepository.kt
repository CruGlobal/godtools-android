package org.cru.godtools.article.aem.db

import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.model.Resource

@Dao
internal abstract class ResourceRepository(private val db: ArticleRoomDatabase) {
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
