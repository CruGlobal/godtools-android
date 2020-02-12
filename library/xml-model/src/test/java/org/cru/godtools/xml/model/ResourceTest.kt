package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResourceTest {
    @Test
    fun testParseResource() {
        val resource = Resource.fromXml(Manifest(), getXmlParserForResource("resource.xml"))
        assertEquals("filename.ext", resource.name)
        assertEquals("srcValue", resource.localName)
    }
}
