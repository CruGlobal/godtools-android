package org.cru.godtools.article.analytics.appsflyer

import android.content.Context
import android.content.Intent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import org.cru.godtools.base.ui.createArticlesIntent
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ArticlesAppsFlyerDeepLinkResolverTest {
    private val context = mockk<Context>()
    private val intent = mockk<Intent>()

    @Before
    fun setupMocks() {
        mockkStatic("org.cru.godtools.base.ui.ActivitiesKt")
        every { context.createArticlesIntent(any(), any()) } returns intent
    }

    @Test
    fun testInvalidDeepLinks() {
        listOf("", "dashboard", "tool|lesson|kgp|en", "tool|cyoa|", "tool|cyoa|test").forEach {
            assertNull("$it should not resolve to an intent", ArticlesAppsFlyerDeepLinkResolver.resolve(context, it))
            verify { context.createArticlesIntent(any(), any()) wasNot Called }
        }
    }

    @Test
    fun testArticlesDeepLink() {
        Assert.assertSame(intent, ArticlesAppsFlyerDeepLinkResolver.resolve(context, "tool|article|test|en-US"))
        verifyAll { context.createArticlesIntent("test", Locale.forLanguageTag("en-US")) }
    }
}
