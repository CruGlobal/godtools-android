package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.cru.godtools.model.jsonapi.ToolTypeConverter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Tool::class.java)
            .addClasses(Attachment::class.java, Translation::class.java)
            .addConverters(ToolTypeConverter)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val tool = parseJson("tool.json")

        assertEquals(1, tool.id)
        assertFalse(tool.isHidden)
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
    fun `testJsonApiParsing - isHidden`() {
        assertTrue(parseJson("tool_hidden_true.json").isHidden)
        assertFalse(parseJson("tool_hidden_null.json").isHidden)
        assertFalse(parseJson("tool_hidden_invalid.json").isHidden)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), Tool::class.java).dataSingle!! }
    // endregion jsonapi parsing

    @Test
    fun testTotalShares() {
        val tool = Tool().apply {
            shares = 1
            pendingShares = 2
        }

        assertEquals(3, tool.totalShares)
    }
}
