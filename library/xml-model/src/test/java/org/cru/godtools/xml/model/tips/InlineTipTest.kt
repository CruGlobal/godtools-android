package org.cru.godtools.xml.model.tips

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineTipTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseInlineTip() {
        val tip = InlineTip(manifest, getXmlParserForResource("inline_tip.xml"))
        assertEquals("tip1", tip.id)
    }
}
