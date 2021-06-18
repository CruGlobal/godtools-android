package org.cru.godtools.tract.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.tract.Card
import org.cru.godtools.tool.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TractPageAnalyticsScreenEventTest {
    @Test
    fun testEventGeneration() {
        val manifest = Manifest(code = "tool", locale = mock())
        val event = TractPageAnalyticsScreenEvent(TractPage(manifest))
        assertSame(manifest.locale, event.locale)
        assertEquals("tool", event.appSection)
    }

    @Test
    fun testScreenNameGeneration() {
        val manifest = Manifest(code = "code", tractPages = { listOf(TractPage(it), TractPage(it)) })
        val page = manifest.tractPages[1]
        assertEquals("code-1", TractPageAnalyticsScreenEvent(page).screen)
        assertEquals("code-1a", TractPageAnalyticsScreenEvent(page, Card(page)).screen)
        assertEquals("code-1b", TractPageAnalyticsScreenEvent(page, Card(page, 1)).screen)
        assertEquals("code-1-100", TractPageAnalyticsScreenEvent(page, Card(page, 100)).screen)
    }
}
