package org.cru.godtools.tract.analytics.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TractPageAnalyticsScreenEventTest {
    private val manifest = Manifest(code = "tool", locale = Locale.FRANCE)
    private val page = spyk(TractPage(manifest)) {
        every { position } returns 0
    }

    @Test
    fun testEventGeneration() {
        val event = TractPageAnalyticsScreenEvent(page)
        assertEquals(manifest.locale, event.locale)
        assertEquals(manifest.code, event.appSection)
    }

    @Test
    fun testScreenNameGeneration() {
        every { page.position } returns 1
        assertEquals("tool-1", TractPageAnalyticsScreenEvent(page).screen)
        assertEquals("tool-1a", TractPageAnalyticsScreenEvent(page, mockk { every { position } returns 0 }).screen)
        assertEquals("tool-1b", TractPageAnalyticsScreenEvent(page, mockk { every { position } returns 1 }).screen)
        assertEquals("tool-1-100", TractPageAnalyticsScreenEvent(page, mockk { every { position } returns 100 }).screen)
    }

    @Test
    fun testSupportedSystems() {
        val event = TractPageAnalyticsScreenEvent(page)
        assertTrue(event.isForSystem(AnalyticsSystem.FIREBASE))
        assertFalse(event.isForSystem(AnalyticsSystem.APPSFLYER))
        assertFalse(event.isForSystem(AnalyticsSystem.USER))
    }
}
