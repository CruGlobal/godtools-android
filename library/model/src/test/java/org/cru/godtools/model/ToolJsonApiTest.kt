package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.cru.godtools.model.jsonapi.ToolTypeConverter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolJsonApiTest {
    private val parser = JsonApiConverter.Builder()
        .addClasses(Tool::class.java)
        .addClasses(Attachment::class.java, Translation::class.java)
        .addConverters(ToolTypeConverter)
        .build()

    @Test
    fun parseTool() {
        val raw = this::class.java.getResourceAsStream("tool.json")!!.reader().use { it.readText() }
        val tool = parser.fromJson(raw, Tool::class.java).dataSingle!!

        assertEquals(1, tool.id)
        assertEquals("kgp-us", tool.code)
        assertEquals("Gospel presentation", tool.description)
        assertEquals(12345, tool.shares)
        assertEquals(Tool.Type.TRACT, tool.type)
        assertEquals(1, tool.bannerId)
        assertEquals(2, tool.detailsBannerId)
        assertThat(tool.attachments, hasSize(3))
        assertThat(tool.latestTranslations, hasSize(2))
    }
}
