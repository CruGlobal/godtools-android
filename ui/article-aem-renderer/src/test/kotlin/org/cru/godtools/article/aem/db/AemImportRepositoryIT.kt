package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AemImportRepositoryIT {
    private val testScope = TestScope()

    @get:Rule
    internal val dbRule = RoomDatabaseRule(ArticleRoomDatabase::class.java)
    private val db get() = dbRule.db
    private val repository get() = db.aemImportRepository()

    @Test
    fun `processAemImportSync()`() = testScope.runTest {
        val aemImportUri = Uri.parse("https://example.com")
        db.aemImportDao().insertOrIgnore(AemImport(aemImportUri))
        val articleUri = aemImportUri.buildUpon().appendPath("article1").build()
        val article = Article(articleUri)

        repository.processAemImportSync(aemImportUri, listOf(article))
        assertEquals(articleUri, db.articleDao().find(articleUri)!!.uri)
    }

    @Test
    fun `processAemImportSync() - GT-1780 - AemImport not in the database`() = testScope.runTest {
        val aemImportUri = Uri.parse("https://example.com")
        val articleUri = aemImportUri.buildUpon().appendPath("article1").build()

        repository.processAemImportSync(aemImportUri, listOf(Article(articleUri)))
        assertNull(db.articleDao().find(articleUri))
    }
}
