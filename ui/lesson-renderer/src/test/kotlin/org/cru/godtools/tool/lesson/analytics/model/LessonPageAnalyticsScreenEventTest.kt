package org.cru.godtools.tool.lesson.analytics.model

import io.fluidsonic.locale.toCommon
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonPageAnalyticsScreenEventTest {
    @Test
    fun testSupportedSystems() {
        val page = mockk<LessonPage> {
            every { manifest } returns Manifest(
                code = "tool",
                locale = Locale.ENGLISH.toCommon()
            )
            every { position } returns 1
        }
        val event = LessonPageAnalyticsScreenEvent(page)
        assertTrue(event.isForSystem(AnalyticsSystem.FIREBASE))
        assertFalse(event.isForSystem(AnalyticsSystem.USER))
    }
}
