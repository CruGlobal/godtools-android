package org.cru.godtools.article.aem.db

import java.util.Date
import kotlinx.coroutines.test.runBlockingTest
import org.ccci.gto.android.common.base.TimeConstants.WEEK_IN_MS
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder

class AemImportRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : AemImportRepository(db) {}

    // region removeOrphanedAemImports()
    @Test
    fun `removeOrphanedAemImports()`() = runBlockingTest {
        repo.removeOrphanedAemImports()

        val accessedBefore = argumentCaptor<Date>()
        inOrder(aemImportDao, articleRepository) {
            verify(aemImportDao).removeOrphanedAemImports(accessedBefore.capture())
            verify(articleRepository).removeOrphanedArticles()
            verifyNoMoreInteractions()
        }
        assertThat(accessedBefore.firstValue.time, lessThanOrEqualTo(System.currentTimeMillis() - WEEK_IN_MS))
    }
    // endregion removeOrphanedAemImports()
}
