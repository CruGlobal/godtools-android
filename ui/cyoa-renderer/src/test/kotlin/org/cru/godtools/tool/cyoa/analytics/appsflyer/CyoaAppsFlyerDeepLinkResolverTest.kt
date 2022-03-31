package org.cru.godtools.tool.cyoa.analytics.appsflyer

import android.content.Context
import android.content.Intent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyAll
import io.mockk.verifyOrder
import java.util.Locale
import org.cru.godtools.base.ui.createCyoaActivityIntent
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class CyoaAppsFlyerDeepLinkResolverTest {
    private val context = mockk<Context>()
    private val intent = mockk<Intent>()

    @Before
    fun setupMocks() {
        mockkStatic("org.cru.godtools.base.ui.ActivitiesKt")
        every { context.createCyoaActivityIntent(any(), *anyVararg(), pageId = any()) } returns intent
    }

    @Test
    fun testInvalidDeepLinks() {
        listOf("tool|tract|kgp|en", "tool|cyoa|", "tool|cyoa|test").forEach {
            assertNull("$it should not resolve to an intent", CyoaAppsFlyerDeepLinkResolver.resolve(context, it))
            verifyAll {
                context.createCyoaActivityIntent(any(), *anyVararg()) wasNot Called
            }
        }
    }

    @Test
    fun testToolDeepLink() {
        assertSame(intent, CyoaAppsFlyerDeepLinkResolver.resolve(context, "tool|cyoa|test|en-US"))
        verifyOrder {
            context.createCyoaActivityIntent("test", Locale.forLanguageTag("en-US"))
        }
    }

    @Test
    fun testToolDeepLinkWithPage() {
        assertSame(intent, CyoaAppsFlyerDeepLinkResolver.resolve(context, "tool|cyoa|test|en-US|pageId"))
        verifyOrder {
            context.createCyoaActivityIntent("test", Locale.forLanguageTag("en-US"), pageId = "pageId")
        }
    }
}
