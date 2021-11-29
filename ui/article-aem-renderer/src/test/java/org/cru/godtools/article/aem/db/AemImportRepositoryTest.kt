package org.cru.godtools.article.aem.db

import android.net.Uri
import java.util.Date
import kotlinx.coroutines.test.runBlockingTest
import org.ccci.gto.android.common.base.TimeConstants.WEEK_IN_MS
import org.cru.godtools.article.aem.model.AemImport
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock

class AemImportRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : AemImportRepository(db) {}

    @Test
    fun `accessAemImport()`() = runBlockingTest {
        val uri = mock<Uri>()
        val start = Date()
        repo.accessAemImport(uri)
        val end = Date()

        inOrder(aemImportDao) {
            verify(aemImportDao).insertOrIgnore(argThat<AemImport> { this.uri == uri })
            argumentCaptor<Date> {
                verify(aemImportDao).updateLastAccessed(eq(uri), capture())

                assertTrue(start == firstValue || start.before(firstValue))
                assertTrue(end == firstValue || end.after(firstValue))
            }
            verifyNoMoreInteractions()
        }
    }

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
}
