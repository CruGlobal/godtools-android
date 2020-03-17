package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseHeader() {
        val header = Header(Page(manifest, 1), getXmlParserForResource("header.xml"))
        assertEquals("5", header.number!!.text)
        assertEquals("title", header.title!!.text)
        assertEquals(Color.RED, header.backgroundColor)
    }
}
