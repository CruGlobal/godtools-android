package org.cru.godtools.tract.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.model.AnalyticsEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentAnalyticsActionEventTest {
    @Test
    fun testFirebaseEventNameAdobeMigration() {
        val adobeEvent =
            ContentAnalyticsActionEvent(AnalyticsEvent(action = "A b_c.d-e", systems = setOf(AnalyticsSystem.ADOBE)))
        assertEquals("a_b_c_d_e", adobeEvent.firebaseEventName)

        val firebaseEvent =
            ContentAnalyticsActionEvent(AnalyticsEvent(action = "A b_c.d-e", systems = setOf(AnalyticsSystem.FIREBASE)))
        assertEquals("A b_c.d-e", firebaseEvent.firebaseEventName)
    }
}
