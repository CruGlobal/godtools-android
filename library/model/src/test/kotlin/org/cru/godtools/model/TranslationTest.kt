package org.cru.godtools.model

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter

class TranslationTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Translation::class.java)
            .addClasses(Tool::class.java, Language::class.java)
            .addConverters(LocaleTypeConverter)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val translation = parseJson("translation.json")

        assertEquals(1138, translation.id)
        assertEquals("rend", translation.toolCode)
        assertEquals(Locale.ENGLISH, translation.languageCode)
        assertEquals(155, translation.version)
        assertEquals("Renderer Testing", translation.name)
        assertEquals("Renderer Testing Description", translation.description)
        assertEquals("Renderer Testing Tagline", translation.tagline)
        assertEquals("Renderer Test Bible References", translation.toolDetailsBibleReferences)
        assertEquals("Renderer Test Conversation Starters", translation.toolDetailsConversationStarters)
        assertEquals("Renderer Test Outline", translation.toolDetailsOutline)
        assertEquals(
            "d13997976f4f9cd9ff6852d7d47afbada4f0f81e315e3061f996dd269391c2dd.xml",
            translation.manifestFileName
        )
        assertTrue(translation.isPublished)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), Translation::class.java).dataSingle!! }
    // endregion jsonapi parsing
}
