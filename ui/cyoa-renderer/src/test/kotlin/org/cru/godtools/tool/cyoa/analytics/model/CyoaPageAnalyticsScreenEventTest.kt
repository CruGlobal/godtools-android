package org.cru.godtools.tool.cyoa.analytics.model

import io.fluidsonic.locale.toCommon
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val TOOL = "tool"
private const val PAGE = "page"

class CyoaPageAnalyticsScreenEventTest {
    private val page: Page = mockk {
        every { manifest } returns Manifest(code = TOOL, locale = Locale.ENGLISH.toCommon())
        every { id } returns PAGE
    }

    @Test
    fun `Property - screen`() {
        assertEquals("$TOOL:$PAGE", CyoaPageAnalyticsScreenEvent(page).screen)
    }

    @Test
    fun `isForSystem() - Supported`() {
        val event = CyoaPageAnalyticsScreenEvent(page)
        assertTrue(event.isForSystem(AnalyticsSystem.FIREBASE))
    }

    @Test
    fun `isForSystem() - Not Supported`() {
        val event = CyoaPageAnalyticsScreenEvent(page)
        assertFalse(event.isForSystem(AnalyticsSystem.USER))
    }
}
