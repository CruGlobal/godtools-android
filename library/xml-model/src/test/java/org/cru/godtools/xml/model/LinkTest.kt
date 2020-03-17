package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseLink() {
        val events = Event.Id.parse(TOOL_CODE, "ns:event1 event2")
        val link = Link(manifest, getXmlParserForResource("link.xml"))
        assertThat(link.events, containsInAnyOrder(*events.toTypedArray()))
        assertEquals("Test", link.text!!.text)
        assertThat(link.analyticsEvents, contains(instanceOf(AnalyticsEvent::class.java)))
    }
}
