package org.cru.godtools.article.aem.db

import android.net.Uri
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.base.TimeConstants.WEEK_IN_MS
import org.cru.godtools.article.aem.model.AemImport
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AemImportRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : AemImportRepository(db) {}

    @Test
    fun `accessAemImport()`() = runTest {
        val uri = mockk<Uri>()
        val start = Date()
        repo.accessAemImport(uri)
        val end = Date()

        coVerifySequence {
            aemImportDao.insertOrIgnore(match<AemImport> { it.uri === uri })
            aemImportDao.updateLastAccessed(uri, match { it in start..end })
        }
    }

    @Test
    fun `removeOrphanedAemImports()`() = runTest {
        coEvery { articleRepository.removeOrphanedArticles() } just Runs

        repo.removeOrphanedAemImports()
        coVerifySequence {
            aemImportDao.removeOrphanedAemImports(match { it.time <= System.currentTimeMillis() - WEEK_IN_MS })
            articleRepository.removeOrphanedArticles()
        }
    }
}
