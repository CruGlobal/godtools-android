package org.cru.godtools.ui.dashboard

import android.content.Context
import android.content.Intent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyAll
import org.cru.godtools.base.ui.createDashboardIntent
import org.cru.godtools.base.ui.dashboard.Page
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DashboardAppsFlyerDeepLinkResolverTest {
    private val context = mockk<Context>()
    private val intent = mockk<Intent>()

    @Before
    fun setupMocks() {
        mockkStatic("org.cru.godtools.base.ui.ActivitiesKt")
        every { context.createDashboardIntent(any()) } returns intent
    }

    @After
    fun cleanupMocks() {
        unmockkStatic("org.cru.godtools.base.ui.ActivitiesKt")
    }

    @Test
    fun testInvalidDeepLinks() {
        listOf("tool|tract|kgp|en", "tool|cyoa|", "tool|cyoa|test", "tool|cyoa|test|en").forEach {
            assertNull("$it should not resolve to an intent", DashboardAppsFlyerDeepLinkResolver.resolve(context, it))
            verify { context.createDashboardIntent(any()) wasNot Called }
        }
    }

    @Test
    fun testDeepLinkDashboardLessons() {
        assertSame(intent, DashboardAppsFlyerDeepLinkResolver.resolve(context, "dashboard|lessons"))
        verifyAll { context.createDashboardIntent(Page.LESSONS) }
    }

    @Test
    fun testDeepLinkDashboardHome() {
        assertSame(intent, DashboardAppsFlyerDeepLinkResolver.resolve(context, "dashboard"))
        verifyAll { context.createDashboardIntent(Page.HOME) }

        assertSame(intent, DashboardAppsFlyerDeepLinkResolver.resolve(context, "dashboard|home"))
        verifyAll { context.createDashboardIntent(Page.HOME) }
    }

    @Test
    fun testDeepLinkDashboardTools() {
        assertSame(intent, DashboardAppsFlyerDeepLinkResolver.resolve(context, "dashboard|tools"))
        verifyAll { context.createDashboardIntent(Page.ALL_TOOLS) }
    }

    @Test
    fun testDeepLinkDashboardDefault() {
        assertSame(intent, DashboardAppsFlyerDeepLinkResolver.resolve(context, "dashboard|non-existant"))
        verifyAll { context.createDashboardIntent(Page.HOME) }
    }
}
