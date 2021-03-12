package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.article.aem.model.Article
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleRoomDatabaseTest : BaseArticleRoomDatabaseIT() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun verifyGetAllArticles() {
        val articleDao = db.articleDao()
        val articles = articleDao.allArticles
        articles.observeForever { }
        for (i in 0..11) articleDao.insertOrIgnore(Article(Uri.parse("test:" + i + "aaslf" + i)))

        assertThat(articles.value, hasSize(12))
    }
}
