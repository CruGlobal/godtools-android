package org.cru.godtools.xml.model.tips

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.TOOL_CODE
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineTipTest {
    @Test
    fun verifyParseInlineTip() {
        val tip = InlineTip(Manifest(TOOL_CODE), getXmlParserForResource("inline_tip.xml"))
        assertEquals("tip1", tip.id)
    }

    @Test
    fun verifyTipAccessor() {
        val tip: Tip = mock { whenever(it.id).thenReturn("tip1") }
        val manifest = Manifest(tips = { listOf(tip) })

        val valid = InlineTip(manifest, "tip1")
        assertSame(tip, valid.tip)

        val missing = InlineTip(manifest, "tip2")
        assertNull(missing.tip)
    }
}
