package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TOOL_CODE = "test"

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
        assertEquals("5", header.number!!.mText)
        assertEquals("title", header.title!!.mText)
        assertEquals(Color.RED, header.backgroundColor)
    }
}
