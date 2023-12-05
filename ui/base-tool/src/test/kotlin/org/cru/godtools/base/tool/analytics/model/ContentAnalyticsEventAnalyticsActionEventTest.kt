package org.cru.godtools.base.tool.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.System.FIREBASE
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.System.USER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentAnalyticsEventAnalyticsActionEventTest {
    @Test
    fun testFirebaseEventName() {
        val event = ContentAnalyticsEventAnalyticsActionEvent(
            AnalyticsEvent(action = "A b_c.d-e", systems = setOf(FIREBASE))
        )
        assertEquals("A b_c.d-e", event.firebaseEventName)
    }

    @Test
    fun testFirebaseParams() {
        val event = ContentAnalyticsEventAnalyticsActionEvent(
            AnalyticsEvent(systems = setOf(FIREBASE), attributes = mapOf("cru.Key" to "value"))
        )
        with(event.firebaseParams) {
            assertFalse(containsKey("cru_key"))
            assertTrue(containsKey("cru.Key"))
            assertEquals("value", getString("cru.Key"))
        }
    }

    @Test
    fun testUserAnalyticsEvent() {
        val event =
            ContentAnalyticsEventAnalyticsActionEvent(AnalyticsEvent(action = "userEvent", systems = setOf(USER)))
        assertEquals("userEvent", event.userCounterName)
    }
}
