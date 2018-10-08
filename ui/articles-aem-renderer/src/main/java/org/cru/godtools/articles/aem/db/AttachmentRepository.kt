package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
import org.cru.godtools.articles.aem.model.Article
import org.cru.godtools.articles.aem.model.Attachment

@Dao
abstract class AttachmentRepository internal constructor(private val mDb: ArticleRoomDatabase) {
    @Transaction
    open fun storeAttachments(article: Article, attachments: List<Attachment>) {
        val articleDao = mDb.articleDao()
        val attachmentDao = mDb.attachmentDao()

        attachments.forEach {
            attachmentDao.insertOrIgnore(it)
            articleDao.insertOrIgnore(Article.ArticleAttachment(article, it))
        }
        articleDao.removeOldAttachments(article.uri, attachments.map { it.uri })
    }
}
