package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalyticsEventTest {
    @Test
    fun testParseAnalyticsEvent() {
        val event = AnalyticsEvent.fromXml(getXmlParserForResource("analytics_event.xml"))
        assertThat(event.action, equalTo("test"))
        AnalyticsSystem.values().filterNot { it == AnalyticsSystem.ADOBE }.forEach {
            assertFalse(event.isForSystem(it))
        }
        assertTrue(event.isForSystem(AnalyticsSystem.ADOBE))
        assertTrue(event.isTriggerType(AnalyticsEvent.Trigger.DEFAULT))
        assertEquals(50, event.delay)
        assertThat(event.attributes, hasEntry("attr", "value"))
        assertEquals(1, event.attributes.size)
    }

    @Test
    fun testParseAnalyticsEvents() {
        val events = AnalyticsEvent.fromEventsXml(getXmlParserForResource("analytics_events.xml"))
        assertThat(
            events,
            containsInAnyOrder(
                hasProperty("action", equalTo("event1")),
                hasProperty("action", equalTo("event2"))
            )
        )
    }
}
