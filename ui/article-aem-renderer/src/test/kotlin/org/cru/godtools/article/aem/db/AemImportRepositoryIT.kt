package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.article.aem.model.Article
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class AemImportRepositoryIT {
    private val testScope = TestScope()

    @get:Rule
    internal val dbRule = RoomDatabaseRule(ArticleRoomDatabase::class.java)
    private val repository get() = dbRule.db.aemImportRepository()

    @Test
    fun `processAemImportSync() - GT-1780 - Missing AemImport`() = testScope.runTest {
        val aemImportUri = Uri.parse("https://example.com")
        val article = Article(aemImportUri.buildUpon().appendPath("article1").build())

        repository.processAemImportSync(aemImportUri, listOf(article))
    }
}
