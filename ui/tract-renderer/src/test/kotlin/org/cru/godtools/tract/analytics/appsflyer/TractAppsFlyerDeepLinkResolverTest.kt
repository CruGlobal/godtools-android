package org.cru.godtools.tract.analytics.appsflyer

import android.content.Context
import android.content.Intent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import org.cru.godtools.base.ui.createTractActivityIntent
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class TractAppsFlyerDeepLinkResolverTest {
    private val context = mockk<Context>()
    private val intent = mockk<Intent>()

    @Before
    fun setupMocks() {
        mockkStatic("org.cru.godtools.base.ui.ActivitiesKt")
        every { context.createTractActivityIntent(any(), *anyVararg(), page = any()) } returns intent
    }

    @Test
    fun testInvalidDeepLinks() {
        listOf("", "dashboard", "tool|lesson|kgp|en", "tool|cyoa|", "tool|cyoa|test").forEach {
            assertNull("$it should not resolve to an intent", TractAppsFlyerDeepLinkResolver.resolve(context, it))
            verify {
                context.createTractActivityIntent(any(), *anyVararg(), page = any(), showTips = any()) wasNot Called
            }
        }
    }

    @Test
    fun testTractDeepLink() {
        assertSame(intent, TractAppsFlyerDeepLinkResolver.resolve(context, "tool|tract|test|en-US"))
        verifyAll { context.createTractActivityIntent("test", Locale.forLanguageTag("en-US")) }
    }

    @Test
    fun testTractDeepLinkWithPage() {
        assertSame(intent, TractAppsFlyerDeepLinkResolver.resolve(context, "tool|tract|test|en-US|5"))
        verifyAll { context.createTractActivityIntent("test", Locale.forLanguageTag("en-US"), page = 5) }
    }
}
