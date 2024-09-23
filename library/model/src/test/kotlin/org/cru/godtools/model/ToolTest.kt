package org.cru.godtools.model

import java.util.Locale
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
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
            .addConverters(ToolTypeConverter, LocaleTypeConverter)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val tool = parseJson("tool.json")

        assertTrue(tool.isValid)
        assertFalse(tool.isHidden)
        assertEquals("kgp-us", tool.code)
        assertEquals(Tool.Type.TRACT, tool.type)
        assertEquals("Know God Personally", tool.name)
        assertEquals("conversation_starter", tool.category)
        assertEquals("Gospel presentation", tool.description)
        assertEquals(12345, tool.shares)
        assertEquals(Tool.Type.TRACT, tool.type)
        assertEquals(1L, tool.bannerId)
        assertEquals(2L, tool.detailsBannerId)
        assertEquals(Tool.DEFAULT_DEFAULT_LOCALE, tool.defaultLocale)
        assertEquals(10, tool.defaultOrder)
        assertEquals(1L, tool.apiId)
        assertThat(tool.apiAttachments, hasSize(3))
        assertThat(tool.translations, hasSize(2))
    }

    @Test
    fun `jsonapi Parsing - code - missing`() {
        val tool = parseJson("tool_code_missing.json")
        assertFalse(tool.isValid)
        assertEquals(Tool.Type.TRACT, tool.type)
    }

    @Test
    fun `jsonapi Parsing - code - null`() {
        val tool = parseJson("tool_code_null.json")
        assertFalse(tool.isValid)
        assertEquals(Tool.Type.TRACT, tool.type)
    }

    @Test
    fun `jsonapi Parsing - code - empty`() {
        val tool = parseJson("tool_code_empty.json")
        assertFalse(tool.isValid)
        assertEquals(Tool.Type.TRACT, tool.type)
    }

    @Test
    fun `jsonapi Parsing - type - missing`() {
        val tool = parseJson("tool_type_missing.json")
        assertFalse(tool.isValid)
        assertEquals("tool", tool.code)
    }

    @Test
    fun `jsonapi Parsing - type - null`() {
        val tool = parseJson("tool_type_null.json")
        assertFalse(tool.isValid)
        assertEquals("tool", tool.code)
    }

    @Test
    fun `jsonapi Parsing - type - empty`() {
        val tool = parseJson("tool_type_invalid.json")
        assertFalse(tool.isValid)
        assertEquals("tool", tool.code)
    }

    @Test
    fun `jsonapi Parsing - defaultLocale`() {
        val tool = parseJson("tool_default_locale.json")
        assertEquals(Locale.FRENCH, tool.defaultLocale)
    }

    @Test
    fun `jsonapi Parsing - defaultLocale - missing`() {
        val tool = parseJson("tool_default_locale_missing.json")
        assertEquals(Tool.DEFAULT_DEFAULT_LOCALE, tool.defaultLocale)
    }

    @Test
    fun `jsonapi Parsing - defaultLocale - null`() {
        val tool = parseJson("tool_default_locale_null.json")
        assertEquals(Tool.DEFAULT_DEFAULT_LOCALE, tool.defaultLocale)
    }

    @Test
    fun `testJsonApiParsing - No Default Order`() {
        assertEquals(0, parseJson("tool_no_default_order.json").defaultOrder)
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
        val tool = Tool("tool", shares = 1, pendingShares = 2)

        assertEquals(3, tool.totalShares)
    }

    @Test
    fun testComparatorDefaultOrder() {
        val tool1 = Tool("", defaultOrder = 1)
        val tool2 = Tool("", defaultOrder = 2)

        assertEquals(0, Tool.COMPARATOR_DEFAULT_ORDER.compare(tool1, tool1))
        assertEquals(0, Tool.COMPARATOR_DEFAULT_ORDER.compare(tool2, tool2))
        assertTrue(Tool.COMPARATOR_DEFAULT_ORDER.compare(tool1, tool2) < 0)
        assertTrue(Tool.COMPARATOR_DEFAULT_ORDER.compare(tool2, tool1) > 0)
    }
}
