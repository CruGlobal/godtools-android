package org.cru.godtools.tract.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class TractPageAnalyticsScreenEventTest {
    private lateinit var manifest: Manifest
    private lateinit var page: TractPage

    @Before
    fun setupMocks() {
        manifest = mock {
            on { code } doReturn "tool"
            on { locale } doReturn mock()
        }
        page = mock {
            on { manifest } doReturn manifest
        }
    }

    @Test
    fun testEventGeneration() {
        val event = TractPageAnalyticsScreenEvent(page)
        assertSame(manifest.locale, event.locale)
        assertEquals("tool", event.appSection)
    }

    @Test
    fun testScreenNameGeneration() {
        whenever(page.position) doReturn 1
        assertEquals("tool-1", TractPageAnalyticsScreenEvent(page).screen)
        assertEquals("tool-1a", TractPageAnalyticsScreenEvent(page, mock { on { position } doReturn 0 }).screen)
        assertEquals("tool-1b", TractPageAnalyticsScreenEvent(page, mock { on { position } doReturn 1 }).screen)
        assertEquals("tool-1-100", TractPageAnalyticsScreenEvent(page, mock { on { position } doReturn 100 }).screen)
    }
}
