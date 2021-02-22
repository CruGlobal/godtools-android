package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpacerTest {
    @Test
    fun testParseSpacer() {
        val spacer = Spacer(Manifest(), getXmlParserForResource("spacer.xml"))
        assertNotNull(spacer)
    }
}
