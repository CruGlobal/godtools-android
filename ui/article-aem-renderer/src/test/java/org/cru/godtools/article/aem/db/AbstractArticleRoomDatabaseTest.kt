package org.cru.godtools.article.aem.db

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

abstract class AbstractArticleRoomDatabaseTest {
    internal val aemImportDao = mock<AemImportDao>()
    internal val aemImportRepository = mock<AemImportRepository>()
    internal val articleDao = mock<ArticleDao>()
    internal val articleRepository = mock<ArticleRepository>()
    internal val resourceDao = mock<ResourceDao>()
    internal val resourceRepository = mock<ResourceRepository>()
    internal val translationDao = mock<TranslationDao>()
    protected val db = mock<ArticleRoomDatabase> {
        on { aemImportDao() } doReturn aemImportDao
        on { aemImportRepository() } doReturn aemImportRepository
        on { articleDao() } doReturn articleDao
        on { articleRepository() } doReturn articleRepository
        on { resourceDao() } doReturn resourceDao
        on { resourceRepository() } doReturn resourceRepository
        on { translationDao() } doReturn translationDao
    }
}
