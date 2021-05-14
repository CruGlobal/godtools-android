package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimationTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseAnimation() {
        val events = EventId.parse("ns:event1 event2")
        val animation = Animation(manifest, getXmlParserForResource("animation.xml"))
        assertEquals("animation.json", animation.resourceName)
        assertTrue(animation.autoPlay)
        assertFalse(animation.loop)
        assertThat(animation.events, containsInAnyOrder(*events.toTypedArray()))
        assertEquals(EventId.parse("event1").toSet(), animation.playListeners)
        assertEquals(EventId.parse("event2").toSet(), animation.stopListeners)
    }
}
