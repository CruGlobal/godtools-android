package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
import org.cru.godtools.articles.aem.model.Article

@Dao
abstract class ArticleRepository internal constructor(private val mDb: ArticleRoomDatabase) {
    @Transaction
    open fun updateContent(article: Article) {
        mDb.articleDao().updateContent(article.uri, article.contentUuid, article.content)
        mDb.attachmentRepository().storeAttachments(article, article.mAttachments)
    }
}
