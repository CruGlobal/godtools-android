package org.cru.godtools.article.aem.db

import io.mockk.every
import io.mockk.mockk

abstract class AbstractArticleRoomDatabaseTest {
    internal val aemImportDao = mockk<AemImportDao>(relaxUnitFun = true)
    internal val aemImportRepository = mockk<AemImportRepository>()
    internal val articleDao = mockk<ArticleDao>(relaxUnitFun = true)
    internal val articleRepository = mockk<ArticleRepository>()
    internal val resourceDao = mockk<ResourceDao>(relaxUnitFun = true)
    internal val resourceRepository = mockk<ResourceRepository>()
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
