package org.cru.godtools.tract.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.model.AnalyticsEvent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun testFirebaseParamsAdobeMigration() {
        val adobeEvent =
            ContentAnalyticsActionEvent(
                AnalyticsEvent(systems = setOf(AnalyticsSystem.ADOBE), attributes = mapOf("cru.Key" to "value"))
            )
        assertThat(adobeEvent.adobeAttributes, hasEntry("cru.Key", "value"))
        with(adobeEvent.firebaseParams) {
            assertTrue(containsKey("cru_key"))
            assertFalse(containsKey("cru.Key"))
            assertEquals("value", getString("cru_key"))
        }

        val firebaseEvent =
            ContentAnalyticsActionEvent(
                AnalyticsEvent(systems = setOf(AnalyticsSystem.FIREBASE), attributes = mapOf("cru.Key" to "value"))
            )
        with(firebaseEvent.firebaseParams) {
            assertFalse(containsKey("cru_key"))
            assertTrue(containsKey("cru.Key"))
            assertEquals("value", getString("cru.Key"))
        }
    }
}
