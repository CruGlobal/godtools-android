package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpacerTest {
    @Test
    fun `parseSpacer - Defaults`() {
        val spacer = Spacer(Manifest(), getXmlParserForResource("spacer.xml"))
        assertNotNull(spacer)
        assertEquals(Spacer.Mode.AUTO, spacer.mode)
        assertEquals(0, spacer.height)
    }

    @Test
    fun `parseSpacer - Fixed Height`() {
        val spacer = Spacer(Manifest(), getXmlParserForResource("spacer_fixed.xml"))
        assertEquals(Spacer.Mode.FIXED, spacer.mode)
        assertEquals(123, spacer.height)
    }
}
