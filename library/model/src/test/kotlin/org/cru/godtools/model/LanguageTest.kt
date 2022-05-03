package org.cru.godtools.model

import java.util.Locale
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Language::class.java)
            .addConverters(LocaleTypeConverter)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val language = parseJson("language.json")

        assertEquals(1, language.id)
        assertEquals(Locale.ENGLISH, language.code)
        assertEquals("English", language.name)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), Language::class.java).dataSingle!! }
    // endregion jsonapi parsing
}
