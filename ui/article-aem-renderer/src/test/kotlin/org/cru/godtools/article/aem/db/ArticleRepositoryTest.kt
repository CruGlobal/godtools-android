package org.cru.godtools.article.aem.db

import io.mockk.coVerifyAll
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.cru.godtools.article.aem.model.Article
import org.junit.Test

class ArticleRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : ArticleRepository(db) {}

    @Test
    fun `updateContent()`() = runTest {
        val article = Article(mockk()).apply {
            contentUuid = UUID.randomUUID().toString()
            content = UUID.randomUUID().toString()
            resources = listOf(mockk(), mockk())
        }

        repo.updateContent(article)
        coVerifyAll {
            articleDao.updateContent(article.uri, article.contentUuid, article.content)
            resourceRepository.storeResources(article, article.resources)
        }
    }

    @Test
    fun `removeOrphanedArticles()`() = runTest {
        repo.removeOrphanedArticles()

        coVerifyAll {
            articleDao.removeOrphanedArticles()
            resourceDao.removeOrphanedResources()
        }
    }
}
