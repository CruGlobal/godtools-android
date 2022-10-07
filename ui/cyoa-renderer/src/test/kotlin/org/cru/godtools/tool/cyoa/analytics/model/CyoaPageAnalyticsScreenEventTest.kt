package org.cru.godtools.tool.cyoa.analytics.model

import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.page.Page
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val TOOL = "tool"
private const val PAGE = "page"

class CyoaPageAnalyticsScreenEventTest {
    private val page: Page = mockk {
        every { manifest } returns Manifest(code = TOOL, locale = Locale.ENGLISH)
        every { id } returns PAGE
    }

    @Test
    fun `Property - screen`() {
        assertEquals("tool:page", CyoaPageAnalyticsScreenEvent(page).screen)
    }

    @Test
    fun `isForSystem() - Supported`() {
        val event = CyoaPageAnalyticsScreenEvent(page)
        assertTrue(event.isForSystem(AnalyticsSystem.FIREBASE))
    }

    @Test
    fun `isForSystem() - Not Supported`() {
        val event = CyoaPageAnalyticsScreenEvent(page)
        assertFalse(event.isForSystem(AnalyticsSystem.APPSFLYER))
    }
}
