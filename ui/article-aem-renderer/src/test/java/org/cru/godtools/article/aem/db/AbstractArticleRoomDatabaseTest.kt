package org.cru.godtools.article.aem.db

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

abstract class AbstractArticleRoomDatabaseTest {
    protected val aemImportDao = mock<AemImportDao> {}
    internal val translationDao = mock<TranslationDao> {}
    protected val db = mock<ArticleRoomDatabase> {
        on { aemImportDao() } doReturn aemImportDao
        on { translationDao() } doReturn translationDao
    }
}
