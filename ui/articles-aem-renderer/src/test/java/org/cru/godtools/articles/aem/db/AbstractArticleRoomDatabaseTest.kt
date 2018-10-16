package org.cru.godtools.articles.aem.db

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock

abstract class AbstractArticleRoomDatabaseTest {
    protected val aemImportDao = mock<AemImportDao> {}
    internal val translationDao = mock<TranslationDao> {}
    protected val db = mock<ArticleRoomDatabase> {
        on { aemImportDao() } doReturn aemImportDao
        on { translationDao() } doReturn translationDao
    }
}
