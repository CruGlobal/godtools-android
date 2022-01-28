package org.cru.godtools.tool.lesson.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.lesson.LessonPage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class LessonPageAnalyticsScreenEventTest {
    private lateinit var manifest: Manifest
    private lateinit var page: LessonPage

    @Before
    fun setupMocks() {
        manifest = mock {
            on { code } doReturn "tool"
            on { locale } doReturn Locale.ENGLISH
        }
        page = mock {
            on { manifest } doReturn manifest
            on { position } doReturn 1
        }
    }

    @Test
    fun testSupportedSystems() {
        val event = LessonPageAnalyticsScreenEvent(page)
        assertTrue(event.isForSystem(AnalyticsSystem.FIREBASE))
        assertFalse(event.isForSystem(AnalyticsSystem.APPSFLYER))
        assertFalse(event.isForSystem(AnalyticsSystem.USER))
    }
}
