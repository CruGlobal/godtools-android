package org.cru.godtools.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.cru.godtools.base.util.getDisplayName

class LanguageTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Language::class.java)
            .addConverters(LocaleTypeConverter)
            .build()
    }

    @Test
    fun `jsonapi parsing - valid`() {
        val language = parseJson("language.json")

        assertTrue(language.isValid)
        assertEquals(1, language.apiId)
        assertEquals(true, language.isForcedName)
        assertEquals(Locale.ENGLISH, language.code)
        assertEquals("English", language.name)
    }

    @Test
    fun `jsonapi parsing - invalid - code missing`() {
        val language = parseJson("language_invalid_code_missing.json")
        assertFalse(language.isValid, "missing language code is invalid")
    }

    @Test
    fun `jsonapi parsing - invalid - code null`() {
        val language = parseJson("language_invalid_code_null.json")
        assertFalse(language.isValid, "null language code is invalid")
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), Language::class.java).dataSingle!! }
    // endregion jsonapi parsing

    // region getDisplayName()
    @Test
    fun `getDisplayName()`() {
        mockkStatic("org.cru.godtools.base.util.LocaleKt") {
            every { any<Locale>().getDisplayName(any(), any(), any()) } returns "DisplayName"

            val context: Context = mockk()
            val inLocale: Locale = Locale.CANADA_FRENCH
            assertEquals("DisplayName", Language(Locale.ENGLISH, name = "name").getDisplayName(context, inLocale))
            verifyAll {
                Locale.ENGLISH.getDisplayName(context, "name", inLocale)
            }
        }
    }

    @Test
    fun `getDisplayName() - isForcedName=true`() {
        val inLocale: Locale = Locale.ENGLISH
        val language = Language(
            code = inLocale,
            name = "English - Tester",
            isForcedName = true
        )
        val context: Context = mockk()

        assertEquals("English - Tester", language.getDisplayName(context, inLocale))
    }
    // endregion getDisplayName()
}
