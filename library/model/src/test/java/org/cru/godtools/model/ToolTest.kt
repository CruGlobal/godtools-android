package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.cru.godtools.model.jsonapi.ToolTypeConverter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolTest {
    @Test
    fun testToolParsing() {
        val raw = this::class.java.getResourceAsStream("tool.json")!!.reader().use { it.readText() }
        val tool = JsonApiConverter.Builder()
            .addClasses(Tool::class.java)
            .addClasses(Attachment::class.java, Translation::class.java)
            .addConverters(ToolTypeConverter)
            .build()
            .fromJson(raw, Tool::class.java).dataSingle!!

        assertEquals(1, tool.id)
        assertEquals("kgp-us", tool.code)
        assertEquals("Know God Personally", tool.name)
        assertEquals("conversation_starter", tool.category)
        assertEquals("Gospel presentation", tool.description)
        assertEquals(12345, tool.shares)
        assertEquals(Tool.Type.TRACT, tool.type)
        assertEquals(1L, tool.bannerId)
        assertEquals(2L, tool.detailsBannerId)
        assertEquals(10, tool.defaultOrder)
        assertThat(tool.attachments, hasSize(3))
        assertThat(tool.latestTranslations, hasSize(2))
    }

    @Test
    fun testTotalShares() {
        val tool = Tool().apply {
            shares = 1
            pendingShares = 2
        }

        assertEquals(3, tool.totalShares)
    }
}
