package org.cru.godtools.tool.lesson.analytics.appsflyer

import android.content.Context
import android.content.Intent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyAll
import io.mockk.verifyOrder
import java.util.Locale
import org.cru.godtools.base.tool.createLessonActivityIntent
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class LessonAppsFlyerDeepLinkResolverTest {
    private val context = mockk<Context>()
    private val intent = mockk<Intent>()

    @Before
    fun setupMocks() {
        mockkStatic("org.cru.godtools.base.tool.ActivitiesKt")
        every { context.createLessonActivityIntent(any(), any()) } returns intent
    }

    @Test
    fun testInvalidDeepLinks() {
        listOf("", "dashboard", "tool|tract|kgp|en", "tool|cyoa|", "tool|cyoa|test").forEach {
            assertNull("$it should not resolve to an intent", LessonAppsFlyerDeepLinkResolver.resolve(context, it))
            verifyAll {
                context.createLessonActivityIntent(any(), any()) wasNot Called
            }
        }
    }

    @Test
    fun testLessonDeepLink() {
        assertSame(intent, LessonAppsFlyerDeepLinkResolver.resolve(context, "tool|lesson|test|en-US"))
        verifyOrder {
            context.createLessonActivityIntent("test", Locale.forLanguageTag("en-US"))
        }
    }
}
