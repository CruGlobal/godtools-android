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
import kotlin.test.Test
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.ui.createArticlesIntent
import org.cru.godtools.tool.article.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
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
    fun `processIntent() - godtoolsapp_com Deep Link`() {
        deepLinkScenario(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/tool/article/$TOOL/en")) {
            it.onActivity {
                assertEquals(TOOL, it.tool)
                assertEquals(Locale.ENGLISH, it.locale)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Custom URI Deep Link`() {
        deepLinkScenario(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/tool/article/$TOOL/en")) {
            it.onActivity {
                assertEquals(TOOL, it.tool)
                assertEquals(Locale.ENGLISH, it.locale)
                assertFalse(it.isFinishing)
            }
        }
    }
    // endregion Intent processing
}
