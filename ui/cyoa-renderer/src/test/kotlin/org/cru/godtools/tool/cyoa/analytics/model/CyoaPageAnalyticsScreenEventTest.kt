package org.cru.godtools.tool.cyoa.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.page.Page
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

private const val TOOL = "tool"
private const val PAGE = "page"

class CyoaPageAnalyticsScreenEventTest {
    private val manifest: Manifest = mock {
        on { code } doReturn TOOL
        on { locale } doReturn Locale.ENGLISH
    }
    private val page: Page = mock {
        on { manifest } doReturn manifest
        on { id } doReturn PAGE
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
