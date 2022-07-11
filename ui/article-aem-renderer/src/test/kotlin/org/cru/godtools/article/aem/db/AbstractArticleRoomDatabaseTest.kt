package org.cru.godtools.article.aem.db

import io.mockk.every
import io.mockk.mockk
import org.mockito.kotlin.mock

abstract class AbstractArticleRoomDatabaseTest {
    internal val aemImportDao = mock<AemImportDao>()
    internal val aemImportRepository = mockk<AemImportRepository>(relaxUnitFun = true)
    internal val articleDao = mockk<ArticleDao>(relaxUnitFun = true)
    internal val articleRepository = mock<ArticleRepository>()
    internal val resourceDao = mock<ResourceDao>()
    internal val resourceRepository = mock<ResourceRepository>()
    internal val translationDao = mockk<TranslationDao>(relaxUnitFun = true)
    protected val db = mockk<ArticleRoomDatabase> {
        every { aemImportDao() } returns aemImportDao
        every { aemImportRepository() } returns aemImportRepository
        every { articleDao() } returns articleDao
        every { articleRepository() } returns articleRepository
        every { resourceDao() } returns resourceDao
        every { resourceRepository() } returns resourceRepository
        every { translationDao() } returns translationDao
    }
}
