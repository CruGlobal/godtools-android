package org.cru.godtools.base.tool.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tool.model.AnalyticsEvent
import org.cru.godtools.tool.model.AnalyticsEvent.System.ADOBE
import org.cru.godtools.tool.model.AnalyticsEvent.System.FIREBASE
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentAnalyticsEventAnalyticsActionEventTest {
    @Test
    fun testFirebaseEventNameAdobeMigration() {
        val adobeEvent =
            ContentAnalyticsEventAnalyticsActionEvent(AnalyticsEvent(action = "A b_c.d-e", systems = setOf(ADOBE)))
        assertEquals("a_b_c_d_e", adobeEvent.firebaseEventName)

        val firebaseEvent =
            ContentAnalyticsEventAnalyticsActionEvent(AnalyticsEvent(action = "A b_c.d-e", systems = setOf(FIREBASE)))
        assertEquals("A b_c.d-e", firebaseEvent.firebaseEventName)
    }

    @Test
    fun testFirebaseParamsAdobeMigration() {
        val adobeEvent =
            ContentAnalyticsEventAnalyticsActionEvent(
                AnalyticsEvent(systems = setOf(ADOBE), attributes = mapOf("cru.Key" to "value"))
            )
        assertThat(adobeEvent.adobeAttributes, hasEntry("cru.Key", "value"))
        with(adobeEvent.firebaseParams) {
            assertTrue(containsKey("cru_key"))
            assertFalse(containsKey("cru.Key"))
            assertEquals("value", getString("cru_key"))
        }

        val firebaseEvent =
            ContentAnalyticsEventAnalyticsActionEvent(
                AnalyticsEvent(systems = setOf(FIREBASE), attributes = mapOf("cru.Key" to "value"))
            )
        with(firebaseEvent.firebaseParams) {
            assertFalse(containsKey("cru_key"))
            assertTrue(containsKey("cru.Key"))
            assertEquals("value", getString("cru.Key"))
        }
    }
}
