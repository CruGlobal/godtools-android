package org.cru.godtools.article.aem.db

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

abstract class AbstractArticleRoomDatabaseTest {
    internal val aemImportDao = mock<AemImportDao>()
    internal val aemImportRepository = mock<AemImportRepository>()
    internal val articleRepository = mock<ArticleRepository>()
    internal val translationDao = mock<TranslationDao>()
    protected val db = mock<ArticleRoomDatabase> {
        on { aemImportDao() } doReturn aemImportDao
        on { aemImportRepository() } doReturn aemImportRepository
        on { articleRepository() } doReturn articleRepository
        on { translationDao() } doReturn translationDao
    }
}
