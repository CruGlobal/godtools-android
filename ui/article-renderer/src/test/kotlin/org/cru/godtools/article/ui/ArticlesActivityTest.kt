package org.cru.godtools.article.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.Locale
import org.cru.godtools.base.ui.createArticlesIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class ArticlesActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    private fun <R> scenario(
        intent: Intent = context.createArticlesIntent(TOOL, Locale.ENGLISH),
        block: (ActivityScenario<ArticlesActivity>) -> R
    ) = ActivityScenario.launch<ArticlesActivity>(intent).use(block)
    private fun <R> deepLinkScenario(uri: Uri, block: (ActivityScenario<ArticlesActivity>) -> R) =
        scenario(Intent(Intent.ACTION_VIEW, uri), block)

    // region Intent processing
    @Test
    fun `processIntent() - Valid direct`() {
        scenario(context.createArticlesIntent(TOOL, Locale.ENGLISH)) {
            it.onActivity {
                assertEquals(TOOL, it.tool)
                assertEquals(Locale.ENGLISH, it.locale)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Custom URI Deep Link`() {
        deepLinkScenario(Uri.parse("godtools://org.cru.godtools.test/tool/article/$TOOL/en")) {
            it.onActivity {
                assertEquals(TOOL, it.tool)
                assertEquals(Locale.ENGLISH, it.locale)
                assertFalse(it.isFinishing)
            }
        }
    }
    // endregion Intent processing
}
