package org.cru.godtools.article.aem.db

import java.util.UUID
import kotlinx.coroutines.test.runBlockingTest
import org.cru.godtools.article.aem.model.Article
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class ArticleRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : ArticleRepository(db) {}

    @Test
    fun `updateContent()`() = runBlockingTest {
        val article = Article(mock()).apply {
            contentUuid = UUID.randomUUID().toString()
            content = UUID.randomUUID().toString()
            resources = listOf(mock(), mock())
        }

        repo.updateContent(article)
        verify(articleDao).updateContent(article.uri, article.contentUuid, article.content)
        verify(resourceRepository).storeResources(article, article.resources)
        verifyNoMoreInteractions(articleDao, resourceRepository)
    }

    @Test
    fun `removeOrphanedArticles()`() = runBlockingTest {
        repo.removeOrphanedArticles()

        verify(articleDao).removeOrphanedArticles()
        verify(resourceDao).removeOrphanedResources()
        verifyNoMoreInteractions(articleDao, resourceDao)
    }
}
